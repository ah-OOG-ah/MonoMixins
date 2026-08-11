package com.llamalad7.mixinextras.lib.antlr.runtime;

import com.llamalad7.mixinextras.lib.antlr.runtime.atn.ATNConfigSet;
import com.llamalad7.mixinextras.lib.antlr.runtime.dfa.DFA;
import java.util.BitSet;
import java.util.Collection;

public class ProxyErrorListener implements ANTLRErrorListener {
   private final Collection<? extends ANTLRErrorListener> delegates;

   public ProxyErrorListener(Collection<? extends ANTLRErrorListener> delegates) {
      if (delegates == null) {
         throw new NullPointerException("delegates");
      } else {
         this.delegates = delegates;
      }
   }

   @Override
   public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
      for (ANTLRErrorListener listener : this.delegates) {
         listener.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
      }
   }

   @Override
   public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
      for (ANTLRErrorListener listener : this.delegates) {
         listener.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs);
      }
   }

   @Override
   public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
      for (ANTLRErrorListener listener : this.delegates) {
         listener.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs);
      }
   }

   @Override
   public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
      for (ANTLRErrorListener listener : this.delegates) {
         listener.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex, prediction, configs);
      }
   }
}
