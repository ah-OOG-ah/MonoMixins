package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.misc.AbstractEqualityComparator;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.Array2DHashSet;
import com.llamalad7.mixinextras.lib.antlr.runtime.misc.DoubleKeyMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ATNConfigSet implements Set<ATNConfig> {
   protected boolean readonly = false;
   public ATNConfigSet.AbstractConfigHashSet configLookup;
   public final ArrayList<ATNConfig> configs = new ArrayList<>(7);
   public int uniqueAlt;
   protected BitSet conflictingAlts;
   public boolean hasSemanticContext;
   public boolean dipsIntoOuterContext;
   public final boolean fullCtx;
   private int cachedHashCode = -1;

   public ATNConfigSet(boolean fullCtx) {
      this.configLookup = new ATNConfigSet.ConfigHashSet();
      this.fullCtx = fullCtx;
   }

   public ATNConfigSet() {
      this(true);
   }

   public boolean add(ATNConfig config) {
      return this.add(config, null);
   }

   public boolean add(ATNConfig config, DoubleKeyMap<PredictionContext, PredictionContext, PredictionContext> mergeCache) {
      if (this.readonly) {
         throw new IllegalStateException("This set is readonly");
      } else {
         if (config.semanticContext != SemanticContext.Empty.Instance) {
            this.hasSemanticContext = true;
         }

         if (config.getOuterContextDepth() > 0) {
            this.dipsIntoOuterContext = true;
         }

         ATNConfig existing = this.configLookup.getOrAdd(config);
         if (existing == config) {
            this.cachedHashCode = -1;
            this.configs.add(config);
            return true;
         } else {
            boolean rootIsWildcard = !this.fullCtx;
            PredictionContext merged = PredictionContext.merge(existing.context, config.context, rootIsWildcard, mergeCache);
            existing.reachesIntoOuterContext = Math.max(existing.reachesIntoOuterContext, config.reachesIntoOuterContext);
            if (config.isPrecedenceFilterSuppressed()) {
               existing.setPrecedenceFilterSuppressed(true);
            }

            existing.context = merged;
            return true;
         }
      }
   }

   public List<ATNConfig> elements() {
      return this.configs;
   }

   public BitSet getAlts() {
      BitSet alts = new BitSet();

      for (ATNConfig config : this.configs) {
         alts.set(config.alt);
      }

      return alts;
   }

   public void optimizeConfigs(ATNSimulator interpreter) {
      if (this.readonly) {
         throw new IllegalStateException("This set is readonly");
      } else if (!this.configLookup.isEmpty()) {
         for (ATNConfig config : this.configs) {
            config.context = interpreter.getCachedContext(config.context);
         }
      }
   }

   @Override
   public boolean addAll(Collection<? extends ATNConfig> coll) {
      for (ATNConfig c : coll) {
         this.add(c);
      }

      return false;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ATNConfigSet)) {
         return false;
      } else {
         ATNConfigSet other = (ATNConfigSet)o;
         return this.configs != null
            && this.configs.equals(other.configs)
            && this.fullCtx == other.fullCtx
            && this.uniqueAlt == other.uniqueAlt
            && this.conflictingAlts == other.conflictingAlts
            && this.hasSemanticContext == other.hasSemanticContext
            && this.dipsIntoOuterContext == other.dipsIntoOuterContext;
      }
   }

   @Override
   public int hashCode() {
      if (this.isReadonly()) {
         if (this.cachedHashCode == -1) {
            this.cachedHashCode = this.configs.hashCode();
         }

         return this.cachedHashCode;
      } else {
         return this.configs.hashCode();
      }
   }

   @Override
   public int size() {
      return this.configs.size();
   }

   @Override
   public boolean isEmpty() {
      return this.configs.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      if (this.configLookup == null) {
         throw new UnsupportedOperationException("This method is not implemented for readonly sets.");
      } else {
         return this.configLookup.contains(o);
      }
   }

   @Override
   public Iterator<ATNConfig> iterator() {
      return this.configs.iterator();
   }

   @Override
   public void clear() {
      if (this.readonly) {
         throw new IllegalStateException("This set is readonly");
      } else {
         this.configs.clear();
         this.cachedHashCode = -1;
         this.configLookup.clear();
      }
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean readonly) {
      this.readonly = readonly;
      this.configLookup = null;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.elements().toString());
      if (this.hasSemanticContext) {
         buf.append(",hasSemanticContext=").append(this.hasSemanticContext);
      }

      if (this.uniqueAlt != 0) {
         buf.append(",uniqueAlt=").append(this.uniqueAlt);
      }

      if (this.conflictingAlts != null) {
         buf.append(",conflictingAlts=").append(this.conflictingAlts);
      }

      if (this.dipsIntoOuterContext) {
         buf.append(",dipsIntoOuterContext");
      }

      return buf.toString();
   }

   public ATNConfig[] toArray() {
      return this.configLookup.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a) {
      return this.configLookup.toArray(a);
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   public abstract static class AbstractConfigHashSet extends Array2DHashSet<ATNConfig> {
      public AbstractConfigHashSet(AbstractEqualityComparator<? super ATNConfig> comparator) {
         this(comparator, 16, 2);
      }

      public AbstractConfigHashSet(AbstractEqualityComparator<? super ATNConfig> comparator, int initialCapacity, int initialBucketCapacity) {
         super(comparator, initialCapacity, initialBucketCapacity);
      }

      protected final ATNConfig asElementType(Object o) {
         return !(o instanceof ATNConfig) ? null : (ATNConfig)o;
      }

      protected final ATNConfig[][] createBuckets(int capacity) {
         return new ATNConfig[capacity][];
      }

      protected final ATNConfig[] createBucket(int capacity) {
         return new ATNConfig[capacity];
      }
   }

   public static final class ConfigEqualityComparator extends AbstractEqualityComparator<ATNConfig> {
      public static final ATNConfigSet.ConfigEqualityComparator INSTANCE = new ATNConfigSet.ConfigEqualityComparator();

      private ConfigEqualityComparator() {
      }

      public int hashCode(ATNConfig o) {
         int hashCode = 7;
         hashCode = 31 * hashCode + o.state.stateNumber;
         hashCode = 31 * hashCode + o.alt;
         return 31 * hashCode + o.semanticContext.hashCode();
      }

      public boolean equals(ATNConfig a, ATNConfig b) {
         if (a == b) {
            return true;
         } else {
            return a != null && b != null ? a.state.stateNumber == b.state.stateNumber && a.alt == b.alt && a.semanticContext.equals(b.semanticContext) : false;
         }
      }
   }

   public static class ConfigHashSet extends ATNConfigSet.AbstractConfigHashSet {
      public ConfigHashSet() {
         super(ATNConfigSet.ConfigEqualityComparator.INSTANCE);
      }
   }
}
