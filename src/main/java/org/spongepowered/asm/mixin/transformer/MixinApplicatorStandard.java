package org.spongepowered.asm.mixin.transformer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.extensibility.IActivityContext;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.struct.Constructor;
import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.mixin.transformer.meta.MixinRenamed;
import org.spongepowered.asm.mixin.transformer.struct.Initialiser;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.transformer.throwables.MixinApplicatorException;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.ConstraintParser;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.asm.util.throwables.InvalidConstraintException;
import org.spongepowered.include.com.google.common.collect.ImmutableList;
import org.spongepowered.include.com.google.common.collect.ImmutableSet;

class MixinApplicatorStandard {
   protected static final List<Class<? extends Annotation>> CONSTRAINED_ANNOTATIONS = ImmutableList.of(
      Overwrite.class, Inject.class, ModifyArg.class, ModifyArgs.class, Redirect.class, ModifyVariable.class, ModifyConstant.class
   );
   protected static final Set<Integer> ORDERS_NONE = ImmutableSet.of(0);
   protected final ILogger logger = MixinService.getService().getLogger("mixin");
   protected final TargetClassContext context;
   protected final String targetName;
   protected final ClassNode targetClass;
   protected final ClassInfo targetClassInfo;
   protected final Profiler profiler = Profiler.getProfiler("mixin");
   protected final IMixinAuditTrail auditTrail;
   protected final ActivityStack activities = new ActivityStack();
   protected final boolean mergeSignatures;

   MixinApplicatorStandard(TargetClassContext context) {
      this.context = context;
      this.targetName = context.getClassName();
      this.targetClass = context.getClassNode();
      this.targetClassInfo = context.getClassInfo();
      ExtensionClassExporter exporter = context.getExtensions().getExtension(ExtensionClassExporter.class);
      this.mergeSignatures = exporter.isDecompilerActive()
         && MixinEnvironment.getCurrentEnvironment().getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE_MERGESIGNATURES);
      this.auditTrail = MixinService.getService().getAuditTrail();
   }

   final void apply(SortedSet<MixinInfo> mixins) {
      List<MixinTargetContext> mixinContexts = new ArrayList<>();
      Iterator<MixinInfo> iter = mixins.iterator();

      while (iter.hasNext()) {
         MixinInfo mixin = iter.next();

         try {
            this.logger.log(mixin.getLoggingLevel(), "Mixing {} from {} into {}", mixin.getName(), mixin.getParent(), this.targetName);
            mixinContexts.add(mixin.createContextFor(this.context));
            if (this.auditTrail != null) {
               this.auditTrail.onApply(this.targetName, mixin.toString());
            }
         } catch (InvalidMixinException var21) {
            if (mixin.isRequired()) {
               throw var21;
            }

            this.context.addSuppressed(var21);
            iter.remove();
         }
      }

      MixinTargetContext current = null;
      this.activities.clear();

      try {
         IActivityContext.IActivity activity = this.activities.begin("PreApply Phase");
         IActivityContext.IActivity preApplyActivity = this.activities.begin("Mixin");

         for (MixinTargetContext context : mixinContexts) {
            preApplyActivity.next(context.toString());
            current = context;
            context.preApply(this.targetName, this.targetClass);
         }

         preApplyActivity.end();

         for (MixinApplicatorStandard.ApplicatorPass pass : MixinApplicatorStandard.ApplicatorPass.values()) {
            activity.next("%s Applicator Phase", pass);
            Profiler.Section timer = this.profiler.begin("pass", pass.name().toLowerCase(Locale.ROOT));
            IActivityContext.IActivity applyActivity = this.activities.begin("Mixin");
            Set<Integer> orders = ORDERS_NONE;
            if (pass == MixinApplicatorStandard.ApplicatorPass.INJECT_APPLY) {
               orders = new TreeSet<>();

               for (MixinTargetContext context : mixinContexts) {
                  context.getInjectorOrders(orders);
               }
            }

            for (Integer injectorOrder : orders) {
               Iterator<MixinTargetContext> iterx = mixinContexts.iterator();

               while (iterx.hasNext()) {
                  current = iterx.next();
                  applyActivity.next(current.toString());

                  try {
                     this.applyMixin(current, pass, injectorOrder);
                  } catch (InvalidMixinException var17) {
                     if (current.isRequired()) {
                        throw var17;
                     }

                     this.context.addSuppressed(var17);
                     iterx.remove();
                  }
               }
            }

            applyActivity.end();
            timer.end();
         }

         activity.next("PostApply Phase");
         IActivityContext.IActivity postApplyActivity = this.activities.begin("Mixin");
         Iterator<MixinTargetContext> iterx = mixinContexts.iterator();

         while (iterx.hasNext()) {
            current = iterx.next();
            postApplyActivity.next(current.toString());

            try {
               current.postApply(this.targetName, this.targetClass);
            } catch (InvalidMixinException var18) {
               if (current.isRequired()) {
                  throw var18;
               }

               this.context.addSuppressed(var18);
               iterx.remove();
            }
         }

         activity.end();
      } catch (InvalidMixinException var19) {
         var19.prepend(this.activities);
         throw var19;
      } catch (Exception var20) {
         throw new MixinApplicatorException(
            current, "Unexpecteded " + var20.getClass().getSimpleName() + " whilst applying the mixin class:", var20, this.activities
         );
      }

      this.applySourceMap(this.context);
      this.context.processDebugTasks();
   }

   protected final void applyMixin(MixinTargetContext mixin, MixinApplicatorStandard.ApplicatorPass pass, int injectorOrder) {
      IActivityContext.IActivity activity = this.activities.begin("Apply");
      switch (pass) {
         case MAIN:
            activity.next("Apply Signature");
            this.applySignature(mixin);
            activity.next("Apply Interfaces");
            this.applyInterfaces(mixin);
            activity.next("Apply Attributess");
            this.applyAttributes(mixin);
            activity.next("Apply Annotations");
            this.applyAnnotations(mixin);
            activity.next("Apply Fields");
            this.applyFields(mixin);
            activity.next("Apply Methods");
            this.applyMethods(mixin);
            activity.next("Apply Initialisers");
            this.applyInitialisers(mixin);
            break;
         case INJECT_PREPARE:
            activity.next("Prepare Injections");
            this.prepareInjections(mixin);
            break;
         case ACCESSOR:
            activity.next("Apply Accessors");
            this.applyAccessors(mixin);
            break;
         case INJECT_PREINJECT:
            activity.next("Apply Injections");
            this.applyPreInjections(mixin);
            break;
         case INJECT_APPLY:
            activity.next("Apply Injections");
            this.applyInjections(mixin, injectorOrder);
            break;
         default:
            throw new IllegalStateException("Invalid pass specified " + pass);
      }

      activity.end();
   }

   protected void applySignature(MixinTargetContext mixin) {
      if (this.mergeSignatures) {
         this.context.mergeSignature(mixin.getSignature());
      }
   }

   protected void applyInterfaces(MixinTargetContext mixin) {
      for (String interfaceName : mixin.getInterfaces()) {
         if (!this.targetClass.interfaces.contains(interfaceName)) {
            this.targetClass.interfaces.add(interfaceName);
            this.targetClassInfo.addInterface(interfaceName);
         }
      }
   }

   protected void applyAttributes(MixinTargetContext mixin) {
      if (mixin.shouldSetSourceFile()) {
         this.targetClass.sourceFile = mixin.getSourceFile();
      }

      int requiredVersion = mixin.getMinRequiredClassVersion();
      if ((requiredVersion & 65535) > (this.targetClass.version & 65535)) {
         this.targetClass.version = requiredVersion;
      }
   }

   protected void applyAnnotations(MixinTargetContext mixin) {
      ClassNode sourceClass = mixin.getClassNode();
      Annotations.merge(sourceClass, this.targetClass);
   }

   protected void applyFields(MixinTargetContext mixin) {
      this.mergeShadowFields(mixin);
      this.mergeNewFields(mixin);
   }

   protected void mergeShadowFields(MixinTargetContext mixin) {
      for (Entry<FieldNode, ClassInfo.Field> entry : mixin.getShadowFields()) {
         FieldNode shadow = entry.getKey();
         FieldNode target = this.findTargetField(shadow);
         if (target != null) {
            Annotations.merge(shadow, target);
            if (entry.getValue().isDecoratedMutable()) {
               target.access &= -17;
            }
         }
      }
   }

   protected void mergeNewFields(MixinTargetContext mixin) {
      for (FieldNode field : mixin.getFields()) {
         FieldNode target = this.findTargetField(field);
         if (target == null) {
            this.targetClass.fields.add(field);
            mixin.fieldMerged(field);
            if (field.signature != null) {
               if (this.mergeSignatures) {
                  SignatureVisitor sv = mixin.getSignature().getRemapper();
                  new SignatureReader(field.signature).accept(sv);
                  field.signature = sv.toString();
               } else {
                  field.signature = null;
               }
            }
         }
      }
   }

   protected void applyMethods(MixinTargetContext mixin) {
      IActivityContext.IActivity activity = this.activities.begin("?");

      for (MethodNode shadow : mixin.getShadowMethods()) {
         activity.next("@Shadow %s:%s", shadow.desc, shadow.name);
         this.applyShadowMethod(mixin, shadow);
      }

      for (MethodNode mixinMethod : mixin.getMethods()) {
         activity.next("%s:%s", mixinMethod.desc, mixinMethod.name);
         this.applyNormalMethod(mixin, mixinMethod);
      }

      activity.end();
   }

   protected void applyShadowMethod(MixinTargetContext mixin, MethodNode shadow) {
      MethodNode target = this.findTargetMethod(shadow);
      if (target != null) {
         Annotations.merge(shadow, target);
      }
   }

   protected void applyNormalMethod(MixinTargetContext mixin, MethodNode mixinMethod) {
      mixin.transformMethod(mixinMethod);
      if (!mixinMethod.name.startsWith("<")) {
         this.checkMethodVisibility(mixin, mixinMethod);
         this.checkMethodConstraints(mixin, mixinMethod);
         this.mergeMethod(mixin, mixinMethod);
      } else if ("<clinit>".equals(mixinMethod.name)) {
         IActivityContext.IActivity activity = this.activities.begin("Merge CLINIT insns");
         this.appendInsns(mixin, mixinMethod);
         activity.end();
      }
   }

   protected void mergeMethod(MixinTargetContext mixin, MethodNode method) {
      boolean isOverwrite = Annotations.getVisible(method, Overwrite.class) != null;
      MethodNode target = this.findTargetMethod(method);
      if (target != null) {
         if (this.isAlreadyMerged(mixin, method, isOverwrite, target)) {
            return;
         }

         AnnotationNode intrinsic = Annotations.getInvisible(method, Intrinsic.class);
         if (intrinsic != null) {
            if (this.mergeIntrinsic(mixin, method, isOverwrite, target, intrinsic)) {
               mixin.getTarget().methodMerged(method);
               return;
            }
         } else {
            if (mixin.requireOverwriteAnnotations() && !isOverwrite) {
               throw new InvalidMixinException(
                  mixin,
                  String.format(
                     "%s%s in %s cannot overwrite method in %s because @Overwrite is required by the parent configuration",
                     method.name,
                     method.desc,
                     mixin,
                     mixin.getTarget().getClassName()
                  )
               );
            }

            this.targetClass.methods.remove(target);
         }
      } else if (isOverwrite) {
         throw new InvalidMixinException(
            mixin, String.format("Overwrite target \"%s\" was not located in target class %s", method.name, mixin.getTargetClassRef())
         );
      }

      this.targetClass.methods.add(method);
      mixin.methodMerged(method);
      if (method.signature != null) {
         if (this.mergeSignatures) {
            SignatureVisitor sv = mixin.getSignature().getRemapper();
            new SignatureReader(method.signature).accept(sv);
            method.signature = sv.toString();
         } else {
            method.signature = null;
         }
      }
   }

   protected boolean isAlreadyMerged(MixinTargetContext mixin, MethodNode method, boolean isOverwrite, MethodNode target) {
      AnnotationNode merged = Annotations.getVisible(target, MixinMerged.class);
      if (merged == null) {
         if (Annotations.getVisible(target, Final.class) != null) {
            this.logger.warn("Overwrite prohibited for @Final method {} in {}. Skipping method.", method.name, mixin);
            return true;
         } else {
            return false;
         }
      } else {
         String sessionId = Annotations.getValue(merged, "sessionId");
         if (!this.context.getSessionId().equals(sessionId)) {
            throw new ClassFormatError("Invalid @MixinMerged annotation found in" + mixin + " at " + method.name + " in " + this.targetClass.name);
         } else if (Bytecode.hasFlag(target, 4160) && Bytecode.hasFlag(method, 4160)) {
            if (mixin.getEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
               this.logger.warn("Synthetic bridge method clash for {} in {}", method.name, mixin);
            }

            return true;
         } else {
            String owner = Annotations.getValue(merged, "mixin");
            int priority = Annotations.<Integer>getValue(merged, "priority");
            AnnotationNode accMethod = Annotations.getSingleVisible(method, Accessor.class, Invoker.class);
            if (accMethod != null) {
               AnnotationNode accTarget = Annotations.getSingleVisible(target, Accessor.class, Invoker.class);
               if (accTarget != null) {
                  String myTarget = Annotations.getValue(accMethod, "target");
                  String trTarget = Annotations.getValue(accTarget, "target");
                  if (myTarget == null) {
                     throw new MixinError("Encountered undecorated Accessor method in " + mixin + " applying to " + this.targetName);
                  }

                  if (myTarget.equals(trTarget)) {
                     return true;
                  }

                  throw new InvalidMixinException(
                     mixin,
                     String.format(
                        "Incompatible @%s %s (for %s) in %s previously written by %s (for %s)",
                        Annotations.getSimpleName(accMethod),
                        method.name,
                        myTarget,
                        mixin,
                        owner,
                        trTarget
                     )
                  );
               }
            }

            if (priority >= mixin.getPriority() && !owner.equals(mixin.getClassName())) {
               this.logger.warn("Method overwrite conflict for {} in {}, previously written by {}. Skipping method.", method.name, mixin, owner);
               return true;
            } else if (Annotations.getVisible(target, Final.class) != null) {
               this.logger.warn("Method overwrite conflict for @Final method {} in {} declared by {}. Skipping method.", method.name, mixin, owner);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   protected boolean mergeIntrinsic(MixinTargetContext mixin, MethodNode method, boolean isOverwrite, MethodNode target, AnnotationNode intrinsic) {
      if (isOverwrite) {
         throw new InvalidMixinException(
            mixin, "@Intrinsic is not compatible with @Overwrite, remove one of these annotations on " + method.name + " in " + mixin
         );
      } else {
         String methodName = method.name + method.desc;
         if (Bytecode.hasFlag(method, 8)) {
            throw new InvalidMixinException(mixin, "@Intrinsic method cannot be static, found " + methodName + " in " + mixin);
         } else {
            if (!Bytecode.hasFlag(method, 4096)) {
               AnnotationNode renamed = Annotations.getVisible(method, MixinRenamed.class);
               if (renamed == null || !Annotations.getValue(renamed, "isInterfaceMember", Boolean.FALSE)) {
                  throw new InvalidMixinException(
                     mixin, "@Intrinsic method must be prefixed interface method, no rename encountered on " + methodName + " in " + mixin
                  );
               }
            }

            if (!Annotations.getValue(intrinsic, "displace", Boolean.FALSE)) {
               this.logger.log(mixin.getLoggingLevel(), "Skipping Intrinsic mixin method {} for {}", methodName, mixin.getTargetClassRef());
               return true;
            } else {
               this.displaceIntrinsic(mixin, method, target);
               return false;
            }
         }
      }
   }

   protected void displaceIntrinsic(MixinTargetContext mixin, MethodNode method, MethodNode target) {
      String proxyName = "proxy+" + target.name;

      for (AbstractInsnNode insn : method.instructions) {
         if (insn instanceof MethodInsnNode && insn.getOpcode() != 184) {
            MethodInsnNode methodNode = (MethodInsnNode)insn;
            if (methodNode.owner.equals(this.targetClass.name) && methodNode.name.equals(target.name) && methodNode.desc.equals(target.desc)) {
               methodNode.name = proxyName;
            }
         }
      }

      target.name = proxyName;
   }

   protected final void appendInsns(MixinTargetContext mixin, MethodNode method) {
      if (Type.getReturnType(method.desc) != Type.VOID_TYPE) {
         throw new IllegalArgumentException("Attempted to merge insns from a method which does not return void");
      } else {
         MethodNode target = this.findTargetMethod(method);
         if (target == null) {
            this.targetClass.methods.add(method);
         } else {
            AbstractInsnNode returnNode = Bytecode.findInsn(target, 177);
            if (returnNode != null) {
               for (AbstractInsnNode insn : method.instructions) {
                  if (!(insn instanceof LineNumberNode) && insn.getOpcode() != 177) {
                     target.instructions.insertBefore(returnNode, insn);
                  }
               }

               target.maxLocals = Math.max(target.maxLocals, method.maxLocals);
               target.maxStack = Math.max(target.maxStack, method.maxStack);
            }
         }
      }
   }

   protected void applyInitialisers(MixinTargetContext mixin) {
      Initialiser initialiser = mixin.getInitialiser();
      if (initialiser != null && initialiser.size() != 0) {
         for (Constructor ctor : this.context.getConstructors()) {
            if (ctor.isInjectable()) {
               int extraStack = initialiser.getMaxStack() - ctor.getMaxStack();
               if (extraStack > 0) {
                  ctor.extendStack().add(extraStack);
               }

               initialiser.injectInto(ctor);
            }
         }
      }
   }

   protected void prepareInjections(MixinTargetContext mixin) {
      mixin.prepareInjections();
   }

   protected void applyPreInjections(MixinTargetContext mixin) {
      mixin.applyPreInjections();
   }

   protected void applyInjections(MixinTargetContext mixin, int injectorOrder) {
      mixin.applyInjections(injectorOrder);
   }

   protected void applyAccessors(MixinTargetContext mixin) {
      for (MethodNode method : mixin.generateAccessors()) {
         if (!method.name.startsWith("<")) {
            this.mergeMethod(mixin, method);
         }
      }
   }

   protected void checkMethodVisibility(MixinTargetContext mixin, MethodNode mixinMethod) {
      if (Bytecode.hasFlag(mixinMethod, 8)
         && !Bytecode.hasFlag(mixinMethod, 2)
         && !Bytecode.hasFlag(mixinMethod, 4096)
         && Annotations.getVisible(mixinMethod, Overwrite.class) == null) {
         throw new InvalidMixinException(mixin, String.format("Mixin %s contains non-private static method %s", mixin, mixinMethod));
      }
   }

   protected void applySourceMap(TargetClassContext context) {
      this.targetClass.sourceDebug = context.getSourceMap().toString();
   }

   protected void checkMethodConstraints(MixinTargetContext mixin, MethodNode method) {
      for (Class<? extends Annotation> annotationType : CONSTRAINED_ANNOTATIONS) {
         AnnotationNode annotation = Annotations.getVisible(method, annotationType);
         if (annotation != null) {
            this.checkConstraints(mixin, method, annotation);
         }
      }
   }

   protected final void checkConstraints(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      try {
         ConstraintParser.Constraint constraint = ConstraintParser.parse(annotation);

         try {
            constraint.check(mixin.getEnvironment());
         } catch (ConstraintViolationException var7) {
            String message = String.format("Constraint violation: %s on %s in %s", var7.getMessage(), method, mixin);
            this.logger.warn(message);
            if (!mixin.getEnvironment().getOption(MixinEnvironment.Option.IGNORE_CONSTRAINTS)) {
               throw new InvalidMixinException(mixin, message, var7);
            }
         }
      } catch (InvalidConstraintException var8) {
         throw new InvalidMixinException(mixin, var8.getMessage());
      }
   }

   protected final MethodNode findTargetMethod(MethodNode searchFor) {
      for (MethodNode target : this.targetClass.methods) {
         if (target.name.equals(searchFor.name) && target.desc.equals(searchFor.desc)) {
            return target;
         }
      }

      return null;
   }

   protected final FieldNode findTargetField(FieldNode searchFor) {
      for (FieldNode target : this.targetClass.fields) {
         if (target.name.equals(searchFor.name) && target.desc.equals(searchFor.desc)) {
            return target;
         }
      }

      return null;
   }

   static enum ApplicatorPass {
      MAIN,
      INJECT_PREPARE,
      ACCESSOR,
      INJECT_PREINJECT,
      INJECT_APPLY;
   }
}
