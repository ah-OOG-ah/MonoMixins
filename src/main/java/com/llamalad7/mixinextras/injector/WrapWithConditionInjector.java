package com.llamalad7.mixinextras.injector;

import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import com.llamalad7.mixinextras.utils.InjectorUtils;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class WrapWithConditionInjector extends Injector {
   public WrapWithConditionInjector(InjectionInfo info) {
      super(info, "@WrapWithCondition");
   }

   @Override
   protected void inject(Target target, InjectionNodes.InjectionNode node) {
      this.checkTargetIsLogicallyVoid(target, node);
      this.checkTargetModifiers(target, false);
      this.wrapTargetWithCondition(target, node);
   }

   private void checkTargetIsLogicallyVoid(Target target, InjectionNodes.InjectionNode node) {
      if (!node.hasDecoration("mixinextras_operationIsImmediatelyPopped")) {
         Type returnType = this.getReturnType(node.getCurrentTarget());
         if (returnType == null) {
            throw CompatibilityHelper.makeInvalidInjectionException(
               this.info, String.format("%s annotation is targeting an invalid insn in %s in %s", this.annotationType, target, this)
            );
         } else if (returnType != Type.VOID_TYPE) {
            throw CompatibilityHelper.makeInvalidInjectionException(
               this.info, String.format("%s annotation is targeting an instruction with a non-void return type in %s in %s", this.annotationType, target, this)
            );
         }
      }
   }

   private void wrapTargetWithCondition(Target target, InjectionNodes.InjectionNode node) {
      AbstractInsnNode currentTarget = node.getCurrentTarget();
      Type returnType = this.getReturnType(currentTarget);
      Type[] originalArgTypes = this.getEffectiveArgTypes(node.getOriginalTarget());
      Type[] currentArgTypes = this.getEffectiveArgTypes(currentTarget);
      InsnList before = new InsnList();
      InsnList after = new InsnList();
      boolean isVirtualRedirect = InjectorUtils.isVirtualRedirect(node);
      this.invokeHandler(target, returnType, originalArgTypes, currentArgTypes, isVirtualRedirect, before, after);
      target.wrapNode(currentTarget, currentTarget, before, after);
   }

   private void invokeHandler(
      Target target, Type returnType, Type[] originalArgTypes, Type[] currentArgTypes, boolean isVirtualRedirect, InsnList before, InsnList after
   ) {
      Injector.InjectorData handler = new Injector.InjectorData(target, "condition wrapper");
      this.validateParams(handler, Type.BOOLEAN_TYPE, originalArgTypes);
      StackExtension stack = new StackExtension(target);
      int[] argMap = this.storeArgs(target, currentArgTypes, before, 0);
      int[] handlerArgMap = ArrayUtils.addAll(argMap, target.getArgIndices());
      if (isVirtualRedirect) {
         handlerArgMap = ArrayUtils.remove(handlerArgMap, 0);
      }

      stack.receiver(this.isStatic);
      stack.capturedArgs(target.arguments, handler.captureTargetArgs);
      stack.extra(1);
      this.invokeHandlerWithArgs(this.methodArgs, before, handlerArgMap);
      LabelNode afterOperation = new LabelNode();
      LabelNode afterDummy = new LabelNode();
      before.add(new JumpInsnNode(153, afterOperation));
      this.pushArgs(currentArgTypes, before, argMap, 0, argMap.length);
      after.add(new JumpInsnNode(167, afterDummy));
      after.add(afterOperation);
      if (returnType != Type.VOID_TYPE) {
         after.add(new InsnNode(ASMUtils.getDummyOpcodeForType(returnType)));
      }

      after.add(afterDummy);
   }

   private Type getReturnType(AbstractInsnNode node) {
      if (node instanceof MethodInsnNode) {
         MethodInsnNode methodInsnNode = (MethodInsnNode)node;
         return Type.getReturnType(methodInsnNode.desc);
      } else if (node instanceof FieldInsnNode) {
         FieldInsnNode fieldInsnNode = (FieldInsnNode)node;
         return fieldInsnNode.getOpcode() != 180 && fieldInsnNode.getOpcode() != 178 ? Type.VOID_TYPE : Type.getType(fieldInsnNode.desc);
      } else {
         return null;
      }
   }

   private Type[] getEffectiveArgTypes(AbstractInsnNode node) {
      if (node instanceof MethodInsnNode) {
         MethodInsnNode methodInsnNode = (MethodInsnNode)node;
         return node.getOpcode() == 184
            ? Type.getArgumentTypes(methodInsnNode.desc)
            : ArrayUtils.addAll(new Type[]{Type.getObjectType(methodInsnNode.owner)}, Type.getArgumentTypes(methodInsnNode.desc));
      } else {
         if (node instanceof FieldInsnNode) {
            FieldInsnNode fieldInsnNode = (FieldInsnNode)node;
            if (fieldInsnNode.getOpcode() == 181) {
               return new Type[]{Type.getObjectType(fieldInsnNode.owner), Type.getType(fieldInsnNode.desc)};
            }

            if (fieldInsnNode.getOpcode() == 179) {
               return new Type[]{Type.getType(fieldInsnNode.desc)};
            }
         }

         throw new UnsupportedOperationException();
      }
   }
}
