package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class Array2DHashSet<T> implements Set<T> {
   protected final AbstractEqualityComparator<? super T> comparator;
   protected T[][] buckets;
   protected int n = 0;
   protected int currentPrime = 1;
   protected int threshold;
   protected final int initialCapacity;
   protected final int initialBucketCapacity;

   public Array2DHashSet() {
      this(null, 16, 8);
   }

   public Array2DHashSet(AbstractEqualityComparator<? super T> comparator, int initialCapacity, int initialBucketCapacity) {
      if (comparator == null) {
         comparator = ObjectEqualityComparator.INSTANCE;
      }

      this.comparator = comparator;
      this.initialCapacity = initialCapacity;
      this.initialBucketCapacity = initialBucketCapacity;
      this.buckets = this.createBuckets(initialCapacity);
      this.threshold = (int)Math.floor(initialCapacity * 0.75);
   }

   public final T getOrAdd(T o) {
      if (this.n > this.threshold) {
         this.expand();
      }

      return this.getOrAddImpl(o);
   }

   protected T getOrAddImpl(T o) {
      int b = this.getBucket(o);
      T[] bucket = this.buckets[b];
      if (bucket == null) {
         bucket = this.createBucket(this.initialBucketCapacity);
         bucket[0] = o;
         this.buckets[b] = bucket;
         this.n++;
         return o;
      } else {
         for (int i = 0; i < bucket.length; i++) {
            T existing = bucket[i];
            if (existing == null) {
               bucket[i] = o;
               this.n++;
               return o;
            }

            if (this.comparator.equals(existing, o)) {
               return existing;
            }
         }

         int oldLength = bucket.length;
         bucket = (T[])Arrays.copyOf(bucket, bucket.length * 2);
         this.buckets[b] = bucket;
         bucket[oldLength] = o;
         this.n++;
         return o;
      }
   }

   public T get(T o) {
      if (o == null) {
         return o;
      } else {
         int b = this.getBucket(o);
         T[] bucket = this.buckets[b];
         if (bucket == null) {
            return null;
         } else {
            for (T e : bucket) {
               if (e == null) {
                  return null;
               }

               if (this.comparator.equals(e, o)) {
                  return e;
               }
            }

            return null;
         }
      }
   }

   protected final int getBucket(T o) {
      int hash = this.comparator.hashCode(o);
      return hash & this.buckets.length - 1;
   }

   @Override
   public int hashCode() {
      int hash = MurmurHash.initialize();

      for (T[] bucket : this.buckets) {
         if (bucket != null) {
            for (T o : bucket) {
               if (o == null) {
                  break;
               }

               hash = MurmurHash.update(hash, this.comparator.hashCode(o));
            }
         }
      }

      return MurmurHash.finish(hash, this.size());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Array2DHashSet)) {
         return false;
      } else {
         Array2DHashSet<?> other = (Array2DHashSet<?>)o;
         return other.size() != this.size() ? false : this.containsAll(other);
      }
   }

   protected void expand() {
      T[][] old = this.buckets;
      this.currentPrime += 4;
      int newCapacity = this.buckets.length * 2;
      T[][] newTable = this.createBuckets(newCapacity);
      int[] newBucketLengths = new int[newTable.length];
      this.buckets = newTable;
      this.threshold = (int)(newCapacity * 0.75);
      int oldSize = this.size();

      for (T[] bucket : old) {
         if (bucket != null) {
            for (T o : bucket) {
               if (o == null) {
                  break;
               }

               int b = this.getBucket(o);
               int bucketLength = newBucketLengths[b];
               T[] newBucket;
               if (bucketLength == 0) {
                  newBucket = this.createBucket(this.initialBucketCapacity);
                  newTable[b] = newBucket;
               } else {
                  newBucket = newTable[b];
                  if (bucketLength == newBucket.length) {
                     newBucket = (T[])Arrays.copyOf(newBucket, newBucket.length * 2);
                     newTable[b] = newBucket;
                  }
               }

               newBucket[bucketLength] = o;
               newBucketLengths[b]++;
            }
         }
      }

      assert this.n == oldSize;
   }

   @Override
   public final boolean add(T t) {
      T existing = this.getOrAdd(t);
      return existing == t;
   }

   @Override
   public final int size() {
      return this.n;
   }

   @Override
   public final boolean isEmpty() {
      return this.n == 0;
   }

   @Override
   public final boolean contains(Object o) {
      return this.containsFast(this.asElementType(o));
   }

   public boolean containsFast(T obj) {
      return obj == null ? false : this.get(obj) != null;
   }

   @Override
   public Iterator<T> iterator() {
      return new Array2DHashSet.SetIterator(this.toArray());
   }

   @Override
   public T[] toArray() {
      T[] a = this.createBucket(this.size());
      int i = 0;

      for (T[] bucket : this.buckets) {
         if (bucket != null) {
            for (T o : bucket) {
               if (o == null) {
                  break;
               }

               a[i++] = o;
            }
         }
      }

      return a;
   }

   @Override
   public <U> U[] toArray(U[] a) {
      if (a.length < this.size()) {
         a = Arrays.copyOf(a, this.size());
      }

      int i = 0;

      for (T[] bucket : this.buckets) {
         if (bucket != null) {
            for (T o : bucket) {
               if (o == null) {
                  break;
               }

               a[i++] = (U)o;
            }
         }
      }

      return a;
   }

   @Override
   public final boolean remove(Object o) {
      return this.removeFast(this.asElementType(o));
   }

   public boolean removeFast(T obj) {
      if (obj == null) {
         return false;
      } else {
         int b = this.getBucket(obj);
         T[] bucket = this.buckets[b];
         if (bucket == null) {
            return false;
         } else {
            for (int i = 0; i < bucket.length; i++) {
               T e = bucket[i];
               if (e == null) {
                  return false;
               }

               if (this.comparator.equals(e, obj)) {
                  System.arraycopy(bucket, i + 1, bucket, i, bucket.length - i - 1);
                  bucket[bucket.length - 1] = null;
                  this.n--;
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      if (collection instanceof Array2DHashSet) {
         Array2DHashSet<?> s = (Array2DHashSet<?>)collection;

         for (Object[] bucket : s.buckets) {
            if (bucket != null) {
               for (Object o : bucket) {
                  if (o == null) {
                     break;
                  }

                  if (!this.containsFast(this.asElementType(o))) {
                     return false;
                  }
               }
            }
         }
      } else {
         for (Object o : collection) {
            if (!this.containsFast(this.asElementType(o))) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends T> c) {
      boolean changed = false;

      for (T o : c) {
         T existing = this.getOrAdd(o);
         if (existing != o) {
            changed = true;
         }
      }

      return changed;
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      int newsize = 0;

      for (T[] bucket : this.buckets) {
         if (bucket != null) {
            int i = 0;

            int j;
            for (j = 0; i < bucket.length && bucket[i] != null; i++) {
               if (c.contains(bucket[i])) {
                  if (i != j) {
                     bucket[j] = bucket[i];
                  }

                  j++;
                  newsize++;
               }
            }

            for (newsize += j; j < i; j++) {
               bucket[j] = null;
            }
         }
      }

      boolean changed = newsize != this.n;
      this.n = newsize;
      return changed;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      boolean changed = false;

      for (Object o : c) {
         changed |= this.removeFast(this.asElementType(o));
      }

      return changed;
   }

   @Override
   public void clear() {
      this.n = 0;
      this.buckets = this.createBuckets(this.initialCapacity);
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

         for (T[] bucket : this.buckets) {
            if (bucket != null) {
               for (T o : bucket) {
                  if (o == null) {
                     break;
                  }

                  if (first) {
                     first = false;
                  } else {
                     buf.append(", ");
                  }

                  buf.append(o.toString());
               }
            }
         }

         buf.append('}');
         return buf.toString();
      }
   }

   protected T asElementType(Object o) {
      return (T)o;
   }

   protected T[][] createBuckets(int capacity) {
      return (T[][])(new Object[capacity][]);
   }

   protected T[] createBucket(int capacity) {
      return (T[])(new Object[capacity]);
   }

   protected class SetIterator implements Iterator<T> {
      final T[] data;
      int nextIndex = 0;
      boolean removed = true;

      public SetIterator(T[] data) {
         this.data = data;
      }

      @Override
      public boolean hasNext() {
         return this.nextIndex < this.data.length;
      }

      @Override
      public T next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.removed = false;
            return this.data[this.nextIndex++];
         }
      }

      @Override
      public void remove() {
         if (this.removed) {
            throw new IllegalStateException();
         } else {
            Array2DHashSet.this.remove(this.data[this.nextIndex - 1]);
            this.removed = true;
         }
      }
   }
}
