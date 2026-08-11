package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.misc.AbstractEqualityComparator;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.FlexibleHashMap;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.MurmurHash;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public enum PredictionMode {
   SLL,
   LL,
   LL_EXACT_AMBIG_DETECTION;

   public static boolean hasSLLConflictTerminatingPrediction(PredictionMode mode, ATNConfigSet configs) {
      if (allConfigsInRuleStopStates(configs)) {
         return true;
      } else {
         if (mode == SLL && configs.hasSemanticContext) {
            ATNConfigSet dup = new ATNConfigSet();

            for (ATNConfig c : configs) {
               c = new ATNConfig(c, SemanticContext.Empty.Instance);
               dup.add(c);
            }

            configs = dup;
         }

         Collection<BitSet> altsets = getConflictingAltSubsets(configs);
         return hasConflictingAltSet(altsets) && !hasStateAssociatedWithOneAlt(configs);
      }
   }

   public static boolean hasConfigInRuleStopState(ATNConfigSet configs) {
      for (ATNConfig c : configs) {
         if (c.state instanceof RuleStopState) {
            return true;
         }
      }

      return false;
   }

   public static boolean allConfigsInRuleStopStates(ATNConfigSet configs) {
      for (ATNConfig config : configs) {
         if (!(config.state instanceof RuleStopState)) {
            return false;
         }
      }

      return true;
   }

   public static int resolvesToJustOneViableAlt(Collection<BitSet> altsets) {
      return getSingleViableAlt(altsets);
   }

   public static boolean allSubsetsConflict(Collection<BitSet> altsets) {
      return !hasNonConflictingAltSet(altsets);
   }

   public static boolean hasNonConflictingAltSet(Collection<BitSet> altsets) {
      for (BitSet alts : altsets) {
         if (alts.cardinality() == 1) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasConflictingAltSet(Collection<BitSet> altsets) {
      for (BitSet alts : altsets) {
         if (alts.cardinality() > 1) {
            return true;
         }
      }

      return false;
   }

   public static boolean allSubsetsEqual(Collection<BitSet> altsets) {
      Iterator<BitSet> it = altsets.iterator();
      BitSet first = it.next();

      while (it.hasNext()) {
         BitSet next = it.next();
         if (!next.equals(first)) {
            return false;
         }
      }

      return true;
   }

   public static int getUniqueAlt(Collection<BitSet> altsets) {
      BitSet all = getAlts(altsets);
      return all.cardinality() == 1 ? all.nextSetBit(0) : 0;
   }

   public static BitSet getAlts(Collection<BitSet> altsets) {
      BitSet all = new BitSet();

      for (BitSet alts : altsets) {
         all.or(alts);
      }

      return all;
   }

   public static Collection<BitSet> getConflictingAltSubsets(ATNConfigSet configs) {
      PredictionMode.AltAndContextMap configToAlts = new PredictionMode.AltAndContextMap();

      for (ATNConfig c : configs) {
         BitSet alts = configToAlts.get(c);
         if (alts == null) {
            alts = new BitSet();
            configToAlts.put(c, alts);
         }

         alts.set(c.alt);
      }

      return configToAlts.values();
   }

   public static Map<ATNState, BitSet> getStateToAltMap(ATNConfigSet configs) {
      Map<ATNState, BitSet> m = new HashMap<>();

      for (ATNConfig c : configs) {
         BitSet alts = m.get(c.state);
         if (alts == null) {
            alts = new BitSet();
            m.put(c.state, alts);
         }

         alts.set(c.alt);
      }

      return m;
   }

   public static boolean hasStateAssociatedWithOneAlt(ATNConfigSet configs) {
      Map<ATNState, BitSet> x = getStateToAltMap(configs);

      for (BitSet alts : x.values()) {
         if (alts.cardinality() == 1) {
            return true;
         }
      }

      return false;
   }

   public static int getSingleViableAlt(Collection<BitSet> altsets) {
      BitSet viableAlts = new BitSet();

      for (BitSet alts : altsets) {
         int minAlt = alts.nextSetBit(0);
         viableAlts.set(minAlt);
         if (viableAlts.cardinality() > 1) {
            return 0;
         }
      }

      return viableAlts.nextSetBit(0);
   }

   private static final class AltAndContextConfigEqualityComparator extends AbstractEqualityComparator<ATNConfig> {
      public static final PredictionMode.AltAndContextConfigEqualityComparator INSTANCE = new PredictionMode.AltAndContextConfigEqualityComparator();

      public int hashCode(ATNConfig o) {
         int hashCode = MurmurHash.initialize(7);
         hashCode = MurmurHash.update(hashCode, o.state.stateNumber);
         hashCode = MurmurHash.update(hashCode, o.context);
         return MurmurHash.finish(hashCode, 2);
      }

      public boolean equals(ATNConfig a, ATNConfig b) {
         if (a == b) {
            return true;
         } else {
            return a != null && b != null ? a.state.stateNumber == b.state.stateNumber && a.context.equals(b.context) : false;
         }
      }
   }

   static class AltAndContextMap extends FlexibleHashMap<ATNConfig, BitSet> {
      public AltAndContextMap() {
         super(PredictionMode.AltAndContextConfigEqualityComparator.INSTANCE);
      }
   }
}
