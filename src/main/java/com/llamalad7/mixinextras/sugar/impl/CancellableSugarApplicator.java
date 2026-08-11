package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.injector.StackExtension;
import com.llamalad7.mixinextras.utils.ASMUtils;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

class CancellableSugarApplicator extends SugarApplicator {
   CancellableSugarApplicator(InjectionInfo info, SugarParameter parameter) {
      super(info, parameter);
   }

   @Override
   void validate(Target target, InjectionNodes.InjectionNode node) {
   }

   @Override
   void prepare(Target target, InjectionNodes.InjectionNode node) {
   }

   @Override
   void inject(Target target, InjectionNodes.InjectionNode node, StackExtension stack) {
      Type ciType = Type.getObjectType(target.getCallbackInfoClass());
      if (!ciType.equals(this.paramType)) {
         throw new IllegalStateException(
            String.format("@Cancellable sugar has wrong type! Expected %s but got %s!", ciType.getClassName(), this.paramType.getClassName())
         );
      } else {
         int ciIndex = this.getOrCreateCi(target, node, stack, ciType);
         stack.extra(1);
         target.insns.insertBefore(node.getCurrentTarget(), new VarInsnNode(25, ciIndex));
      }
   }

   @Override
   int postProcessingPriority() {
      return -1000;
   }

   private int getOrCreateCi(Target target, InjectionNodes.InjectionNode node, StackExtension stack, Type ciType) {
      if (node.hasDecoration("mixinextras_cancellableCiIndex")) {
         return node.<Integer>getDecoration("mixinextras_cancellableCiIndex");
      } else {
         int ciIndex = target.allocateLocal();
         target.addLocalVariable(ciIndex, "callbackInfo" + ciIndex, ciType.getDescriptor());
         node.decorate("mixinextras_cancellableCiIndex", ciIndex);
         InsnList init = new InsnList();
         init.add(new TypeInsnNode(187, ciType.getInternalName()));
         init.add(new InsnNode(89));
         init.add(new LdcInsnNode(target.method.name));
         init.add(new InsnNode(4));
         init.add(new MethodInsnNode(183, ciType.getInternalName(), "<init>", "(Ljava/lang/String;Z)V", false));
         init.add(new VarInsnNode(58, ciIndex));
         target.insertBefore(node, init);
         stack.extra(4);
         SugarPostProcessingExtension.enqueuePostProcessing(
            this,
            () -> {
               InsnList cancellation = new InsnList();
               LabelNode notCancelled = new LabelNode();
               cancellation.add(new VarInsnNode(25, ciIndex));
               cancellation.add(new MethodInsnNode(182, ciType.getInternalName(), "isCancelled", "()Z", false));
               cancellation.add(new JumpInsnNode(153, notCancelled));
               cancellation.add(new VarInsnNode(25, ciIndex));
               if (target.returnType.equals(Type.VOID_TYPE)) {
                  cancellation.add(new InsnNode(177));
               } else if (ASMUtils.isPrimitive(target.returnType)) {
                  cancellation.add(
                     new MethodInsnNode(
                        182, ciType.getInternalName(), "getReturnValue" + target.returnType.getDescriptor(), "()" + target.returnType.getDescriptor(), false
                     )
                  );
                  cancellation.add(new InsnNode(target.returnType.getOpcode(172)));
               } else {
                  cancellation.add(new MethodInsnNode(182, ciType.getInternalName(), "getReturnValue", "()Ljava/lang/Object;", false));
                  cancellation.add(new TypeInsnNode(192, target.returnType.getInternalName()));
                  cancellation.add(new InsnNode(176));
               }

               cancellation.add(notCancelled);
               target.insns.insert(node.getCurrentTarget(), cancellation);
            }
         );
         return ciIndex;
      }
   }
}
