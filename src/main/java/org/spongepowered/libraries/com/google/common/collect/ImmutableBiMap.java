package org.spongepowered.libraries.com.google.common.collect;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableBiMap<K, V> extends ImmutableBiMapFauxverideShim<K, V> implements BiMap<K, V> {
   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableBiMap(keyFunction, valueFunction);
   }

   public static <K, V> ImmutableBiMap<K, V> of() {
      return (ImmutableBiMap<K, V>)RegularImmutableBiMap.EMPTY;
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
      return new SingletonImmutableBiMap<>(k1, v1);
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
   }

   public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return RegularImmutableBiMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
   }

   public static <K, V> ImmutableBiMap.Builder<K, V> builder() {
      return new ImmutableBiMap.Builder<>();
   }

   public static <K, V> ImmutableBiMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
      if (map instanceof ImmutableBiMap) {
         ImmutableBiMap<K, V> bimap = (ImmutableBiMap<K, V>)map;
         if (!bimap.isPartialView()) {
            return bimap;
         }
      }

      return copyOf(map.entrySet());
   }

   @Beta
   public static <K, V> ImmutableBiMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      Entry<K, V>[] entryArray = Iterables.toArray((Iterable<? extends Entry<K, V>>)entries, (Entry<K, V>[])EMPTY_ENTRY_ARRAY);
      switch (entryArray.length) {
         case 0:
            return of();
         case 1:
            Entry<K, V> entry = entryArray[0];
            return of(entry.getKey(), entry.getValue());
         default:
            return RegularImmutableBiMap.fromEntries(entryArray);
      }
   }

   ImmutableBiMap() {
   }

   public abstract ImmutableBiMap<V, K> inverse();

   public ImmutableSet<V> values() {
      return this.inverse().keySet();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public V forcePut(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   Object writeReplace() {
      return new ImmutableBiMap.SerializedForm(this);
   }

   public static final class Builder<K, V> extends ImmutableMap.Builder<K, V> {
      public Builder() {
      }

      Builder(int size) {
         super(size);
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableBiMap.Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
         super.putAll(map);
         return this;
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableBiMap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableBiMap.Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
         super.orderEntriesByValue(valueComparator);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableBiMap.Builder<K, V> combine(ImmutableMap.Builder<K, V> builder) {
         super.combine(builder);
         return this;
      }

      public ImmutableBiMap<K, V> build() {
         switch (this.size) {
            case 0:
               return ImmutableBiMap.of();
            case 1:
               return ImmutableBiMap.of(this.entries[0].getKey(), this.entries[0].getValue());
            default:
               if (this.valueComparator != null) {
                  if (this.entriesUsed) {
                     this.entries = Arrays.copyOf(this.entries, this.size);
                  }

                  Arrays.sort(this.entries, 0, this.size, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
               }

               this.entriesUsed = this.size == this.entries.length;
               return RegularImmutableBiMap.fromEntryArray(this.size, this.entries);
         }
      }
   }

   private static class SerializedForm extends ImmutableMap.SerializedForm {
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableBiMap<?, ?> bimap) {
         super(bimap);
      }

      @Override
      Object readResolve() {
         ImmutableBiMap.Builder<Object, Object> builder = new ImmutableBiMap.Builder<>();
         return this.createMap(builder);
      }
   }
}
