package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.Lexer;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;

public final class LexerPushModeAction implements LexerAction {
   private final int mode;

   public LexerPushModeAction(int mode) {
      this.mode = mode;
   }

   public LexerActionType getActionType() {
      return LexerActionType.PUSH_MODE;
   }

   @Override
   public boolean isPositionDependent() {
      return false;
   }

   @Override
   public void execute(Lexer lexer) {
      lexer.pushMode(this.mode);
   }

   @Override
   public int hashCode() {
      int hash = MurmurHash.initialize();
      hash = MurmurHash.update(hash, this.getActionType().ordinal());
      hash = MurmurHash.update(hash, this.mode);
      return MurmurHash.finish(hash, 2);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return !(obj instanceof LexerPushModeAction) ? false : this.mode == ((LexerPushModeAction)obj).mode;
      }
   }

   @Override
   public String toString() {
      return String.format("pushMode(%d)", this.mode);
   }
}
