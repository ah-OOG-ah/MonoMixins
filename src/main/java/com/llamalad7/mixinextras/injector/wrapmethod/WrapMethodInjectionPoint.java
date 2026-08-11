package com.llamalad7.mixinextras.injector.wrapmethod;

import java.util.Collection;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

class WrapMethodInjectionPoint extends InjectionPoint {
   @Override
   public boolean checkPriority(int targetPriority, int ownerPriority) {
      return true;
   }

   @Override
   public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
      if (insns.size() == 0) {
         throw new UnsupportedOperationException("Cannot use @WrapMethod on an abstract method!");
      } else {
         return nodes.add(insns.getFirst());
      }
   }
}
