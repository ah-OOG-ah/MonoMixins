package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.dfa.DFAState;
import java.util.IdentityHashMap;

public abstract class ATNSimulator {
   public static final DFAState ERROR = new DFAState(new ATNConfigSet());
   public final ATN atn;
   protected final PredictionContextCache sharedContextCache;

   public ATNSimulator(ATN atn, PredictionContextCache sharedContextCache) {
      this.atn = atn;
      this.sharedContextCache = sharedContextCache;
   }

   public abstract void reset();

   public PredictionContext getCachedContext(PredictionContext context) {
      if (this.sharedContextCache == null) {
         return context;
      } else {
         synchronized (this.sharedContextCache) {
            IdentityHashMap<PredictionContext, PredictionContext> visited = new IdentityHashMap<>();
            return PredictionContext.getCachedContext(context, this.sharedContextCache, visited);
         }
      }
   }

   static {
      ERROR.stateNumber = Integer.MAX_VALUE;
   }
}
