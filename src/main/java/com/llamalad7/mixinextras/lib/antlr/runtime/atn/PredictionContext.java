package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.ParserRuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.Recognizer;
import com.llamalad7.mixinextras.lib.antlr.runtime.RuleContext;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.DoubleKeyMap;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PredictionContext {
   private static final AtomicInteger globalNodeCount = new AtomicInteger();
   public final int id = globalNodeCount.getAndIncrement();
   public final int cachedHashCode;

   protected PredictionContext(int cachedHashCode) {
      this.cachedHashCode = cachedHashCode;
   }

   public static PredictionContext fromRuleContext(ATN atn, RuleContext outerContext) {
      if (outerContext == null) {
         outerContext = ParserRuleContext.EMPTY;
      }

      if (outerContext.parent != null && outerContext != ParserRuleContext.EMPTY) {
         PredictionContext parent = EmptyPredictionContext.Instance;
         parent = fromRuleContext(atn, outerContext.parent);
         ATNState state = atn.states.get(outerContext.invokingState);
         RuleTransition transition = (RuleTransition)state.transition(0);
         return SingletonPredictionContext.create(parent, transition.followState.stateNumber);
      } else {
         return EmptyPredictionContext.Instance;
      }
   }

   public abstract int size();

   public abstract PredictionContext getParent(int var1);

   public abstract int getReturnState(int var1);

   public boolean isEmpty() {
      return this == EmptyPredictionContext.Instance;
   }

   public boolean hasEmptyPath() {
      return this.getReturnState(this.size() - 1) == Integer.MAX_VALUE;
   }

   @Override
   public final int hashCode() {
      return this.cachedHashCode;
   }

   @Override
   public abstract boolean equals(Object var1);

   protected static int calculateEmptyHashCode() {
      int hash = MurmurHash.initialize(1);
      return MurmurHash.finish(hash, 0);
   }

   protected static int calculateHashCode(PredictionContext parent, int returnState) {
      int hash = MurmurHash.initialize(1);
      hash = MurmurHash.update(hash, parent);
      hash = MurmurHash.update(hash, returnState);
      return MurmurHash.finish(hash, 2);
   }

   protected static int calculateHashCode(PredictionContext[] parents, int[] returnStates) {
      int hash = MurmurHash.initialize(1);

      for (PredictionContext parent : parents) {
         hash = MurmurHash.update(hash, parent);
      }

      for (int returnState : returnStates) {
         hash = MurmurHash.update(hash, returnState);
      }

      return MurmurHash.finish(hash, 2 * parents.length);
   }

   public static PredictionContext merge(
      PredictionContext a, PredictionContext b, boolean rootIsWildcard, DoubleKeyMap<PredictionContext, PredictionContext, PredictionContext> mergeCache
   ) {
      assert a != null && b != null;

      if (a == b || a.equals(b)) {
         return a;
      } else if (a instanceof SingletonPredictionContext && b instanceof SingletonPredictionContext) {
         return mergeSingletons((SingletonPredictionContext)a, (SingletonPredictionContext)b, rootIsWildcard, mergeCache);
      } else {
         if (rootIsWildcard) {
            if (a instanceof EmptyPredictionContext) {
               return a;
            }

            if (b instanceof EmptyPredictionContext) {
               return b;
            }
         }

         if (a instanceof SingletonPredictionContext) {
            a = new ArrayPredictionContext((SingletonPredictionContext)a);
         }

         if (b instanceof SingletonPredictionContext) {
            b = new ArrayPredictionContext((SingletonPredictionContext)b);
         }

         return mergeArrays((ArrayPredictionContext)a, (ArrayPredictionContext)b, rootIsWildcard, mergeCache);
      }
   }

   public static PredictionContext mergeSingletons(
      SingletonPredictionContext a,
      SingletonPredictionContext b,
      boolean rootIsWildcard,
      DoubleKeyMap<PredictionContext, PredictionContext, PredictionContext> mergeCache
   ) {
      if (mergeCache != null) {
         PredictionContext previous = mergeCache.get(a, b);
         if (previous != null) {
            return previous;
         }

         previous = mergeCache.get(b, a);
         if (previous != null) {
            return previous;
         }
      }

      PredictionContext rootMerge = mergeRoot(a, b, rootIsWildcard);
      if (rootMerge != null) {
         if (mergeCache != null) {
            mergeCache.put(a, b, rootMerge);
         }

         return rootMerge;
      } else if (a.returnState == b.returnState) {
         PredictionContext parent = merge(a.parent, b.parent, rootIsWildcard, mergeCache);
         if (parent == a.parent) {
            return a;
         } else if (parent == b.parent) {
            return b;
         } else {
            PredictionContext a_ = SingletonPredictionContext.create(parent, a.returnState);
            if (mergeCache != null) {
               mergeCache.put(a, b, a_);
            }

            return a_;
         }
      } else {
         PredictionContext singleParent = null;
         if (a == b || a.parent != null && a.parent.equals(b.parent)) {
            singleParent = a.parent;
         }

         if (singleParent != null) {
            int[] payloads = new int[]{a.returnState, b.returnState};
            if (a.returnState > b.returnState) {
               payloads[0] = b.returnState;
               payloads[1] = a.returnState;
            }

            PredictionContext[] parents = new PredictionContext[]{singleParent, singleParent};
            PredictionContext a_ = new ArrayPredictionContext(parents, payloads);
            if (mergeCache != null) {
               mergeCache.put(a, b, a_);
            }

            return a_;
         } else {
            int[] payloadsx = new int[]{a.returnState, b.returnState};
            PredictionContext[] parents = new PredictionContext[]{a.parent, b.parent};
            if (a.returnState > b.returnState) {
               payloadsx[0] = b.returnState;
               payloadsx[1] = a.returnState;
               parents = new PredictionContext[]{b.parent, a.parent};
            }

            PredictionContext a_ = new ArrayPredictionContext(parents, payloadsx);
            if (mergeCache != null) {
               mergeCache.put(a, b, a_);
            }

            return a_;
         }
      }
   }

   public static PredictionContext mergeRoot(SingletonPredictionContext a, SingletonPredictionContext b, boolean rootIsWildcard) {
      if (rootIsWildcard) {
         if (a == EmptyPredictionContext.Instance) {
            return EmptyPredictionContext.Instance;
         }

         if (b == EmptyPredictionContext.Instance) {
            return EmptyPredictionContext.Instance;
         }
      } else {
         if (a == EmptyPredictionContext.Instance && b == EmptyPredictionContext.Instance) {
            return EmptyPredictionContext.Instance;
         }

         if (a == EmptyPredictionContext.Instance) {
            int[] payloads = new int[]{b.returnState, Integer.MAX_VALUE};
            PredictionContext[] parents = new PredictionContext[]{b.parent, null};
            PredictionContext joined = new ArrayPredictionContext(parents, payloads);
            return joined;
         }

         if (b == EmptyPredictionContext.Instance) {
            int[] payloads = new int[]{a.returnState, Integer.MAX_VALUE};
            PredictionContext[] parents = new PredictionContext[]{a.parent, null};
            PredictionContext joined = new ArrayPredictionContext(parents, payloads);
            return joined;
         }
      }

      return null;
   }

   public static PredictionContext mergeArrays(
      ArrayPredictionContext a,
      ArrayPredictionContext b,
      boolean rootIsWildcard,
      DoubleKeyMap<PredictionContext, PredictionContext, PredictionContext> mergeCache
   ) {
      if (mergeCache != null) {
         PredictionContext previous = mergeCache.get(a, b);
         if (previous != null) {
            if (ParserATNSimulator.trace_atn_sim) {
               System.out.println("mergeArrays a=" + a + ",b=" + b + " -> previous");
            }

            return previous;
         }

         previous = mergeCache.get(b, a);
         if (previous != null) {
            if (ParserATNSimulator.trace_atn_sim) {
               System.out.println("mergeArrays a=" + a + ",b=" + b + " -> previous");
            }

            return previous;
         }
      }

      int i = 0;
      int j = 0;
      int k = 0;
      int[] mergedReturnStates = new int[a.returnStates.length + b.returnStates.length];

      PredictionContext[] mergedParents;
      for (mergedParents = new PredictionContext[a.returnStates.length + b.returnStates.length]; i < a.returnStates.length && j < b.returnStates.length; k++) {
         PredictionContext a_parent = a.parents[i];
         PredictionContext b_parent = b.parents[j];
         if (a.returnStates[i] == b.returnStates[j]) {
            int payload = a.returnStates[i];
            boolean both$ = payload == Integer.MAX_VALUE && a_parent == null && b_parent == null;
            boolean ax_ax = a_parent != null && b_parent != null && a_parent.equals(b_parent);
            if (!both$ && !ax_ax) {
               PredictionContext mergedParent = merge(a_parent, b_parent, rootIsWildcard, mergeCache);
               mergedParents[k] = mergedParent;
               mergedReturnStates[k] = payload;
            } else {
               mergedParents[k] = a_parent;
               mergedReturnStates[k] = payload;
            }

            i++;
            j++;
         } else if (a.returnStates[i] < b.returnStates[j]) {
            mergedParents[k] = a_parent;
            mergedReturnStates[k] = a.returnStates[i];
            i++;
         } else {
            mergedParents[k] = b_parent;
            mergedReturnStates[k] = b.returnStates[j];
            j++;
         }
      }

      if (i < a.returnStates.length) {
         for (int p = i; p < a.returnStates.length; p++) {
            mergedParents[k] = a.parents[p];
            mergedReturnStates[k] = a.returnStates[p];
            k++;
         }
      } else {
         for (int p = j; p < b.returnStates.length; p++) {
            mergedParents[k] = b.parents[p];
            mergedReturnStates[k] = b.returnStates[p];
            k++;
         }
      }

      if (k < mergedParents.length) {
         if (k == 1) {
            PredictionContext a_ = SingletonPredictionContext.create(mergedParents[0], mergedReturnStates[0]);
            if (mergeCache != null) {
               mergeCache.put(a, b, a_);
            }

            return a_;
         }

         mergedParents = Arrays.copyOf(mergedParents, k);
         mergedReturnStates = Arrays.copyOf(mergedReturnStates, k);
      }

      PredictionContext M = new ArrayPredictionContext(mergedParents, mergedReturnStates);
      if (M.equals(a)) {
         if (mergeCache != null) {
            mergeCache.put(a, b, a);
         }

         if (ParserATNSimulator.trace_atn_sim) {
            System.out.println("mergeArrays a=" + a + ",b=" + b + " -> a");
         }

         return a;
      } else if (M.equals(b)) {
         if (mergeCache != null) {
            mergeCache.put(a, b, b);
         }

         if (ParserATNSimulator.trace_atn_sim) {
            System.out.println("mergeArrays a=" + a + ",b=" + b + " -> b");
         }

         return b;
      } else {
         combineCommonParents(mergedParents);
         if (mergeCache != null) {
            mergeCache.put(a, b, M);
         }

         if (ParserATNSimulator.trace_atn_sim) {
            System.out.println("mergeArrays a=" + a + ",b=" + b + " -> " + M);
         }

         return M;
      }
   }

   protected static void combineCommonParents(PredictionContext[] parents) {
      Map<PredictionContext, PredictionContext> uniqueParents = new HashMap<>();

      for (int p = 0; p < parents.length; p++) {
         PredictionContext parent = parents[p];
         if (!uniqueParents.containsKey(parent)) {
            uniqueParents.put(parent, parent);
         }
      }

      for (int px = 0; px < parents.length; px++) {
         parents[px] = uniqueParents.get(parents[px]);
      }
   }

   public static PredictionContext getCachedContext(
      PredictionContext context, PredictionContextCache contextCache, IdentityHashMap<PredictionContext, PredictionContext> visited
   ) {
      if (context.isEmpty()) {
         return context;
      } else {
         PredictionContext existing = visited.get(context);
         if (existing != null) {
            return existing;
         } else {
            existing = contextCache.get(context);
            if (existing != null) {
               visited.put(context, existing);
               return existing;
            } else {
               boolean changed = false;
               PredictionContext[] parents = new PredictionContext[context.size()];

               for (int i = 0; i < parents.length; i++) {
                  PredictionContext parent = getCachedContext(context.getParent(i), contextCache, visited);
                  if (changed || parent != context.getParent(i)) {
                     if (!changed) {
                        parents = new PredictionContext[context.size()];

                        for (int j = 0; j < context.size(); j++) {
                           parents[j] = context.getParent(j);
                        }

                        changed = true;
                     }

                     parents[i] = parent;
                  }
               }

               if (!changed) {
                  contextCache.add(context);
                  visited.put(context, context);
                  return context;
               } else {
                  PredictionContext updated;
                  if (parents.length == 0) {
                     updated = EmptyPredictionContext.Instance;
                  } else if (parents.length == 1) {
                     updated = SingletonPredictionContext.create(parents[0], context.getReturnState(0));
                  } else {
                     ArrayPredictionContext arrayPredictionContext = (ArrayPredictionContext)context;
                     updated = new ArrayPredictionContext(parents, arrayPredictionContext.returnStates);
                  }

                  contextCache.add(updated);
                  visited.put(updated, updated);
                  visited.put(context, updated);
                  return updated;
               }
            }
         }
      }
   }

   public String toString(Recognizer<?, ?> recog) {
      return this.toString();
   }
}
