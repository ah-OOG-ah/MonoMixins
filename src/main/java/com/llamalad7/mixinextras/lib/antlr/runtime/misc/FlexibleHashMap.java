package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlexibleHashMap<K, V> implements Map<K, V> {
   protected final AbstractEqualityComparator<? super K> comparator;
   protected LinkedList<FlexibleHashMap.Entry<K, V>>[] buckets;
   protected int n = 0;
   protected int currentPrime = 1;
   protected int threshold;
   protected final int initialCapacity;
   protected final int initialBucketCapacity;

   public FlexibleHashMap(AbstractEqualityComparator<? super K> comparator) {
      this(comparator, 16, 8);
   }

   public FlexibleHashMap(AbstractEqualityComparator<? super K> comparator, int initialCapacity, int initialBucketCapacity) {
      if (comparator == null) {
         comparator = ObjectEqualityComparator.INSTANCE;
      }

      this.comparator = comparator;
      this.initialCapacity = initialCapacity;
      this.initialBucketCapacity = initialBucketCapacity;
      this.threshold = (int)Math.floor(initialCapacity * 0.75);
      this.buckets = createEntryListArray(initialBucketCapacity);
   }

   private static <K, V> LinkedList<FlexibleHashMap.Entry<K, V>>[] createEntryListArray(int length) {
      return new LinkedList[length];
   }

   protected int getBucket(K key) {
      int hash = this.comparator.hashCode(key);
      return hash & this.buckets.length - 1;
   }

   @Override
   public V get(Object key) {
      K typedKey = (K)key;
      if (key == null) {
         return null;
      } else {
         int b = this.getBucket((K)key);
         LinkedList<FlexibleHashMap.Entry<K, V>> bucket = this.buckets[b];
         if (bucket == null) {
            return null;
         } else {
            for (FlexibleHashMap.Entry<K, V> e : bucket) {
               if (this.comparator.equals(e.key, typedKey)) {
                  return e.value;
               }
            }

            return null;
         }
      }
   }

   @Override
   public V put(K key, V value) {
      if (key == null) {
         return null;
      } else {
         if (this.n > this.threshold) {
            this.expand();
         }

         int b = this.getBucket(key);
         LinkedList<FlexibleHashMap.Entry<K, V>> bucket = this.buckets[b];
         if (bucket == null) {
            bucket = this.buckets[b] = new LinkedList<>();
         }

         for (FlexibleHashMap.Entry<K, V> e : bucket) {
            if (this.comparator.equals(e.key, key)) {
               V prev = e.value;
               e.value = value;
               this.n++;
               return prev;
            }
         }

         bucket.add(new FlexibleHashMap.Entry<>(key, value));
         this.n++;
         return null;
      }
   }

   @Override
   public V remove(Object key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<K> keySet() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Collection<V> values() {
      List<V> a = new ArrayList<>(this.size());

      for (LinkedList<FlexibleHashMap.Entry<K, V>> bucket : this.buckets) {
         if (bucket != null) {
            for (FlexibleHashMap.Entry<K, V> e : bucket) {
               a.add(e.value);
            }
         }
      }

      return a;
   }

   @Override
   public Set<java.util.Map.Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsKey(Object key) {
      return this.get(key) != null;
   }

   @Override
   public boolean containsValue(Object value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int hashCode() {
      int hash = MurmurHash.initialize();

      for (LinkedList<FlexibleHashMap.Entry<K, V>> bucket : this.buckets) {
         if (bucket != null) {
            for (FlexibleHashMap.Entry<K, V> e : bucket) {
               if (e == null) {
                  break;
               }

               hash = MurmurHash.update(hash, this.comparator.hashCode(e.key));
            }
         }
      }

      return MurmurHash.finish(hash, this.size());
   }

   @Override
   public boolean equals(Object o) {
      throw new UnsupportedOperationException();
   }

   protected void expand() {
      LinkedList<FlexibleHashMap.Entry<K, V>>[] old = this.buckets;
      this.currentPrime += 4;
      int newCapacity = this.buckets.length * 2;
      LinkedList<FlexibleHashMap.Entry<K, V>>[] newTable = createEntryListArray(newCapacity);
      this.buckets = newTable;
      this.threshold = (int)(newCapacity * 0.75);
      int oldSize = this.size();

      for (LinkedList<FlexibleHashMap.Entry<K, V>> bucket : old) {
         if (bucket != null) {
            for (FlexibleHashMap.Entry<K, V> e : bucket) {
               if (e == null) {
                  break;
               }

               this.put(e.key, e.value);
            }
         }
      }

      this.n = oldSize;
   }

   @Override
   public int size() {
      return this.n;
   }

   @Override
   public boolean isEmpty() {
      return this.n == 0;
   }

   @Override
   public void clear() {
      this.buckets = createEntryListArray(this.initialCapacity);
      this.n = 0;
      this.threshold = (int)Math.floor(this.initialCapacity * 0.75);
   }

   @Override
   public String toString() {
      if (this.size() == 0) {
         return "{}";
      } else {
         StringBuilder buf = new StringBuilder();
         buf.append('{');
         boolean first = true;

         for (LinkedList<FlexibleHashMap.Entry<K, V>> bucket : this.buckets) {
            if (bucket != null) {
               for (FlexibleHashMap.Entry<K, V> e : bucket) {
                  if (e == null) {
                     break;
                  }

                  if (first) {
                     first = false;
                  } else {
                     buf.append(", ");
                  }

                  buf.append(e.toString());
               }
            }
         }

         buf.append('}');
         return buf.toString();
      }
   }

   public static class Entry<K, V> {
      public final K key;
      public V value;

      public Entry(K key, V value) {
         this.key = key;
         this.value = value;
      }

      @Override
      public String toString() {
         return this.key.toString() + ":" + this.value.toString();
      }
   }
}
