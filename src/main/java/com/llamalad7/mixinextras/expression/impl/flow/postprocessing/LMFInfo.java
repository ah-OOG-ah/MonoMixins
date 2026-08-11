package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import org.spongepowered.asm.lib.Handle;

public class LMFInfo {
   public final Handle impl;
   public final LMFInfo.Type type;

   public LMFInfo(Handle impl, LMFInfo.Type type) {
      this.impl = impl;
      this.type = type;
   }

   public static enum Type {
      FREE_METHOD,
      BOUND_METHOD,
      INSTANTIATION;
   }
}
