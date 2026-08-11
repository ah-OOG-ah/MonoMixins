package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.CharStream;
import com.llamalad7.mixinextras.lib.antlr.runtime.Lexer;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;
import java.util.Arrays;

public class LexerActionExecutor {
   private final LexerAction[] lexerActions;
   private final int hashCode;

   public LexerActionExecutor(LexerAction[] lexerActions) {
      this.lexerActions = lexerActions;
      int hash = MurmurHash.initialize();

      for (LexerAction lexerAction : lexerActions) {
         hash = MurmurHash.update(hash, lexerAction);
      }

      this.hashCode = MurmurHash.finish(hash, lexerActions.length);
   }

   public static LexerActionExecutor append(LexerActionExecutor lexerActionExecutor, LexerAction lexerAction) {
      if (lexerActionExecutor == null) {
         return new LexerActionExecutor(new LexerAction[]{lexerAction});
      } else {
         LexerAction[] lexerActions = Arrays.copyOf(lexerActionExecutor.lexerActions, lexerActionExecutor.lexerActions.length + 1);
         lexerActions[lexerActions.length - 1] = lexerAction;
         return new LexerActionExecutor(lexerActions);
      }
   }

   public LexerActionExecutor fixOffsetBeforeMatch(int offset) {
      LexerAction[] updatedLexerActions = null;

      for (int i = 0; i < this.lexerActions.length; i++) {
         if (this.lexerActions[i].isPositionDependent() && !(this.lexerActions[i] instanceof LexerIndexedCustomAction)) {
            if (updatedLexerActions == null) {
               updatedLexerActions = (LexerAction[])this.lexerActions.clone();
            }

            updatedLexerActions[i] = new LexerIndexedCustomAction(offset, this.lexerActions[i]);
         }
      }

      return updatedLexerActions == null ? this : new LexerActionExecutor(updatedLexerActions);
   }

   public void execute(Lexer lexer, CharStream input, int startIndex) {
      boolean requiresSeek = false;
      int stopIndex = input.index();

      try {
         for (LexerAction lexerAction : this.lexerActions) {
            if (lexerAction instanceof LexerIndexedCustomAction) {
               int offset = ((LexerIndexedCustomAction)lexerAction).getOffset();
               input.seek(startIndex + offset);
               lexerAction = ((LexerIndexedCustomAction)lexerAction).getAction();
               requiresSeek = startIndex + offset != stopIndex;
            } else if (lexerAction.isPositionDependent()) {
               input.seek(stopIndex);
               requiresSeek = false;
            }

            lexerAction.execute(lexer);
         }
      } finally {
         if (requiresSeek) {
            input.seek(stopIndex);
         }
      }
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof LexerActionExecutor)) {
         return false;
      } else {
         LexerActionExecutor other = (LexerActionExecutor)obj;
         return this.hashCode == other.hashCode && Arrays.equals((Object[])this.lexerActions, (Object[])other.lexerActions);
      }
   }
}
