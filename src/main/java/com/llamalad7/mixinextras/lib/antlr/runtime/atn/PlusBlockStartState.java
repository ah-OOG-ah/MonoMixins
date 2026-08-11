package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class PlusBlockStartState extends BlockStartState {
   public PlusLoopbackState loopBackState;

   @Override
   public int getStateType() {
      return 4;
   }
}
