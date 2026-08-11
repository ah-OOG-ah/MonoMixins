package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.Lexer;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;

public final class LexerMoreAction implements LexerAction {
   public static final LexerMoreAction INSTANCE = new LexerMoreAction();

   private LexerMoreAction() {
   }

   public LexerActionType getActionType() {
      return LexerActionType.MORE;
   }

   @Override
   public boolean isPositionDependent() {
      return false;
   }

   @Override
   public void execute(Lexer lexer) {
      lexer.more();
   }

   @Override
   public int hashCode() {
      int hash = MurmurHash.initialize();
      hash = MurmurHash.update(hash, this.getActionType().ordinal());
      return MurmurHash.finish(hash, 1);
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this;
   }

   @Override
   public String toString() {
      return "more";
   }
}
