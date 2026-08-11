package org.spongepowered.include.com.google.common.collect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.include.com.google.errorprone.annotations.concurrent.LazyInit;

public abstract class ImmutableMap<K, V> implements Serializable, Map<K, V> {
   static final Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Entry[0];
   @LazyInit
   private transient ImmutableSet<Entry<K, V>> entrySet;
   @LazyInit
   private transient ImmutableSet<K> keySet;
   @LazyInit
   private transient ImmutableCollection<V> values;

   public static <K, V> ImmutableMap<K, V> of() {
      return ImmutableBiMap.of();
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
      return ImmutableBiMap.of(k1, v1);
   }

   static <K, V> ImmutableMapEntry<K, V> entryOf(K key, V value) {
      return new ImmutableMapEntry<>(key, value);
   }

   public static <K, V> ImmutableMap.Builder<K, V> builder() {
      return new ImmutableMap.Builder<>();
   }

   static void checkNoConflict(boolean safe, String conflictDescription, Entry<?, ?> entry1, Entry<?, ?> entry2) {
      if (!safe) {
         throw new IllegalArgumentException("Multiple entries with same " + conflictDescription + ": " + entry1 + " and " + entry2);
      }
   }

   ImmutableMap() {
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final V put(K k, V v) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final V putIfAbsent(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final boolean replace(K key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V replace(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void putAll(Map<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final V remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.get(key) != null;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.values().contains(value);
   }

   @Override
   public abstract V get(@Nullable Object var1);

   @Override
   public final V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
      V result = this.get(key);
      return result != null ? result : defaultValue;
   }

   public ImmutableSet<Entry<K, V>> entrySet() {
      ImmutableSet<Entry<K, V>> result = this.entrySet;
      return result == null ? (this.entrySet = this.createEntrySet()) : result;
   }

   abstract ImmutableSet<Entry<K, V>> createEntrySet();

   public ImmutableSet<K> keySet() {
      ImmutableSet<K> result = this.keySet;
      return result == null ? (this.keySet = this.createKeySet()) : result;
   }

   ImmutableSet<K> createKeySet() {
      return (ImmutableSet<K>)(this.isEmpty() ? ImmutableSet.of() : new ImmutableMapKeySet<>(this));
   }

   UnmodifiableIterator<K> keyIterator() {
      final UnmodifiableIterator<Entry<K, V>> entryIterator = this.entrySet().iterator();
      return new UnmodifiableIterator<K>() {
         @Override
         public boolean hasNext() {
            return entryIterator.hasNext();
         }

         @Override
         public K next() {
            return entryIterator.next().getKey();
         }
      };
   }

   Spliterator<K> keySpliterator() {
      return CollectSpliterators.map(this.entrySet().spliterator(), Entry::getKey);
   }

   public ImmutableCollection<V> values() {
      ImmutableCollection<V> result = this.values;
      return result == null ? (this.values = this.createValues()) : result;
   }

   ImmutableCollection<V> createValues() {
      return new ImmutableMapValues<>(this);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return Maps.equalsImpl(this, object);
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this.entrySet());
   }

   boolean isHashCodeFast() {
      return false;
   }

   @Override
   public String toString() {
      return Maps.toStringImpl(this);
   }

   public static class Builder<K, V> {
      Comparator<? super V> valueComparator;
      ImmutableMapEntry<K, V>[] entries;
      int size;
      boolean entriesUsed;

      public Builder() {
         this(4);
      }

      Builder(int initialCapacity) {
         this.entries = new ImmutableMapEntry[initialCapacity];
         this.size = 0;
         this.entriesUsed = false;
      }

      private void ensureCapacity(int minCapacity) {
         if (minCapacity > this.entries.length) {
            this.entries = Arrays.copyOf(this.entries, ImmutableCollection.Builder.expandedCapacity(this.entries.length, minCapacity));
            this.entriesUsed = false;
         }
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> put(K key, V value) {
         this.ensureCapacity(this.size + 1);
         ImmutableMapEntry<K, V> entry = ImmutableMap.entryOf(key, value);
         this.entries[this.size++] = entry;
         return this;
      }

      public ImmutableMap<K, V> build() {
         switch (this.size) {
            case 0:
               return ImmutableMap.of();
            case 1:
               return ImmutableMap.of(this.entries[0].getKey(), this.entries[0].getValue());
            default:
               if (this.valueComparator != null) {
                  if (this.entriesUsed) {
                     this.entries = Arrays.copyOf(this.entries, this.size);
                  }

                  Arrays.sort(this.entries, 0, this.size, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
               }

               this.entriesUsed = this.size == this.entries.length;
               return RegularImmutableMap.fromEntryArray(this.size, this.entries);
         }
      }
   }
}
