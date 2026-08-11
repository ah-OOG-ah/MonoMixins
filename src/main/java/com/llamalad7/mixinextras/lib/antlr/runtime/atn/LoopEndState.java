package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class LoopEndState extends ATNState {
   public ATNState loopBackState;

   @Override
   public int getStateType() {
      return 12;
   }
}
