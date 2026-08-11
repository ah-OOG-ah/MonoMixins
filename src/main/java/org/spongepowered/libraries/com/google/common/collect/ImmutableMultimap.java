package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.j2objc.annotations.Weak;

@GwtCompatible(emulated = true)
public abstract class ImmutableMultimap<K, V> extends AbstractMultimap<K, V> implements Serializable {
   final transient ImmutableMap<K, ? extends ImmutableCollection<V>> map;
   final transient int size;
   private static final long serialVersionUID = 0L;

   public static <K, V> ImmutableMultimap<K, V> of() {
      return ImmutableListMultimap.of();
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1) {
      return ImmutableListMultimap.of(k1, v1);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2) {
      return ImmutableListMultimap.of(k1, v1, k2, v2);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
   }

   public static <K, V> ImmutableMultimap.Builder<K, V> builder() {
      return new ImmutableMultimap.Builder<>();
   }

   public static <K, V> ImmutableMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
      if (multimap instanceof ImmutableMultimap) {
         ImmutableMultimap<K, V> kvMultimap = (ImmutableMultimap<K, V>)multimap;
         if (!kvMultimap.isPartialView()) {
            return kvMultimap;
         }
      }

      return ImmutableListMultimap.copyOf(multimap);
   }

   @Beta
   public static <K, V> ImmutableMultimap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      return ImmutableListMultimap.copyOf(entries);
   }

   ImmutableMultimap(ImmutableMap<K, ? extends ImmutableCollection<V>> map, int size) {
      this.map = map;
      this.size = size;
   }

   @Deprecated
   @CanIgnoreReturnValue
   public ImmutableCollection<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   public ImmutableCollection<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   public abstract ImmutableCollection<V> get(K var1);

   public abstract ImmutableMultimap<V, K> inverse();

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public boolean putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
   }

   boolean isPartialView() {
      return this.map.isPartialView();
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.map.containsKey(key);
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return value != null && super.containsValue(value);
   }

   @Override
   public int size() {
      return this.size;
   }

   public ImmutableSet<K> keySet() {
      return this.map.keySet();
   }

   public ImmutableMap<K, Collection<V>> asMap() {
      return (ImmutableMap<K, Collection<V>>)this.map;
   }

   @Override
   Map<K, Collection<V>> createAsMap() {
      throw new AssertionError("should never be called");
   }

   public ImmutableCollection<Entry<K, V>> entries() {
      return (ImmutableCollection<Entry<K, V>>)super.entries();
   }

   ImmutableCollection<Entry<K, V>> createEntries() {
      return new ImmutableMultimap.EntryCollection<>(this);
   }

   UnmodifiableIterator<Entry<K, V>> entryIterator() {
      return new ImmutableMultimap<K, V>.Itr<Entry<K, V>>() {
         Entry<K, V> output(K key, V value) {
            return Maps.immutableEntry(key, value);
         }
      };
   }

   @Override
   Spliterator<Entry<K, V>> entrySpliterator() {
      return CollectSpliterators.flatMap(this.asMap().entrySet().spliterator(), keyToValueCollectionEntry -> {
         K key = keyToValueCollectionEntry.getKey();
         Collection<V> valueCollection = keyToValueCollectionEntry.getValue();
         return CollectSpliterators.map(valueCollection.spliterator(), value -> Maps.immutableEntry(key, (V)value));
      }, 64 | (this instanceof SetMultimap ? 1 : 0), this.size());
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.asMap().forEach((key, valueCollection) -> valueCollection.forEach(value -> action.accept(key, value)));
   }

   public ImmutableMultiset<K> keys() {
      return (ImmutableMultiset<K>)super.keys();
   }

   ImmutableMultiset<K> createKeys() {
      return new ImmutableMultimap.Keys();
   }

   public ImmutableCollection<V> values() {
      return (ImmutableCollection<V>)super.values();
   }

   ImmutableCollection<V> createValues() {
      return new ImmutableMultimap.Values<>(this);
   }

   UnmodifiableIterator<V> valueIterator() {
      return new ImmutableMultimap<K, V>.Itr<V>() {
         @Override
         V output(K key, V value) {
            return value;
         }
      };
   }

   public static class Builder<K, V> {
      Multimap<K, V> builderMultimap;
      Comparator<? super K> keyComparator;
      Comparator<? super V> valueComparator;

      public Builder() {
         this(MultimapBuilder.linkedHashKeys().arrayListValues().build());
      }

      Builder(Multimap<K, V> builderMultimap) {
         this.builderMultimap = builderMultimap;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> put(K key, V value) {
         CollectPreconditions.checkEntryNotNull(key, value);
         this.builderMultimap.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         return this.put((K)entry.getKey(), (V)entry.getValue());
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableMultimap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         for (Entry<? extends K, ? extends V> entry : entries) {
            this.put(entry);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(K key, Iterable<? extends V> values) {
         if (key == null) {
            throw new NullPointerException("null key in entry: null=" + Iterables.toString(values));
         } else {
            Collection<V> valueList = this.builderMultimap.get(key);

            for (V value : values) {
               CollectPreconditions.checkEntryNotNull(key, value);
               valueList.add(value);
            }

            return this;
         }
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(K key, V... values) {
         return this.putAll(key, Arrays.asList(values));
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
         for (Entry<? extends K, ? extends Collection<? extends V>> entry : multimap.asMap().entrySet()) {
            this.putAll((K)entry.getKey(), (Iterable<? extends V>)entry.getValue());
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
         this.keyComparator = Preconditions.checkNotNull(keyComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
         this.valueComparator = Preconditions.checkNotNull(valueComparator);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableMultimap.Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
         this.putAll(other.builderMultimap);
         return this;
      }

      public ImmutableMultimap<K, V> build() {
         if (this.valueComparator != null) {
            for (Collection<V> values : this.builderMultimap.asMap().values()) {
               List<V> list = (List<V>)values;
               Collections.sort(list, this.valueComparator);
            }
         }

         if (this.keyComparator != null) {
            Multimap<K, V> sortedCopy = MultimapBuilder.linkedHashKeys().arrayListValues().build();

            for (Entry<K, Collection<V>> entry : Ordering.from(this.keyComparator).onKeys().immutableSortedCopy(this.builderMultimap.asMap().entrySet())) {
               sortedCopy.putAll(entry.getKey(), entry.getValue());
            }

            this.builderMultimap = sortedCopy;
         }

         return ImmutableMultimap.copyOf(this.builderMultimap);
      }
   }

   private static class EntryCollection<K, V> extends ImmutableCollection<Entry<K, V>> {
      @Weak
      final ImmutableMultimap<K, V> multimap;
      private static final long serialVersionUID = 0L;

      EntryCollection(ImmutableMultimap<K, V> multimap) {
         this.multimap = multimap;
      }

      @Override
      public UnmodifiableIterator<Entry<K, V>> iterator() {
         return this.multimap.entryIterator();
      }

      @Override
      boolean isPartialView() {
         return this.multimap.isPartialView();
      }

      @Override
      public int size() {
         return this.multimap.size();
      }

      @Override
      public boolean contains(Object object) {
         if (object instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)object;
            return this.multimap.containsEntry(entry.getKey(), entry.getValue());
         } else {
            return false;
         }
      }
   }

   @GwtIncompatible
   static class FieldSettersHolder {
      static final Serialization.FieldSetter<ImmutableMultimap> MAP_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "map");
      static final Serialization.FieldSetter<ImmutableMultimap> SIZE_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "size");
      static final Serialization.FieldSetter<ImmutableSetMultimap> EMPTY_SET_FIELD_SETTER = Serialization.getFieldSetter(ImmutableSetMultimap.class, "emptySet");
   }

   private abstract class Itr<T> extends UnmodifiableIterator<T> {
      final Iterator<Entry<K, Collection<V>>> mapIterator = ImmutableMultimap.this.asMap().entrySet().iterator();
      K key = (K)null;
      Iterator<V> valueIterator = Iterators.emptyIterator();

      private Itr() {
      }

      abstract T output(K var1, V var2);

      @Override
      public boolean hasNext() {
         return this.mapIterator.hasNext() || this.valueIterator.hasNext();
      }

      @Override
      public T next() {
         if (!this.valueIterator.hasNext()) {
            Entry<K, Collection<V>> mapEntry = this.mapIterator.next();
            this.key = mapEntry.getKey();
            this.valueIterator = mapEntry.getValue().iterator();
         }

         return this.output((V)this.key, this.valueIterator.next());
      }
   }

   class Keys extends ImmutableMultiset<K> {
      @Override
      public boolean contains(@Nullable Object object) {
         return ImmutableMultimap.this.containsKey(object);
      }

      @Override
      public int count(@Nullable Object element) {
         Collection<V> values = (Collection<V>)ImmutableMultimap.this.map.get(element);
         return values == null ? 0 : values.size();
      }

      @Override
      public ImmutableSet<K> elementSet() {
         return ImmutableMultimap.this.keySet();
      }

      @Override
      public int size() {
         return ImmutableMultimap.this.size();
      }

      @Override
      Multiset.Entry<K> getEntry(int index) {
         Map.Entry<K, ? extends Collection<V>> entry = ImmutableMultimap.this.map.entrySet().asList().get(index);
         return Multisets.immutableEntry(entry.getKey(), entry.getValue().size());
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }

   private static final class Values<K, V> extends ImmutableCollection<V> {
      @Weak
      private final transient ImmutableMultimap<K, V> multimap;
      private static final long serialVersionUID = 0L;

      Values(ImmutableMultimap<K, V> multimap) {
         this.multimap = multimap;
      }

      @Override
      public boolean contains(@Nullable Object object) {
         return this.multimap.containsValue(object);
      }

      @Override
      public UnmodifiableIterator<V> iterator() {
         return this.multimap.valueIterator();
      }

      @GwtIncompatible
      @Override
      int copyIntoArray(Object[] dst, int offset) {
         for (ImmutableCollection<V> valueCollection : this.multimap.map.values()) {
            offset = valueCollection.copyIntoArray(dst, offset);
         }

         return offset;
      }

      @Override
      public int size() {
         return this.multimap.size();
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }
}
