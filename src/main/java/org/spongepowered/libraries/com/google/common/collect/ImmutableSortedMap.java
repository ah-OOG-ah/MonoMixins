package org.spongepowered.libraries.com.google.common.collect;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(serializable = true, emulated = true)
public final class ImmutableSortedMap<K, V> extends ImmutableSortedMapFauxverideShim<K, V> implements NavigableMap<K, V> {
   private static final Comparator<Comparable> NATURAL_ORDER = Ordering.natural();
   private static final ImmutableSortedMap<Comparable, Object> NATURAL_EMPTY_MAP = new ImmutableSortedMap<>(
      ImmutableSortedSet.emptySet(Ordering.natural()), ImmutableList.of()
   );
   private final transient RegularImmutableSortedSet<K> keySet;
   private final transient ImmutableList<V> valueList;
   private transient ImmutableSortedMap<K, V> descendingMap;
   private static final long serialVersionUID = 0L;

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
      Comparator<? super K> comparator, Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableSortedMap(comparator, keyFunction, valueFunction);
   }

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
      Comparator<? super K> comparator,
      Function<? super T, ? extends K> keyFunction,
      Function<? super T, ? extends V> valueFunction,
      BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(comparator);
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collectors.collectingAndThen(
         Collectors.toMap(keyFunction, valueFunction, mergeFunction, () -> new TreeMap<>(comparator)), ImmutableSortedMap::copyOfSorted
      );
   }

   static <K, V> ImmutableSortedMap<K, V> emptyMap(Comparator<? super K> comparator) {
      return Ordering.natural().equals(comparator) ? of() : new ImmutableSortedMap<>(ImmutableSortedSet.emptySet(comparator), ImmutableList.of());
   }

   public static <K, V> ImmutableSortedMap<K, V> of() {
      return (ImmutableSortedMap<K, V>)NATURAL_EMPTY_MAP;
   }

   public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1) {
      return of(Ordering.natural(), k1, v1);
   }

   private static <K, V> ImmutableSortedMap<K, V> of(Comparator<? super K> comparator, K k1, V v1) {
      return new ImmutableSortedMap<>(new RegularImmutableSortedSet<>(ImmutableList.of(k1), Preconditions.checkNotNull(comparator)), ImmutableList.of(v1));
   }

   private static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> ofEntries(ImmutableMapEntry<K, V>... entries) {
      return fromEntries(Ordering.natural(), false, entries, entries.length);
   }

   public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2) {
      return ofEntries(entryOf(k1, v1), entryOf(k2, v2));
   }

   public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return ofEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3));
   }

   public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return ofEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4));
   }

   public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return ofEntries(entryOf(k1, v1), entryOf(k2, v2), entryOf(k3, v3), entryOf(k4, v4), entryOf(k5, v5));
   }

   public static <K, V> ImmutableSortedMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
      Ordering<K> naturalOrder = (Ordering<K>)NATURAL_ORDER;
      return copyOfInternal(map, naturalOrder);
   }

   public static <K, V> ImmutableSortedMap<K, V> copyOf(Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
      return copyOfInternal(map, Preconditions.checkNotNull(comparator));
   }

   @Beta
   public static <K, V> ImmutableSortedMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      Ordering<K> naturalOrder = (Ordering<K>)NATURAL_ORDER;
      return copyOf(entries, naturalOrder);
   }

   @Beta
   public static <K, V> ImmutableSortedMap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries, Comparator<? super K> comparator) {
      return fromEntries(Preconditions.checkNotNull(comparator), false, entries);
   }

   public static <K, V> ImmutableSortedMap<K, V> copyOfSorted(SortedMap<K, ? extends V> map) {
      Comparator<? super K> comparator = map.comparator();
      if (comparator == null) {
         comparator = NATURAL_ORDER;
      }

      if (map instanceof ImmutableSortedMap) {
         ImmutableSortedMap<K, V> kvMap = (ImmutableSortedMap<K, V>)map;
         if (!kvMap.isPartialView()) {
            return kvMap;
         }
      }

      return fromEntries(comparator, true, map.entrySet());
   }

   private static <K, V> ImmutableSortedMap<K, V> copyOfInternal(Map<? extends K, ? extends V> map, Comparator<? super K> comparator) {
      boolean sameComparator = false;
      if (map instanceof SortedMap) {
         SortedMap<?, ?> sortedMap = (SortedMap<?, ?>)map;
         Comparator<?> comparator2 = sortedMap.comparator();
         sameComparator = comparator2 == null ? comparator == NATURAL_ORDER : comparator.equals(comparator2);
      }

      if (sameComparator && map instanceof ImmutableSortedMap) {
         ImmutableSortedMap<K, V> kvMap = (ImmutableSortedMap<K, V>)map;
         if (!kvMap.isPartialView()) {
            return kvMap;
         }
      }

      return fromEntries(comparator, sameComparator, map.entrySet());
   }

   private static <K, V> ImmutableSortedMap<K, V> fromEntries(
      Comparator<? super K> comparator, boolean sameComparator, Iterable<? extends Entry<? extends K, ? extends V>> entries
   ) {
      Entry<K, V>[] entryArray = Iterables.toArray((Iterable<? extends Entry<K, V>>)entries, (Entry<K, V>[])EMPTY_ENTRY_ARRAY);
      return fromEntries(comparator, sameComparator, entryArray, entryArray.length);
   }

   private static <K, V> ImmutableSortedMap<K, V> fromEntries(Comparator<? super K> comparator, boolean sameComparator, Entry<K, V>[] entryArray, int size) {
      switch (size) {
         case 0:
            return emptyMap(comparator);
         case 1:
            return of(comparator, entryArray[0].getKey(), entryArray[0].getValue());
         default:
            Object[] keys = new Object[size];
            Object[] values = new Object[size];
            if (sameComparator) {
               for (int i = 0; i < size; i++) {
                  Object key = entryArray[i].getKey();
                  Object value = entryArray[i].getValue();
                  CollectPreconditions.checkEntryNotNull(key, value);
                  keys[i] = key;
                  values[i] = value;
               }
            } else {
               Arrays.sort(entryArray, 0, size, Ordering.from(comparator).onKeys());
               K prevKey = entryArray[0].getKey();
               keys[0] = prevKey;
               values[0] = entryArray[0].getValue();

               for (int i = 1; i < size; i++) {
                  K key = entryArray[i].getKey();
                  V value = entryArray[i].getValue();
                  CollectPreconditions.checkEntryNotNull(key, value);
                  keys[i] = key;
                  values[i] = value;
                  checkNoConflict(comparator.compare(prevKey, key) != 0, "key", entryArray[i - 1], entryArray[i]);
                  prevKey = key;
               }
            }

            return new ImmutableSortedMap<>(new RegularImmutableSortedSet<>(new RegularImmutableList<>(keys), comparator), new RegularImmutableList<>(values));
      }
   }

   public static <K extends Comparable<?>, V> ImmutableSortedMap.Builder<K, V> naturalOrder() {
      return new ImmutableSortedMap.Builder<>(Ordering.natural());
   }

   public static <K, V> ImmutableSortedMap.Builder<K, V> orderedBy(Comparator<K> comparator) {
      return new ImmutableSortedMap.Builder<>(comparator);
   }

   public static <K extends Comparable<?>, V> ImmutableSortedMap.Builder<K, V> reverseOrder() {
      return new ImmutableSortedMap.Builder<>(Ordering.natural().reverse());
   }

   ImmutableSortedMap(RegularImmutableSortedSet<K> keySet, ImmutableList<V> valueList) {
      this(keySet, valueList, null);
   }

   ImmutableSortedMap(RegularImmutableSortedSet<K> keySet, ImmutableList<V> valueList, ImmutableSortedMap<K, V> descendingMap) {
      this.keySet = keySet;
      this.valueList = valueList;
      this.descendingMap = descendingMap;
   }

   @Override
   public int size() {
      return this.valueList.size();
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      ImmutableList<K> keyList = this.keySet.asList();

      for (int i = 0; i < this.size(); i++) {
         action.accept(keyList.get(i), this.valueList.get(i));
      }
   }

   @Override
   public V get(@Nullable Object key) {
      int index = this.keySet.indexOf(key);
      return index == -1 ? null : this.valueList.get(index);
   }

   @Override
   boolean isPartialView() {
      return this.keySet.isPartialView() || this.valueList.isPartialView();
   }

   @Override
   public ImmutableSet<Entry<K, V>> entrySet() {
      return super.entrySet();
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      class EntrySet extends ImmutableMapEntrySet<K, V> {
         @Override
         public UnmodifiableIterator<Entry<K, V>> iterator() {
            return this.asList().iterator();
         }

         @Override
         public Spliterator<Entry<K, V>> spliterator() {
            return this.asList().spliterator();
         }

         @Override
         public void forEach(Consumer<? super Entry<K, V>> action) {
            this.asList().forEach(action);
         }

         @Override
         ImmutableList<Entry<K, V>> createAsList() {
            return new ImmutableAsList<Entry<K, V>>() {
               public Entry<K, V> get(int index) {
                  return Maps.immutableEntry(ImmutableSortedMap.this.keySet.asList().get(index), ImmutableSortedMap.this.valueList.get(index));
               }

               @Override
               public Spliterator<Entry<K, V>> spliterator() {
                  return CollectSpliterators.indexed(this.size(), 1297, this::get);
               }

               @Override
               ImmutableCollection<Entry<K, V>> delegateCollection() {
                  return EntrySet.this;
               }
            };
         }

         @Override
         ImmutableMap<K, V> map() {
            return ImmutableSortedMap.this;
         }
      }

      return (ImmutableSet<Entry<K, V>>)(this.isEmpty() ? ImmutableSet.of() : new EntrySet());
   }

   public ImmutableSortedSet<K> keySet() {
      return this.keySet;
   }

   @Override
   public ImmutableCollection<V> values() {
      return this.valueList;
   }

   @Override
   public Comparator<? super K> comparator() {
      return this.keySet().comparator();
   }

   @Override
   public K firstKey() {
      return this.keySet().first();
   }

   @Override
   public K lastKey() {
      return this.keySet().last();
   }

   private ImmutableSortedMap<K, V> getSubMap(int fromIndex, int toIndex) {
      if (fromIndex == 0 && toIndex == this.size()) {
         return this;
      } else {
         return fromIndex == toIndex
            ? emptyMap(this.comparator())
            : new ImmutableSortedMap<>(this.keySet.getSubSet(fromIndex, toIndex), this.valueList.subList(fromIndex, toIndex));
      }
   }

   public ImmutableSortedMap<K, V> headMap(K toKey) {
      return this.headMap(toKey, false);
   }

   public ImmutableSortedMap<K, V> headMap(K toKey, boolean inclusive) {
      return this.getSubMap(0, this.keySet.headIndex(Preconditions.checkNotNull(toKey), inclusive));
   }

   public ImmutableSortedMap<K, V> subMap(K fromKey, K toKey) {
      return this.subMap(fromKey, true, toKey, false);
   }

   public ImmutableSortedMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      Preconditions.checkNotNull(fromKey);
      Preconditions.checkNotNull(toKey);
      Preconditions.checkArgument(this.comparator().compare(fromKey, toKey) <= 0, "expected fromKey <= toKey but %s > %s", fromKey, toKey);
      return this.headMap(toKey, toInclusive).tailMap(fromKey, fromInclusive);
   }

   public ImmutableSortedMap<K, V> tailMap(K fromKey) {
      return this.tailMap(fromKey, true);
   }

   public ImmutableSortedMap<K, V> tailMap(K fromKey, boolean inclusive) {
      return this.getSubMap(this.keySet.tailIndex(Preconditions.checkNotNull(fromKey), inclusive), this.size());
   }

   @Override
   public Entry<K, V> lowerEntry(K key) {
      return this.headMap(key, false).lastEntry();
   }

   @Override
   public K lowerKey(K key) {
      return Maps.keyOrNull(this.lowerEntry(key));
   }

   @Override
   public Entry<K, V> floorEntry(K key) {
      return this.headMap(key, true).lastEntry();
   }

   @Override
   public K floorKey(K key) {
      return Maps.keyOrNull(this.floorEntry(key));
   }

   @Override
   public Entry<K, V> ceilingEntry(K key) {
      return this.tailMap(key, true).firstEntry();
   }

   @Override
   public K ceilingKey(K key) {
      return Maps.keyOrNull(this.ceilingEntry(key));
   }

   @Override
   public Entry<K, V> higherEntry(K key) {
      return this.tailMap(key, false).firstEntry();
   }

   @Override
   public K higherKey(K key) {
      return Maps.keyOrNull(this.higherEntry(key));
   }

   @Override
   public Entry<K, V> firstEntry() {
      return this.isEmpty() ? null : this.entrySet().asList().get(0);
   }

   @Override
   public Entry<K, V> lastEntry() {
      return this.isEmpty() ? null : this.entrySet().asList().get(this.size() - 1);
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final Entry<K, V> pollFirstEntry() {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final Entry<K, V> pollLastEntry() {
      throw new UnsupportedOperationException();
   }

   public ImmutableSortedMap<K, V> descendingMap() {
      ImmutableSortedMap<K, V> result = this.descendingMap;
      if (result == null) {
         return this.isEmpty()
            ? emptyMap(Ordering.from(this.comparator()).reverse())
            : new ImmutableSortedMap<>((RegularImmutableSortedSet<K>)this.keySet.descendingSet(), this.valueList.reverse(), this);
      } else {
         return result;
      }
   }

   public ImmutableSortedSet<K> navigableKeySet() {
      return this.keySet;
   }

   public ImmutableSortedSet<K> descendingKeySet() {
      return this.keySet.descendingSet();
   }

   @Override
   Object writeReplace() {
      return new ImmutableSortedMap.SerializedForm(this);
   }

   public static class Builder<K, V> extends ImmutableMap.Builder<K, V> {
      private final Comparator<? super K> comparator;

      public Builder(Comparator<? super K> comparator) {
         this.comparator = Preconditions.checkNotNull(comparator);
      }

      @CanIgnoreReturnValue
      public ImmutableSortedMap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSortedMap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSortedMap.Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
         super.putAll(map);
         return this;
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableSortedMap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @Deprecated
      @CanIgnoreReturnValue
      @Beta
      public ImmutableSortedMap.Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
         throw new UnsupportedOperationException("Not available on ImmutableSortedMap.Builder");
      }

      ImmutableSortedMap.Builder<K, V> combine(ImmutableMap.Builder<K, V> other) {
         super.combine(other);
         return this;
      }

      public ImmutableSortedMap<K, V> build() {
         switch (this.size) {
            case 0:
               return ImmutableSortedMap.emptyMap(this.comparator);
            case 1:
               return ImmutableSortedMap.of(this.comparator, this.entries[0].getKey(), this.entries[0].getValue());
            default:
               return ImmutableSortedMap.fromEntries(this.comparator, false, this.entries, this.size);
         }
      }
   }

   private static class SerializedForm extends ImmutableMap.SerializedForm {
      private final Comparator<Object> comparator;
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableSortedMap<?, ?> sortedMap) {
         super(sortedMap);
         this.comparator = (Comparator<Object>)sortedMap.comparator();
      }

      @Override
      Object readResolve() {
         ImmutableSortedMap.Builder<Object, Object> builder = new ImmutableSortedMap.Builder<>(this.comparator);
         return this.createMap(builder);
      }
   }
}
