package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class ActionTransition extends Transition {
   public final int ruleIndex;
   public final int actionIndex;
   public final boolean isCtxDependent;

   public ActionTransition(ATNState target, int ruleIndex, int actionIndex, boolean isCtxDependent) {
      super(target);
      this.ruleIndex = ruleIndex;
      this.actionIndex = actionIndex;
      this.isCtxDependent = isCtxDependent;
   }

   @Override
   public int getSerializationType() {
      return 6;
   }

   @Override
   public boolean isEpsilon() {
      return true;
   }

   @Override
   public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
      return false;
   }

   @Override
   public String toString() {
      return "action_" + this.ruleIndex + ":" + this.actionIndex;
   }
}
