package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class RuleStartState extends ATNState {
   public RuleStopState stopState;
   public boolean isLeftRecursiveRule;

   @Override
   public int getStateType() {
      return 2;
   }
}
