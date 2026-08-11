package org.spongepowered.asm.mixin.transformer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.commons.Remapper;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InnerClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.service.ISyntheticClassInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.IConsumer;
import org.spongepowered.asm.util.asm.ASM;
import org.spongepowered.include.com.google.common.collect.BiMap;
import org.spongepowered.include.com.google.common.collect.HashBiMap;

final class InnerClassGenerator implements IClassGenerator {
   private static Class<? extends ClassVisitor> clRemapper;
   private static final ILogger logger = MixinService.getService().getLogger("mixin");
   private final IConsumer<ISyntheticClassInfo> registry;
   private final Map<String, String> innerClassNames = new HashMap<>();
   private final Map<String, InnerClassGenerator.InnerClassInfo> innerClasses = new HashMap<>();
   private final MixinCoprocessorNestHost nestHostCoprocessor;

   public InnerClassGenerator(IConsumer<ISyntheticClassInfo> registry, MixinCoprocessorNestHost nestHostCoprocessor) {
      this.registry = registry;
      this.nestHostCoprocessor = nestHostCoprocessor;
   }

   @Override
   public String getName() {
      return "inner";
   }

   void registerInnerClass(MixinInfo owner, ClassInfo targetClass, String innerClassName) {
      String coordinate = innerClassCoordinate(owner, targetClass, innerClassName);
      String uniqueName = this.innerClassNames.get(coordinate);
      if (uniqueName == null) {
         uniqueName = getUniqueReference(innerClassName, targetClass);
         ClassInfo nestHost = targetClass.resolveNestHost();
         InnerClassGenerator.InnerClassInfo info = new InnerClassGenerator.InnerClassInfo(owner, targetClass, nestHost, innerClassName, uniqueName, owner);
         this.innerClassNames.put(coordinate, uniqueName);
         this.innerClasses.put(uniqueName, info);
         this.registry.accept(info);
         logger.debug("Inner class {} in {} on {} gets unique name {}", innerClassName, owner.getClassRef(), targetClass, uniqueName);
         this.nestHostCoprocessor.registerNestMember(nestHost.getClassName(), uniqueName);
      }
   }

   BiMap<String, String> getInnerClasses(MixinInfo owner, String targetName) {
      BiMap<String, String> innerClasses = HashBiMap.create();

      for (InnerClassGenerator.InnerClassInfo innerClass : this.innerClasses.values()) {
         if (innerClass.getMixin() == owner && targetName.equals(innerClass.getTargetName())) {
            innerClasses.put(innerClass.getOriginalName(), innerClass.getName());
         }
      }

      return innerClasses;
   }

   @Override
   public boolean generate(String name, ClassNode classNode) {
      String ref = name.replace('.', '/');
      InnerClassGenerator.InnerClassInfo info = this.innerClasses.get(ref);
      return info == null ? false : this.generate(info, classNode);
   }

   private boolean generate(InnerClassGenerator.InnerClassInfo info, ClassNode classNode) {
      try {
         logger.debug("Generating mapped inner class {} (originally {})", info.getName(), info.getOriginalName());
         info.accept(new InnerClassGenerator.InnerClassAdapter(createRemappingAdapter(classNode, info), info));
         return true;
      } catch (InvalidMixinException var4) {
         throw var4;
      } catch (Exception var5) {
         logger.catching(var5);
         return false;
      }
   }

   private static String getUniqueReference(String originalName, ClassInfo targetClass) {
      String name = originalName.substring(originalName.lastIndexOf(36) + 1);
      if (name.matches("^[0-9]+$")) {
         name = "Anonymous";
      }

      UUID uuid = UUID.nameUUIDFromBytes(originalName.getBytes(StandardCharsets.UTF_8));
      return String.format("%s$%s$%s", targetClass, name, uuid.toString().replace("-", ""));
   }

   private static ClassVisitor createRemappingAdapter(ClassVisitor cv, Remapper remapper) throws ReflectiveOperationException {
      if (clRemapper == null) {
         try {
            clRemapper = (Class<? extends ClassVisitor>)Class.forName("org.spongepowered.asm.lib.commons.ClassRemapper");
         } catch (ClassNotFoundException var4) {
         }

         if (clRemapper == null) {
            try {
               clRemapper = (Class<? extends ClassVisitor>)Class.forName("org.spongepowered.asm.lib.commons.RemappingClassAdapter");
            } catch (ClassNotFoundException var3) {
               throw new ClassNotFoundException("org.spongepowered.asm.lib.commons.ClassRemapper or org.objectweb.asm.commons.RemappingClassAdapter");
            }
         }
      }

      return clRemapper.getConstructor(ClassVisitor.class, Remapper.class).newInstance(cv, remapper);
   }

   private static String innerClassCoordinate(MixinInfo owner, ClassInfo targetClass, String innerClassName) {
      return String.format("%s:%s:%s", owner.getClassRef(), innerClassName, targetClass.getName());
   }

   static class InnerClassAdapter extends ClassVisitor {
      private final InnerClassGenerator.InnerClassInfo info;

      InnerClassAdapter(ClassVisitor cv, InnerClassGenerator.InnerClassInfo info) {
         super(ASM.API_VERSION, cv);
         this.info = info;
      }

      @Override
      public void visitNestHost(String nestHost) {
         this.cv.visitNestHost(this.info.getNestHostName());
      }

      @Override
      public void visitSource(String source, String debug) {
         super.visitSource(source, debug);
         AnnotationVisitor av = this.cv.visitAnnotation("Lorg/spongepowered/asm/mixin/transformer/meta/MixinInner;", false);
         av.visit("mixin", this.info.getOwner().toString());
         av.visit("name", this.info.getOriginalName().substring(this.info.getOriginalName().lastIndexOf(47) + 1));
         av.visitEnd();
      }
   }

   class InnerClassInfo extends Remapper implements ISyntheticClassInfo {
      private final IMixinInfo mixin;
      private final ClassInfo targetClassInfo;
      private final String originalName;
      private final String name;
      private final MixinInfo owner;
      private final String ownerName;
      private final String nestHostName;
      private int loadCounter;

      InnerClassInfo(IMixinInfo mixin, ClassInfo targetClass, ClassInfo nestHost, String originalName, String name, MixinInfo owner) {
         this.mixin = mixin;
         this.targetClassInfo = targetClass;
         this.originalName = originalName;
         this.name = name;
         this.owner = owner;
         this.ownerName = owner.getClassRef();
         this.nestHostName = nestHost.getName();
      }

      @Override
      public IMixinInfo getMixin() {
         return this.mixin;
      }

      @Override
      public boolean isLoaded() {
         return this.loadCounter > 0;
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public String getClassName() {
         return this.name.replace('/', '.');
      }

      String getOriginalName() {
         return this.originalName;
      }

      MixinInfo getOwner() {
         return this.owner;
      }

      String getTargetName() {
         return this.targetClassInfo.getName();
      }

      String getNestHostName() {
         return this.nestHostName;
      }

      void accept(ClassVisitor classVisitor) throws ClassNotFoundException, IOException {
         ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(this.originalName);
         this.readInnerClasses(classNode);
         classNode.accept(classVisitor);
         this.loadCounter++;
      }

      private void readInnerClasses(ClassNode classNode) {
         for (InnerClassNode inner : classNode.innerClasses) {
            if (inner.outerName != null && this.findRemappedName(inner.outerName) != null || inner.name.startsWith(this.mixin.getClassRef() + "$")) {
               InnerClassGenerator.this.registerInnerClass(this.owner, this.targetClassInfo, inner.name);
            }
         }
      }

      @Override
      public String mapFieldName(String owner, String name, String descriptor) {
         if (this.ownerName.equals(owner)) {
            ClassInfo.Field field = this.owner.getClassInfo().findField(name, descriptor, 10);
            if (field != null) {
               return field.getName();
            }
         }

         return super.mapFieldName(owner, name, descriptor);
      }

      @Override
      public String mapMethodName(String owner, String name, String desc) {
         if (this.ownerName.equals(owner)) {
            ClassInfo.Method method = this.owner.getClassInfo().findMethod(name, desc, 10);
            if (method != null) {
               return method.getName();
            }
         }

         return super.mapMethodName(owner, name, desc);
      }

      @Override
      public String map(String key) {
         String remappedName = this.findRemappedName(key);
         return remappedName != null ? remappedName : key;
      }

      @Override
      public String toString() {
         return this.name;
      }

      private String findRemappedName(String originalName) {
         return this.ownerName.equals(originalName)
            ? this.targetClassInfo.getName()
            : InnerClassGenerator.this.innerClassNames.get(InnerClassGenerator.innerClassCoordinate(this.owner, this.targetClassInfo, originalName));
      }
   }
}
