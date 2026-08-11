package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableMap<K, V> implements Map<K, V>, Serializable {
   static final Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Entry[0];
   @LazyInit
   private transient ImmutableSet<Entry<K, V>> entrySet;
   @LazyInit
   private transient ImmutableSet<K> keySet;
   @LazyInit
   private transient ImmutableCollection<V> values;
   @LazyInit
   private transient ImmutableSetMultimap<K, V> multimapView;

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableMap(keyFunction, valueFunction);
   }

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collectors.collectingAndThen(Collectors.toMap(keyFunction, valueFunction, mergeFunction, LinkedHashMap::new), ImmutableMap::copyOf);
   }

   public static <K, V> ImmutableMap<K, V> of() {
      return ImmutableBiMap.of();
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
      return ImmutableBiMap.of(k1, v1);
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
   }

   public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return RegularImmutableMap.fromEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
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

   public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
      if (map instanceof ImmutableMap && !(map instanceof ImmutableSortedMap)) {
         ImmutableMap<K, V> kvMap = (ImmutableMap<K, V>)map;
         if (!kvMap.isPartialView()) {
            return kvMap;
         }
      } else if (map instanceof EnumMap) {
         return copyOfEnumMap((EnumMap<K, ? extends V>)map);
      }

      return copyOf(map.entrySet());
   }

   @Beta
   public static <K, V> ImmutableMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      Entry<K, V>[] entryArray = Iterables.toArray((Iterable<? extends Entry<K, V>>)entries, (Entry<K, V>[])EMPTY_ENTRY_ARRAY);
      switch (entryArray.length) {
         case 0:
            return of();
         case 1:
            Entry<K, V> onlyEntry = entryArray[0];
            return of(onlyEntry.getKey(), onlyEntry.getValue());
         default:
            return RegularImmutableMap.fromEntries(entryArray);
      }
   }

   private static <K extends Enum<K>, V> ImmutableMap<K, V> copyOfEnumMap(EnumMap<K, ? extends V> original) {
      EnumMap<K, V> copy = new EnumMap<>(original);

      for (Entry<?, ?> entry : copy.entrySet()) {
         CollectPreconditions.checkEntryNotNull(entry.getKey(), entry.getValue());
      }

      return ImmutableEnumMap.asImmutable(copy);
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

   public ImmutableSetMultimap<K, V> asMultimap() {
      if (this.isEmpty()) {
         return ImmutableSetMultimap.of();
      } else {
         ImmutableSetMultimap<K, V> result = this.multimapView;
         return result == null
            ? (this.multimapView = new ImmutableSetMultimap<>(new ImmutableMap.MapViewOfValuesAsSingletonSets(), this.size(), null))
            : result;
      }
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return Maps.equalsImpl(this, object);
   }

   abstract boolean isPartialView();

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

   Object writeReplace() {
      return new ImmutableMap.SerializedForm(this);
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

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         return this.put((K)entry.getKey(), (V)entry.getValue());
      }

      @CanIgnoreReturnValue
      public ImmutableMap.Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
         return this.putAll(map.entrySet());
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableMap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         if (entries instanceof Collection) {
            this.ensureCapacity(this.size + ((Collection)entries).size());
         }

         for (Entry<? extends K, ? extends V> entry : entries) {
            this.put(entry);
         }

         return this;
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableMap.Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
         Preconditions.checkState(this.valueComparator == null, "valueComparator was already set");
         this.valueComparator = Preconditions.checkNotNull(valueComparator, "valueComparator");
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableMap.Builder<K, V> combine(ImmutableMap.Builder<K, V> other) {
         Preconditions.checkNotNull(other);
         this.ensureCapacity(this.size + other.size);
         System.arraycopy(other.entries, 0, this.entries, this.size, other.size);
         this.size = this.size + other.size;
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

   abstract static class IteratorBasedImmutableMap<K, V> extends ImmutableMap<K, V> {
      abstract UnmodifiableIterator<Entry<K, V>> entryIterator();

      Spliterator<Entry<K, V>> entrySpliterator() {
         return Spliterators.spliterator(this.entryIterator(), (long)this.size(), 1297);
      }

      @Override
      ImmutableSet<Entry<K, V>> createEntrySet() {
         class EntrySetImpl extends ImmutableMapEntrySet<K, V> {
            @Override
            ImmutableMap<K, V> map() {
               return IteratorBasedImmutableMap.this;
            }

            @Override
            public UnmodifiableIterator<Entry<K, V>> iterator() {
               return IteratorBasedImmutableMap.this.entryIterator();
            }
         }

         return new EntrySetImpl();
      }
   }

   private final class MapViewOfValuesAsSingletonSets extends ImmutableMap.IteratorBasedImmutableMap<K, ImmutableSet<V>> {
      private MapViewOfValuesAsSingletonSets() {
      }

      @Override
      public int size() {
         return ImmutableMap.this.size();
      }

      @Override
      public ImmutableSet<K> keySet() {
         return ImmutableMap.this.keySet();
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return ImmutableMap.this.containsKey(key);
      }

      public ImmutableSet<V> get(@Nullable Object key) {
         V outerValue = ImmutableMap.this.get(key);
         return outerValue == null ? null : ImmutableSet.of(outerValue);
      }

      @Override
      boolean isPartialView() {
         return ImmutableMap.this.isPartialView();
      }

      @Override
      public int hashCode() {
         return ImmutableMap.this.hashCode();
      }

      @Override
      boolean isHashCodeFast() {
         return ImmutableMap.this.isHashCodeFast();
      }

      @Override
      UnmodifiableIterator<Entry<K, ImmutableSet<V>>> entryIterator() {
         final Iterator<Entry<K, V>> backingIterator = ImmutableMap.this.entrySet().iterator();
         return new UnmodifiableIterator<Entry<K, ImmutableSet<V>>>() {
            @Override
            public boolean hasNext() {
               return backingIterator.hasNext();
            }

            public Entry<K, ImmutableSet<V>> next() {
               final Entry<K, V> backingEntry = backingIterator.next();
               return new AbstractMapEntry<K, ImmutableSet<V>>() {
                  @Override
                  public K getKey() {
                     return backingEntry.getKey();
                  }

                  public ImmutableSet<V> getValue() {
                     return ImmutableSet.of(backingEntry.getValue());
                  }
               };
            }
         };
      }
   }

   static class SerializedForm implements Serializable {
      private final Object[] keys;
      private final Object[] values;
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableMap<?, ?> map) {
         this.keys = new Object[map.size()];
         this.values = new Object[map.size()];
         int i = 0;

         for (Entry<?, ?> entry : map.entrySet()) {
            this.keys[i] = entry.getKey();
            this.values[i] = entry.getValue();
            i++;
         }
      }

      Object readResolve() {
         ImmutableMap.Builder<Object, Object> builder = new ImmutableMap.Builder<>(this.keys.length);
         return this.createMap(builder);
      }

      Object createMap(ImmutableMap.Builder<Object, Object> builder) {
         for (int i = 0; i < this.keys.length; i++) {
            builder.put(this.keys[i], this.values[i]);
         }

         return builder.build();
      }
   }
}
