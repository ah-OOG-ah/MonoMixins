package org.spongepowered.include.com.google.common.collect;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Function;
import org.spongepowered.include.com.google.common.base.Joiner;
import org.spongepowered.include.com.google.common.base.Objects;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.j2objc.annotations.Weak;

public final class Maps {
   static final Joiner.MapJoiner STANDARD_JOINER = Collections2.STANDARD_JOINER.withKeyValueSeparator("=");

   static <K> Function<Entry<K, ?>, K> keyFunction() {
      return Maps.EntryFunction.KEY;
   }

   static <V> Function<Entry<?, V>, V> valueFunction() {
      return Maps.EntryFunction.VALUE;
   }

   static <K, V> Iterator<K> keyIterator(Iterator<Entry<K, V>> entryIterator) {
      return Iterators.transform(entryIterator, keyFunction());
   }

   static <K, V> Iterator<V> valueIterator(Iterator<Entry<K, V>> entryIterator) {
      return Iterators.transform(entryIterator, valueFunction());
   }

   static int capacity(int expectedSize) {
      if (expectedSize < 3) {
         CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
         return expectedSize + 1;
      } else {
         return expectedSize < 1073741824 ? (int)(expectedSize / 0.75F + 1.0F) : Integer.MAX_VALUE;
      }
   }

   public static <K extends Comparable, V> TreeMap<K, V> newTreeMap() {
      return new TreeMap<>();
   }

   public static <K, V> Entry<K, V> immutableEntry(@Nullable K key, @Nullable V value) {
      return new ImmutableEntry<>(key, value);
   }

   static <V> V safeGet(Map<?, V> map, @Nullable Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.get(key);
      } catch (ClassCastException var3) {
         return null;
      } catch (NullPointerException var4) {
         return null;
      }
   }

   static boolean safeContainsKey(Map<?, ?> map, Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.containsKey(key);
      } catch (ClassCastException var3) {
         return false;
      } catch (NullPointerException var4) {
         return false;
      }
   }

   static <V> V safeRemove(Map<?, V> map, Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.remove(key);
      } catch (ClassCastException var3) {
         return null;
      } catch (NullPointerException var4) {
         return null;
      }
   }

   static boolean equalsImpl(Map<?, ?> map, Object object) {
      if (map == object) {
         return true;
      } else if (object instanceof Map) {
         Map<?, ?> o = (Map<?, ?>)object;
         return map.entrySet().equals(o.entrySet());
      } else {
         return false;
      }
   }

   static String toStringImpl(Map<?, ?> map) {
      StringBuilder sb = Collections2.newStringBuilderForCollection(map.size()).append('{');
      STANDARD_JOINER.appendTo(sb, map);
      return sb.append('}').toString();
   }

   @Nullable
   static <K> K keyOrNull(@Nullable Entry<K, ?> entry) {
      return entry == null ? null : entry.getKey();
   }

   @Nullable
   static <V> V valueOrNull(@Nullable Entry<?, V> entry) {
      return entry == null ? null : entry.getValue();
   }

   private static enum EntryFunction implements Function<Entry<?, ?>, Object> {
      KEY {
         @Nullable
         public Object apply(Entry<?, ?> entry) {
            return entry.getKey();
         }
      },
      VALUE {
         @Nullable
         public Object apply(Entry<?, ?> entry) {
            return entry.getValue();
         }
      };

      private EntryFunction() {
      }
   }

   abstract static class EntrySet<K, V> extends Sets.ImprovedAbstractSet<Entry<K, V>> {
      abstract Map<K, V> map();

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public void clear() {
         this.map().clear();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            Object key = entry.getKey();
            V value = Maps.safeGet(this.map(), key);
            return Objects.equal(value, entry.getValue()) && (value != null || this.map().containsKey(key));
         }
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean remove(Object o) {
         if (this.contains(o)) {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            return this.map().keySet().remove(entry.getKey());
         } else {
            return false;
         }
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         try {
            return super.removeAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException var3) {
            return Sets.removeAllImpl(this, c.iterator());
         }
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         try {
            return super.retainAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException var7) {
            Set<Object> keys = Sets.newHashSetWithExpectedSize(c.size());

            for (Object o : c) {
               if (this.contains(o)) {
                  Entry<?, ?> entry = (Entry<?, ?>)o;
                  keys.add(entry.getKey());
               }
            }

            return this.map().keySet().retainAll(keys);
         }
      }
   }

   abstract static class IteratorBasedAbstractMap<K, V> extends AbstractMap<K, V> {
      @Override
      public abstract int size();

      abstract Iterator<Entry<K, V>> entryIterator();

      Spliterator<Entry<K, V>> entrySpliterator() {
         return Spliterators.spliterator(this.entryIterator(), (long)this.size(), 65);
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         return new Maps.EntrySet<K, V>() {
            @Override
            Map<K, V> map() {
               return IteratorBasedAbstractMap.this;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return IteratorBasedAbstractMap.this.entryIterator();
            }

            @Override
            public Spliterator<Entry<K, V>> spliterator() {
               return IteratorBasedAbstractMap.this.entrySpliterator();
            }

            @Override
            public void forEach(Consumer<? super Entry<K, V>> action) {
               IteratorBasedAbstractMap.this.forEachEntry(action);
            }
         };
      }

      void forEachEntry(Consumer<? super Entry<K, V>> action) {
         this.entryIterator().forEachRemaining(action);
      }

      @Override
      public void clear() {
         Iterators.clear(this.entryIterator());
      }
   }

   static class KeySet<K, V> extends Sets.ImprovedAbstractSet<K> {
      @Weak
      final Map<K, V> map;

      KeySet(Map<K, V> map) {
         this.map = Preconditions.checkNotNull(map);
      }

      Map<K, V> map() {
         return this.map;
      }

      @Override
      public Iterator<K> iterator() {
         return Maps.keyIterator(this.map().entrySet().iterator());
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         Preconditions.checkNotNull(action);
         this.map.forEach((k, v) -> action.accept(k));
      }

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return this.map().containsKey(o);
      }

      @Override
      public boolean remove(Object o) {
         if (this.contains(o)) {
            this.map().remove(o);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void clear() {
         this.map().clear();
      }
   }

   static class Values<K, V> extends AbstractCollection<V> {
      @Weak
      final Map<K, V> map;

      Values(Map<K, V> map) {
         this.map = Preconditions.checkNotNull(map);
      }

      final Map<K, V> map() {
         return this.map;
      }

      @Override
      public Iterator<V> iterator() {
         return Maps.valueIterator(this.map().entrySet().iterator());
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         Preconditions.checkNotNull(action);
         this.map.forEach((k, v) -> action.accept(v));
      }

      @Override
      public boolean remove(Object o) {
         try {
            return super.remove(o);
         } catch (UnsupportedOperationException var5) {
            for (Entry<K, V> entry : this.map().entrySet()) {
               if (Objects.equal(o, entry.getValue())) {
                  this.map().remove(entry.getKey());
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         try {
            return super.removeAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException var6) {
            Set<K> toRemove = Sets.newHashSet();

            for (Entry<K, V> entry : this.map().entrySet()) {
               if (c.contains(entry.getValue())) {
                  toRemove.add(entry.getKey());
               }
            }

            return this.map().keySet().removeAll(toRemove);
         }
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         try {
            return super.retainAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException var6) {
            Set<K> toRetain = Sets.newHashSet();

            for (Entry<K, V> entry : this.map().entrySet()) {
               if (c.contains(entry.getValue())) {
                  toRetain.add(entry.getKey());
               }
            }

            return this.map().keySet().retainAll(toRetain);
         }
      }

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         return this.map().containsValue(o);
      }

      @Override
      public void clear() {
         this.map().clear();
      }
   }

   abstract static class ViewCachingAbstractMap<K, V> extends AbstractMap<K, V> {
      private transient Set<Entry<K, V>> entrySet;
      private transient Set<K> keySet;
      private transient Collection<V> values;

      abstract Set<Entry<K, V>> createEntrySet();

      @Override
      public Set<Entry<K, V>> entrySet() {
         Set<Entry<K, V>> result = this.entrySet;
         return result == null ? (this.entrySet = this.createEntrySet()) : result;
      }

      @Override
      public Set<K> keySet() {
         Set<K> result = this.keySet;
         return result == null ? (this.keySet = this.createKeySet()) : result;
      }

      Set<K> createKeySet() {
         return new Maps.KeySet<>(this);
      }

      @Override
      public Collection<V> values() {
         Collection<V> result = this.values;
         return result == null ? (this.values = this.createValues()) : result;
      }

      Collection<V> createValues() {
         return new Maps.Values<>(this);
      }
   }
}
