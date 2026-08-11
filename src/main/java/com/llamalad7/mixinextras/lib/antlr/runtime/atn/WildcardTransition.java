package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public final class WildcardTransition extends Transition {
   public WildcardTransition(ATNState target) {
      super(target);
   }

   @Override
   public int getSerializationType() {
      return 9;
   }

   @Override
   public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
      return symbol >= minVocabSymbol && symbol <= maxVocabSymbol;
   }

   @Override
   public String toString() {
      return ".";
   }
}
