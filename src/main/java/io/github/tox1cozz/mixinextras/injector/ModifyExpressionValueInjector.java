package io.github.tox1cozz.mixinextras.injector;

import io.github.tox1cozz.mixinextras.utils.CompatibilityHelper;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.util.Bytecode;

public class ModifyExpressionValueInjector extends Injector {
   public ModifyExpressionValueInjector(InjectionInfo info) {
      super(info, "@ModifyExpressionValue");
   }

   @Override
   protected void inject(Target target, InjectionNodes.InjectionNode node) {
      this.checkTargetReturnsAValue(target, node);
      this.checkTargetModifiers(target, false);
      AbstractInsnNode valueNode = node.getCurrentTarget();
      Type valueType = this.getReturnType(valueNode);
      if (valueNode instanceof TypeInsnNode && valueNode.getOpcode() == 187) {
         valueNode = target.findInitNodeFor((TypeInsnNode)valueNode);
      }

      this.injectValueModifier(target, valueNode, valueType);
   }

   private void checkTargetReturnsAValue(Target target, InjectionNodes.InjectionNode node) {
      Type returnType = this.getReturnType(node.getCurrentTarget());
      if (returnType == Type.VOID_TYPE) {
         throw CompatibilityHelper.makeInvalidInjectionException(
            this.info, String.format("%s annotation is targeting an instruction with a return type of 'void' in %s in %s", this.annotationType, target, this)
         );
      } else if (returnType == null) {
         throw CompatibilityHelper.makeInvalidInjectionException(
            this.info, String.format("%s annotation is targeting an invalid insn in %s in %s", this.annotationType, target, this)
         );
      }
   }

   private void injectValueModifier(Target target, AbstractInsnNode valueNode, Type valueType) {
      Target.Extension extraStack = target.extendStack();
      InsnList after = new InsnList();
      this.invokeHandler(valueType, target, extraStack, after);
      extraStack.apply();
      target.insns.insert(valueNode, after);
   }

   private void invokeHandler(Type valueType, Target target, Target.Extension extraStack, InsnList after) {
      Injector.InjectorData handler = new Injector.InjectorData(target, "expression value modifier");
      this.validateParams(handler, valueType, valueType);
      if (!this.isStatic) {
         after.add(new VarInsnNode(25, 0));
         if (valueType.getSize() == 2) {
            after.add(new InsnNode(91));
            after.add(new InsnNode(87));
         } else {
            after.add(new InsnNode(95));
         }
      }

      if (handler.captureTargetArgs > 0) {
         this.pushArgs(target.arguments, after, target.getArgIndices(), 0, handler.captureTargetArgs, extraStack);
      }

      this.invokeHandler(after);
   }

   private Type getReturnType(AbstractInsnNode node) {
      if (node instanceof MethodInsnNode) {
         MethodInsnNode methodInsnNode = (MethodInsnNode)node;
         return Type.getReturnType(methodInsnNode.desc);
      } else if (node instanceof FieldInsnNode) {
         FieldInsnNode fieldInsnNode = (FieldInsnNode)node;
         return fieldInsnNode.getOpcode() != 180 && fieldInsnNode.getOpcode() != 178 ? Type.VOID_TYPE : Type.getType(fieldInsnNode.desc);
      } else if (Bytecode.isConstant(node)) {
         return Bytecode.getConstantType(node);
      } else if (node instanceof TypeInsnNode && node.getOpcode() == 187) {
         TypeInsnNode typeInsnNode = (TypeInsnNode)node;
         return Type.getObjectType(typeInsnNode.desc);
      } else {
         return null;
      }
   }
}
