package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.RuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.IntervalSet;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class LL1Analyzer {
   public final ATN atn;

   public LL1Analyzer(ATN atn) {
      this.atn = atn;
   }

   public IntervalSet LOOK(ATNState s, RuleContext ctx) {
      return this.LOOK(s, null, ctx);
   }

   public IntervalSet LOOK(ATNState s, ATNState stopState, RuleContext ctx) {
      IntervalSet r = new IntervalSet();
      boolean seeThruPreds = true;
      PredictionContext lookContext = ctx != null ? PredictionContext.fromRuleContext(s.atn, ctx) : null;
      this._LOOK(s, stopState, lookContext, r, new HashSet<>(), new BitSet(), seeThruPreds, true);
      return r;
   }

   protected void _LOOK(
      ATNState s,
      ATNState stopState,
      PredictionContext ctx,
      IntervalSet look,
      Set<ATNConfig> lookBusy,
      BitSet calledRuleStack,
      boolean seeThruPreds,
      boolean addEOF
   ) {
      ATNConfig c = new ATNConfig(s, 0, ctx);
      if (lookBusy.add(c)) {
         if (s == stopState) {
            if (ctx == null) {
               look.add(-2);
               return;
            }

            if (ctx.isEmpty() && addEOF) {
               look.add(-1);
               return;
            }
         }

         if (s instanceof RuleStopState) {
            if (ctx == null) {
               look.add(-2);
               return;
            }

            if (ctx.isEmpty() && addEOF) {
               look.add(-1);
               return;
            }

            if (ctx != EmptyPredictionContext.Instance) {
               boolean removed = calledRuleStack.get(s.ruleIndex);

               try {
                  calledRuleStack.clear(s.ruleIndex);

                  for (int i = 0; i < ctx.size(); i++) {
                     ATNState returnState = this.atn.states.get(ctx.getReturnState(i));
                     this._LOOK(returnState, stopState, ctx.getParent(i), look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
                  }
               } finally {
                  if (removed) {
                     calledRuleStack.set(s.ruleIndex);
                  }
               }

               return;
            }
         }

         int n = s.getNumberOfTransitions();

         for (int i = 0; i < n; i++) {
            Transition t = s.transition(i);
            if (t.getClass() == RuleTransition.class) {
               if (!calledRuleStack.get(((RuleTransition)t).target.ruleIndex)) {
                  PredictionContext newContext = SingletonPredictionContext.create(ctx, ((RuleTransition)t).followState.stateNumber);

                  try {
                     calledRuleStack.set(((RuleTransition)t).target.ruleIndex);
                     this._LOOK(t.target, stopState, newContext, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
                  } finally {
                     calledRuleStack.clear(((RuleTransition)t).target.ruleIndex);
                  }
               }
            } else if (t instanceof AbstractPredicateTransition) {
               if (seeThruPreds) {
                  this._LOOK(t.target, stopState, ctx, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
               } else {
                  look.add(0);
               }
            } else if (t.isEpsilon()) {
               this._LOOK(t.target, stopState, ctx, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
            } else if (t.getClass() == WildcardTransition.class) {
               look.addAll(IntervalSet.of(1, this.atn.maxTokenType));
            } else {
               IntervalSet set = t.label();
               if (set != null) {
                  if (t instanceof NotSetTransition) {
                     set = set.complement(IntervalSet.of(1, this.atn.maxTokenType));
                  }

                  look.addAll(set);
               }
            }
         }
      }
   }
}
