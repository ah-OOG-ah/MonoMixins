package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class PrecedencePredicateTransition extends AbstractPredicateTransition {
   public final int precedence;

   public PrecedencePredicateTransition(ATNState target, int precedence) {
      super(target);
      this.precedence = precedence;
   }

   @Override
   public int getSerializationType() {
      return 10;
   }

   @Override
   public boolean isEpsilon() {
      return true;
   }

   @Override
   public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
      return false;
   }

   public SemanticContext.PrecedencePredicate getPredicate() {
      return new SemanticContext.PrecedencePredicate(this.precedence);
   }

   @Override
   public String toString() {
      return this.precedence + " >= _p";
   }
}
