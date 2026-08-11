package com.llamalad7.mixinextras.expression.impl.pool;

import org.spongepowered.asm.lib.tree.AbstractInsnNode;

class ArrayLengthDef implements SimpleMemberDefinition {
   @Override
   public boolean matches(AbstractInsnNode insn) {
      return insn.getOpcode() == 190;
   }
}
