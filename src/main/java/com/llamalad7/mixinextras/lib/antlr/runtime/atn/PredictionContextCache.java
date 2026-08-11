package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import java.util.HashMap;
import java.util.Map;

public class PredictionContextCache {
   protected final Map<PredictionContext, PredictionContext> cache = new HashMap<>();

   public PredictionContext add(PredictionContext ctx) {
      if (ctx == EmptyPredictionContext.Instance) {
         return EmptyPredictionContext.Instance;
      } else {
         PredictionContext existing = this.cache.get(ctx);
         if (existing != null) {
            return existing;
         } else {
            this.cache.put(ctx, ctx);
            return ctx;
         }
      }
   }

   public PredictionContext get(PredictionContext ctx) {
      return this.cache.get(ctx);
   }
}
