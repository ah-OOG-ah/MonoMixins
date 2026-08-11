package io.github.tox1cozz.mixinextras.injector;

import io.github.tox1cozz.mixinextras.utils.CompatibilityHelper;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class ModifyReturnValueInjector extends Injector {
   public ModifyReturnValueInjector(InjectionInfo info) {
      super(info, "@ModifyReturnValue");
   }

   @Override
   protected void inject(Target target, InjectionNodes.InjectionNode node) {
      int opcode = node.getCurrentTarget().getOpcode();
      if (opcode >= 172 && opcode < 177) {
         this.checkTargetModifiers(target, false);
         this.injectReturnValueModifier(target, node);
      } else {
         throw CompatibilityHelper.makeInvalidInjectionException(
            this.info, String.format("%s annotation is targeting an invalid insn in %s in %s", this.annotationType, target, this)
         );
      }
   }

   private void injectReturnValueModifier(Target target, InjectionNodes.InjectionNode node) {
      Injector.InjectorData handler = new Injector.InjectorData(target, "return value modifier");
      InsnList insns = new InsnList();
      this.validateParams(handler, target.returnType, target.returnType);
      if (!this.isStatic) {
         insns.add(new VarInsnNode(25, 0));
         if (target.returnType.getSize() == 2) {
            insns.add(new InsnNode(91));
            insns.add(new InsnNode(87));
         } else {
            insns.add(new InsnNode(95));
         }
      }

      if (handler.captureTargetArgs > 0) {
         this.pushArgs(target.arguments, insns, target.getArgIndices(), 0, handler.captureTargetArgs);
      }

      this.invokeHandler(insns);
      target.insertBefore(node, insns);
   }
}
