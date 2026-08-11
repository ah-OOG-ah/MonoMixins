package com.llamalad7.mixinextras.versions;

import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;

public class MixinVersionImpl_v0_8_4 extends MixinVersionImpl_v0_8_3 {
   @Override
   public RuntimeException makeInvalidInjectionException(InjectionInfo info, String message) {
      return new InvalidInjectionException(info, message);
   }

   @Override
   public LocalVariableDiscriminator.Context makeLvtContext(InjectionInfo info, Type returnType, boolean argsOnly, Target target, AbstractInsnNode node) {
      return new LocalVariableDiscriminator.Context(info, returnType, argsOnly, target, node);
   }
}
