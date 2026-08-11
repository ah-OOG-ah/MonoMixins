package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class BlockEndState extends ATNState {
   public BlockStartState startState;

   @Override
   public int getStateType() {
      return 8;
   }
}
