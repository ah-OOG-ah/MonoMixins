package org.spongepowered.asm.mixin.gen;

import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.gen.throwables.InvalidAccessorException;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Bytecode;

public class AccessorGeneratorFieldSetter extends AccessorGeneratorField {
   private boolean mutable;

   public AccessorGeneratorFieldSetter(AccessorInfo info) {
      super(info);
   }

   @Override
   public void validate() {
      if (Bytecode.hasFlag(this.info.getClassNode(), 512)) {
         throw new InvalidAccessorException(
            this.info, String.format("%s tried to change interface field %s::%s", this.info, this.info.getClassNode().name, this.targetField.name)
         );
      } else {
         super.validate();
         ClassInfo.Method method = this.info.getClassInfo().findMethod(this.info.getMethod());
         this.mutable = method.isDecoratedMutable();
         if (!this.mutable && Bytecode.hasFlag(this.targetField, 16)) {
            if (this.info.getMixin().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
               MixinService.getService()
                  .getLogger("mixin")
                  .warn("{} for final field {}::{} is not @Mutable", this.info, ((MixinTargetContext)this.info.getMixin()).getTarget(), this.targetField.name);
            }
         }
      }
   }

   @Override
   public MethodNode generate() {
      if (this.mutable) {
         this.targetField.access &= -17;
      }

      int stackSpace = this.targetIsStatic ? 0 : 1;
      int maxLocals = stackSpace + this.targetType.getSize();
      int maxStack = stackSpace + this.targetType.getSize();
      MethodNode method = this.createMethod(maxLocals, maxStack);
      if (!this.targetIsStatic) {
         method.instructions.add(new VarInsnNode(25, 0));
      }

      method.instructions.add(new VarInsnNode(this.targetType.getOpcode(21), stackSpace));
      int opcode = this.targetIsStatic ? 179 : 181;
      method.instructions.add(new FieldInsnNode(opcode, this.info.getTargetClassNode().name, this.targetField.name, this.targetField.desc));
      method.instructions.add(new InsnNode(177));
      return method;
   }
}
