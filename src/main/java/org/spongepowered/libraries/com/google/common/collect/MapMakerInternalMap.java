package org.spongepowered.libraries.com.google.common.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Equivalence;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.primitives.Ints;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.j2objc.annotations.Weak;

@GwtIncompatible
class MapMakerInternalMap<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>>
   extends AbstractMap<K, V>
   implements ConcurrentMap<K, V>,
   Serializable {
   static final int MAXIMUM_CAPACITY = 1073741824;
   static final int MAX_SEGMENTS = 65536;
   static final int CONTAINS_VALUE_RETRIES = 3;
   static final int DRAIN_THRESHOLD = 63;
   static final int DRAIN_MAX = 16;
   static final long CLEANUP_EXECUTOR_DELAY_SECS = 60L;
   final transient int segmentMask;
   final transient int segmentShift;
   final transient MapMakerInternalMap.Segment<K, V, E, S>[] segments;
   final int concurrencyLevel;
   final Equivalence<Object> keyEquivalence;
   final transient MapMakerInternalMap.InternalEntryHelper<K, V, E, S> entryHelper;
   static final MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry> UNSET_WEAK_VALUE_REFERENCE = new MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry>() {
      public MapMakerInternalMap.DummyInternalEntry getEntry() {
         return null;
      }

      @Override
      public void clear() {
      }

      @Override
      public Object get() {
         return null;
      }

      public MapMakerInternalMap.WeakValueReference<Object, Object, MapMakerInternalMap.DummyInternalEntry> copyFor(
         ReferenceQueue<Object> queue, MapMakerInternalMap.DummyInternalEntry entry
      ) {
         return this;
      }
   };
   transient Set<K> keySet;
   transient Collection<V> values;
   transient Set<Entry<K, V>> entrySet;
   private static final long serialVersionUID = 5L;

   private MapMakerInternalMap(MapMaker builder, MapMakerInternalMap.InternalEntryHelper<K, V, E, S> entryHelper) {
      this.concurrencyLevel = Math.min(builder.getConcurrencyLevel(), 65536);
      this.keyEquivalence = builder.getKeyEquivalence();
      this.entryHelper = entryHelper;
      int initialCapacity = Math.min(builder.getInitialCapacity(), 1073741824);
      int segmentShift = 0;

      int segmentCount;
      for (segmentCount = 1; segmentCount < this.concurrencyLevel; segmentCount <<= 1) {
         segmentShift++;
      }

      this.segmentShift = 32 - segmentShift;
      this.segmentMask = segmentCount - 1;
      this.segments = this.newSegmentArray(segmentCount);
      int segmentCapacity = initialCapacity / segmentCount;
      if (segmentCapacity * segmentCount < initialCapacity) {
         segmentCapacity++;
      }

      int segmentSize = 1;

      while (segmentSize < segmentCapacity) {
         segmentSize <<= 1;
      }

      for (int i = 0; i < this.segments.length; i++) {
         this.segments[i] = this.createSegment(segmentSize, -1);
      }
   }

   static <K, V> MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?> create(MapMaker builder) {
      if (builder.getKeyStrength() == MapMakerInternalMap.Strength.STRONG && builder.getValueStrength() == MapMakerInternalMap.Strength.STRONG) {
         return (MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?>)(new MapMakerInternalMap<>(
            builder, MapMakerInternalMap.StrongKeyStrongValueEntry.Helper.instance()
         ));
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.STRONG && builder.getValueStrength() == MapMakerInternalMap.Strength.WEAK) {
         return (MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?>)(new MapMakerInternalMap<>(
            builder, MapMakerInternalMap.StrongKeyWeakValueEntry.Helper.instance()
         ));
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.WEAK && builder.getValueStrength() == MapMakerInternalMap.Strength.STRONG) {
         return (MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?>)(new MapMakerInternalMap<>(
            builder, MapMakerInternalMap.WeakKeyStrongValueEntry.Helper.instance()
         ));
      } else if (builder.getKeyStrength() == MapMakerInternalMap.Strength.WEAK && builder.getValueStrength() == MapMakerInternalMap.Strength.WEAK) {
         return (MapMakerInternalMap<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>, ?>)(new MapMakerInternalMap<>(
            builder, MapMakerInternalMap.WeakKeyWeakValueEntry.Helper.instance()
         ));
      } else {
         throw new AssertionError();
      }
   }

   static <K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> MapMakerInternalMap.WeakValueReference<K, V, E> unsetWeakValueReference() {
      return (MapMakerInternalMap.WeakValueReference<K, V, E>)UNSET_WEAK_VALUE_REFERENCE;
   }

   static int rehash(int h) {
      h += h << 15 ^ -12931;
      h ^= h >>> 10;
      h += h << 3;
      h ^= h >>> 6;
      h += (h << 2) + (h << 14);
      return h ^ h >>> 16;
   }

   @VisibleForTesting
   E copyEntry(E original, E newNext) {
      int hash = original.getHash();
      return this.segmentFor(hash).copyEntry(original, newNext);
   }

   int hash(Object key) {
      int h = this.keyEquivalence.hash(key);
      return rehash(h);
   }

   void reclaimValue(MapMakerInternalMap.WeakValueReference<K, V, E> valueReference) {
      E entry = valueReference.getEntry();
      int hash = entry.getHash();
      this.segmentFor(hash).reclaimValue(entry.getKey(), hash, valueReference);
   }

   void reclaimKey(E entry) {
      int hash = entry.getHash();
      this.segmentFor(hash).reclaimKey(entry, hash);
   }

   @VisibleForTesting
   boolean isLiveForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
      return this.segmentFor(entry.getHash()).getLiveValueForTesting(entry) != null;
   }

   MapMakerInternalMap.Segment<K, V, E, S> segmentFor(int hash) {
      return this.segments[hash >>> this.segmentShift & this.segmentMask];
   }

   MapMakerInternalMap.Segment<K, V, E, S> createSegment(int initialCapacity, int maxSegmentSize) {
      return this.entryHelper.newSegment(this, initialCapacity, maxSegmentSize);
   }

   V getLiveValue(E entry) {
      if (entry.getKey() == null) {
         return null;
      } else {
         V value = entry.getValue();
         return value == null ? null : value;
      }
   }

   final MapMakerInternalMap.Segment<K, V, E, S>[] newSegmentArray(int ssize) {
      return new MapMakerInternalMap.Segment[ssize];
   }

   @VisibleForTesting
   MapMakerInternalMap.Strength keyStrength() {
      return this.entryHelper.keyStrength();
   }

   @VisibleForTesting
   MapMakerInternalMap.Strength valueStrength() {
      return this.entryHelper.valueStrength();
   }

   @VisibleForTesting
   Equivalence<Object> valueEquivalence() {
      return this.entryHelper.valueStrength().defaultEquivalence();
   }

   @Override
   public boolean isEmpty() {
      long sum = 0L;
      MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;

      for (int i = 0; i < segments.length; i++) {
         if (segments[i].count != 0) {
            return false;
         }

         sum += segments[i].modCount;
      }

      if (sum != 0L) {
         for (int i = 0; i < segments.length; i++) {
            if (segments[i].count != 0) {
               return false;
            }

            sum -= segments[i].modCount;
         }

         if (sum != 0L) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int size() {
      MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;
      long sum = 0L;

      for (int i = 0; i < segments.length; i++) {
         sum += segments[i].count;
      }

      return Ints.saturatedCast(sum);
   }

   @Override
   public V get(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).get(key, hash);
      }
   }

   E getEntry(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).getEntry(key, hash);
      }
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      if (key == null) {
         return false;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).containsKey(key, hash);
      }
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      if (value == null) {
         return false;
      } else {
         MapMakerInternalMap.Segment<K, V, E, S>[] segments = this.segments;
         long last = -1L;

         for (int i = 0; i < 3; i++) {
            long sum = 0L;

            for (MapMakerInternalMap.Segment<K, V, E, S> segment : segments) {
               int unused = segment.count;
               AtomicReferenceArray<E> table = segment.table;

               for (int j = 0; j < table.length(); j++) {
                  for (E e = table.get(j); e != null; e = e.getNext()) {
                     V v = segment.getLiveValue(e);
                     if (v != null && this.valueEquivalence().equivalent(value, v)) {
                        return true;
                     }
                  }
               }

               sum += segment.modCount;
            }

            if (sum == last) {
               break;
            }

            last = sum;
         }

         return false;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public V put(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).put(key, hash, value, false);
   }

   @CanIgnoreReturnValue
   @Override
   public V putIfAbsent(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).put(key, hash, value, true);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      for (Entry<? extends K, ? extends V> e : m.entrySet()) {
         this.put((K)e.getKey(), (V)e.getValue());
      }
   }

   @CanIgnoreReturnValue
   @Override
   public V remove(@Nullable Object key) {
      if (key == null) {
         return null;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).remove(key, hash);
      }
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(@Nullable Object key, @Nullable Object value) {
      if (key != null && value != null) {
         int hash = this.hash(key);
         return this.segmentFor(hash).remove(key, hash, value);
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public boolean replace(K key, @Nullable V oldValue, V newValue) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(newValue);
      if (oldValue == null) {
         return false;
      } else {
         int hash = this.hash(key);
         return this.segmentFor(hash).replace(key, hash, oldValue, newValue);
      }
   }

   @CanIgnoreReturnValue
   @Override
   public V replace(K key, V value) {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      int hash = this.hash(key);
      return this.segmentFor(hash).replace(key, hash, value);
   }

   @Override
   public void clear() {
      for (MapMakerInternalMap.Segment<K, V, E, S> segment : this.segments) {
         segment.clear();
      }
   }

   @Override
   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks != null ? ks : (this.keySet = new MapMakerInternalMap.KeySet());
   }

   @Override
   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs != null ? vs : (this.values = new MapMakerInternalMap.Values());
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      return es != null ? es : (this.entrySet = new MapMakerInternalMap.EntrySet());
   }

   private static <E> ArrayList<E> toArrayList(Collection<E> c) {
      ArrayList<E> result = new ArrayList<>(c.size());
      Iterators.addAll(result, c.iterator());
      return result;
   }

   Object writeReplace() {
      return new MapMakerInternalMap.SerializationProxy<>(
         this.entryHelper.keyStrength(),
         this.entryHelper.valueStrength(),
         this.keyEquivalence,
         this.entryHelper.valueStrength().defaultEquivalence(),
         this.concurrencyLevel,
         this
      );
   }

   abstract static class AbstractSerializationProxy<K, V> extends ForwardingConcurrentMap<K, V> implements Serializable {
      private static final long serialVersionUID = 3L;
      final MapMakerInternalMap.Strength keyStrength;
      final MapMakerInternalMap.Strength valueStrength;
      final Equivalence<Object> keyEquivalence;
      final Equivalence<Object> valueEquivalence;
      final int concurrencyLevel;
      transient ConcurrentMap<K, V> delegate;

      AbstractSerializationProxy(
         MapMakerInternalMap.Strength keyStrength,
         MapMakerInternalMap.Strength valueStrength,
         Equivalence<Object> keyEquivalence,
         Equivalence<Object> valueEquivalence,
         int concurrencyLevel,
         ConcurrentMap<K, V> delegate
      ) {
         this.keyStrength = keyStrength;
         this.valueStrength = valueStrength;
         this.keyEquivalence = keyEquivalence;
         this.valueEquivalence = valueEquivalence;
         this.concurrencyLevel = concurrencyLevel;
         this.delegate = delegate;
      }

      @Override
      protected ConcurrentMap<K, V> delegate() {
         return this.delegate;
      }

      void writeMapTo(ObjectOutputStream out) throws IOException {
         out.writeInt(this.delegate.size());

         for (Entry<K, V> entry : this.delegate.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
         }

         out.writeObject(null);
      }

      MapMaker readMapMaker(ObjectInputStream in) throws IOException {
         int size = in.readInt();
         return new MapMaker()
            .initialCapacity(size)
            .setKeyStrength(this.keyStrength)
            .setValueStrength(this.valueStrength)
            .keyEquivalence(this.keyEquivalence)
            .concurrencyLevel(this.concurrencyLevel);
      }

      void readEntries(ObjectInputStream in) throws IOException, ClassNotFoundException {
         while (true) {
            K key = (K)in.readObject();
            if (key == null) {
               return;
            }

            V value = (V)in.readObject();
            this.delegate.put(key, value);
         }
      }
   }

   abstract static class AbstractStrongKeyEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>>
      implements MapMakerInternalMap.InternalEntry<K, V, E> {
      final K key;
      final int hash;
      final E next;

      AbstractStrongKeyEntry(K key, int hash, @Nullable E next) {
         this.key = key;
         this.hash = hash;
         this.next = next;
      }

      @Override
      public K getKey() {
         return this.key;
      }

      @Override
      public int getHash() {
         return this.hash;
      }

      @Override
      public E getNext() {
         return this.next;
      }
   }

   abstract static class AbstractWeakKeyEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>>
      extends WeakReference<K>
      implements MapMakerInternalMap.InternalEntry<K, V, E> {
      final int hash;
      final E next;

      AbstractWeakKeyEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable E next) {
         super(key, queue);
         this.hash = hash;
         this.next = next;
      }

      @Override
      public K getKey() {
         return this.get();
      }

      @Override
      public int getHash() {
         return this.hash;
      }

      @Override
      public E getNext() {
         return this.next;
      }
   }

   static final class CleanupMapTask implements Runnable {
      final WeakReference<MapMakerInternalMap<?, ?, ?, ?>> mapReference;

      public CleanupMapTask(MapMakerInternalMap<?, ?, ?, ?> map) {
         this.mapReference = new WeakReference<>(map);
      }

      @Override
      public void run() {
         MapMakerInternalMap<?, ?, ?, ?> map = this.mapReference.get();
         if (map == null) {
            throw new CancellationException();
         } else {
            for (MapMakerInternalMap.Segment<?, ?, ?, ?> segment : map.segments) {
               segment.runCleanup();
            }
         }
      }
   }

   static final class DummyInternalEntry implements MapMakerInternalMap.InternalEntry<Object, Object, MapMakerInternalMap.DummyInternalEntry> {
      private DummyInternalEntry() {
         throw new AssertionError();
      }

      public MapMakerInternalMap.DummyInternalEntry getNext() {
         throw new AssertionError();
      }

      @Override
      public int getHash() {
         throw new AssertionError();
      }

      @Override
      public Object getKey() {
         throw new AssertionError();
      }

      @Override
      public Object getValue() {
         throw new AssertionError();
      }
   }

   final class EntryIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<Entry<K, V>> {
      public Entry<K, V> next() {
         return this.nextEntry();
      }
   }

   final class EntrySet extends MapMakerInternalMap.SafeToArraySet<Entry<K, V>> {
      @Override
      public Iterator<Entry<K, V>> iterator() {
         return MapMakerInternalMap.this.new EntryIterator();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            Object key = e.getKey();
            if (key == null) {
               return false;
            } else {
               V v = MapMakerInternalMap.this.get(key);
               return v != null && MapMakerInternalMap.this.valueEquivalence().equivalent(e.getValue(), v);
            }
         }
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> e = (Entry<?, ?>)o;
            Object key = e.getKey();
            return key != null && MapMakerInternalMap.this.remove(key, e.getValue());
         }
      }

      @Override
      public int size() {
         return MapMakerInternalMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      @Override
      public void clear() {
         MapMakerInternalMap.this.clear();
      }
   }

   abstract class HashIterator<T> implements Iterator<T> {
      int nextSegmentIndex = MapMakerInternalMap.this.segments.length - 1;
      int nextTableIndex = -1;
      MapMakerInternalMap.Segment<K, V, E, S> currentSegment;
      AtomicReferenceArray<E> currentTable;
      E nextEntry;
      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry nextExternal;
      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry lastReturned;

      HashIterator() {
         this.advance();
      }

      @Override
      public abstract T next();

      final void advance() {
         this.nextExternal = null;
         if (!this.nextInChain()) {
            if (!this.nextInTable()) {
               while (this.nextSegmentIndex >= 0) {
                  this.currentSegment = MapMakerInternalMap.this.segments[this.nextSegmentIndex--];
                  if (this.currentSegment.count != 0) {
                     this.currentTable = this.currentSegment.table;
                     this.nextTableIndex = this.currentTable.length() - 1;
                     if (this.nextInTable()) {
                        return;
                     }
                  }
               }
            }
         }
      }

      boolean nextInChain() {
         if (this.nextEntry != null) {
            for (this.nextEntry = this.nextEntry.getNext(); this.nextEntry != null; this.nextEntry = this.nextEntry.getNext()) {
               if (this.advanceTo(this.nextEntry)) {
                  return true;
               }
            }
         }

         return false;
      }

      boolean nextInTable() {
         while (this.nextTableIndex >= 0) {
            if ((this.nextEntry = this.currentTable.get(this.nextTableIndex--)) != null && (this.advanceTo(this.nextEntry) || this.nextInChain())) {
               return true;
            }
         }

         return false;
      }

      boolean advanceTo(E entry) {
         boolean var4;
         try {
            K key = entry.getKey();
            V value = MapMakerInternalMap.this.getLiveValue(entry);
            if (value == null) {
               return false;
            }

            this.nextExternal = MapMakerInternalMap.this.new WriteThroughEntry(key, value);
            var4 = true;
         } finally {
            this.currentSegment.postReadCleanup();
         }

         return var4;
      }

      @Override
      public boolean hasNext() {
         return this.nextExternal != null;
      }

      MapMakerInternalMap<K, V, E, S>.WriteThroughEntry nextEntry() {
         if (this.nextExternal == null) {
            throw new NoSuchElementException();
         } else {
            this.lastReturned = this.nextExternal;
            this.advance();
            return this.lastReturned;
         }
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(this.lastReturned != null);
         MapMakerInternalMap.this.remove(this.lastReturned.getKey());
         this.lastReturned = null;
      }
   }

   interface InternalEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> {
      E getNext();

      int getHash();

      K getKey();

      V getValue();
   }

   interface InternalEntryHelper<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>> {
      MapMakerInternalMap.Strength keyStrength();

      MapMakerInternalMap.Strength valueStrength();

      S newSegment(MapMakerInternalMap<K, V, E, S> var1, int var2, int var3);

      E newEntry(S var1, K var2, int var3, @Nullable E var4);

      E copy(S var1, E var2, @Nullable E var3);

      void setValue(S var1, E var2, V var3);
   }

   final class KeyIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<K> {
      @Override
      public K next() {
         return this.nextEntry().getKey();
      }
   }

   final class KeySet extends MapMakerInternalMap.SafeToArraySet<K> {
      @Override
      public Iterator<K> iterator() {
         return MapMakerInternalMap.this.new KeyIterator();
      }

      @Override
      public int size() {
         return MapMakerInternalMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return MapMakerInternalMap.this.containsKey(o);
      }

      @Override
      public boolean remove(Object o) {
         return MapMakerInternalMap.this.remove(o) != null;
      }

      @Override
      public void clear() {
         MapMakerInternalMap.this.clear();
      }
   }

   private abstract static class SafeToArraySet<E> extends AbstractSet<E> {
      private SafeToArraySet() {
      }

      @Override
      public Object[] toArray() {
         return MapMakerInternalMap.toArrayList(this).toArray();
      }

      @Override
      public <E> E[] toArray(E[] a) {
         return MapMakerInternalMap.toArrayList(this).toArray(a);
      }
   }

   abstract static class Segment<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>, S extends MapMakerInternalMap.Segment<K, V, E, S>>
      extends ReentrantLock {
      @Weak
      final MapMakerInternalMap<K, V, E, S> map;
      volatile int count;
      int modCount;
      int threshold;
      volatile AtomicReferenceArray<E> table;
      final int maxSegmentSize;
      final AtomicInteger readCount = new AtomicInteger();

      Segment(MapMakerInternalMap<K, V, E, S> map, int initialCapacity, int maxSegmentSize) {
         this.map = map;
         this.maxSegmentSize = maxSegmentSize;
         this.initTable(this.newEntryArray(initialCapacity));
      }

      abstract S self();

      @GuardedBy("this")
      void maybeDrainReferenceQueues() {
      }

      void maybeClearReferenceQueues() {
      }

      void setValue(E entry, V value) {
         this.map.entryHelper.setValue(this.self(), entry, value);
      }

      E copyEntry(E original, E newNext) {
         return this.map.entryHelper.copy(this.self(), original, newNext);
      }

      AtomicReferenceArray<E> newEntryArray(int size) {
         return new AtomicReferenceArray<>(size);
      }

      void initTable(AtomicReferenceArray<E> newTable) {
         this.threshold = newTable.length() * 3 / 4;
         if (this.threshold == this.maxSegmentSize) {
            this.threshold++;
         }

         this.table = newTable;
      }

      abstract E castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> var1);

      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         throw new AssertionError();
      }

      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         throw new AssertionError();
      }

      MapMakerInternalMap.WeakValueReference<K, V, E> getWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         throw new AssertionError();
      }

      MapMakerInternalMap.WeakValueReference<K, V, E> newWeakValueReferenceForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, V value) {
         throw new AssertionError();
      }

      void setWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> entry,
         MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference
      ) {
         throw new AssertionError();
      }

      void setTableEntryForTesting(int i, MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         this.table.set(i, this.castForTesting(entry));
      }

      E copyForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, @Nullable MapMakerInternalMap.InternalEntry<K, V, ?> newNext) {
         return this.map.entryHelper.copy(this.self(), this.castForTesting(entry), this.castForTesting(newNext));
      }

      void setValueForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry, V value) {
         this.map.entryHelper.setValue(this.self(), this.castForTesting(entry), value);
      }

      E newEntryForTesting(K key, int hash, @Nullable MapMakerInternalMap.InternalEntry<K, V, ?> next) {
         return this.map.entryHelper.newEntry(this.self(), key, hash, this.castForTesting(next));
      }

      @CanIgnoreReturnValue
      boolean removeTableEntryForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.removeEntryForTesting(this.castForTesting(entry));
      }

      E removeFromChainForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> first, MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.removeFromChain(this.castForTesting(first), this.castForTesting(entry));
      }

      @Nullable
      V getLiveValueForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return this.getLiveValue(this.castForTesting(entry));
      }

      void tryDrainReferenceQueues() {
         if (this.tryLock()) {
            try {
               this.maybeDrainReferenceQueues();
            } finally {
               this.unlock();
            }
         }
      }

      @GuardedBy("this")
      void drainKeyReferenceQueue(ReferenceQueue<K> keyReferenceQueue) {
         int i = 0;

         Reference<? extends K> ref;
         while ((ref = keyReferenceQueue.poll()) != null) {
            E entry = (E)ref;
            this.map.reclaimKey(entry);
            if (++i == 16) {
               break;
            }
         }
      }

      @GuardedBy("this")
      void drainValueReferenceQueue(ReferenceQueue<V> valueReferenceQueue) {
         int i = 0;

         Reference<? extends V> ref;
         while ((ref = valueReferenceQueue.poll()) != null) {
            MapMakerInternalMap.WeakValueReference<K, V, E> valueReference = (MapMakerInternalMap.WeakValueReference<K, V, E>)ref;
            this.map.reclaimValue(valueReference);
            if (++i == 16) {
               break;
            }
         }
      }

      <T> void clearReferenceQueue(ReferenceQueue<T> referenceQueue) {
         while (referenceQueue.poll() != null) {
         }
      }

      E getFirst(int hash) {
         AtomicReferenceArray<E> table = this.table;
         return table.get(hash & table.length() - 1);
      }

      E getEntry(Object key, int hash) {
         if (this.count != 0) {
            for (E e = this.getFirst(hash); e != null; e = e.getNext()) {
               if (e.getHash() == hash) {
                  K entryKey = e.getKey();
                  if (entryKey == null) {
                     this.tryDrainReferenceQueues();
                  } else if (this.map.keyEquivalence.equivalent(key, entryKey)) {
                     return e;
                  }
               }
            }
         }

         return null;
      }

      E getLiveEntry(Object key, int hash) {
         return this.getEntry(key, hash);
      }

      V get(Object key, int hash) {
         V value;
         try {
            E e = this.getLiveEntry(key, hash);
            if (e != null) {
               value = e.getValue();
               if (value == null) {
                  this.tryDrainReferenceQueues();
               }

               return value;
            }

            value = null;
         } finally {
            this.postReadCleanup();
         }

         return value;
      }

      boolean containsKey(Object key, int hash) {
         boolean var4;
         try {
            if (this.count == 0) {
               return false;
            }

            E e = this.getLiveEntry(key, hash);
            var4 = e != null && e.getValue() != null;
         } finally {
            this.postReadCleanup();
         }

         return var4;
      }

      @VisibleForTesting
      boolean containsValue(Object value) {
         try {
            if (this.count != 0) {
               AtomicReferenceArray<E> table = this.table;
               int length = table.length();

               for (int i = 0; i < length; i++) {
                  for (E e = table.get(i); e != null; e = e.getNext()) {
                     V entryValue = this.getLiveValue(e);
                     if (entryValue != null && this.map.valueEquivalence().equivalent(value, entryValue)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         } finally {
            this.postReadCleanup();
         }
      }

      V put(K key, int hash, V value, boolean onlyIfAbsent) {
         this.lock();

         try {
            this.preWriteCleanup();
            int newCount = this.count + 1;
            if (newCount > this.threshold) {
               this.expand();
               newCount = this.count + 1;
            }

            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  if (entryValue == null) {
                     this.modCount++;
                     this.setValue(e, value);
                     newCount = this.count;
                     this.count = newCount;
                     return null;
                  }

                  if (!onlyIfAbsent) {
                     this.modCount++;
                     this.setValue(e, value);
                     return entryValue;
                  }

                  return entryValue;
               }
            }

            this.modCount++;
            E newEntry = this.map.entryHelper.newEntry(this.self(), key, hash, first);
            this.setValue(newEntry, value);
            table.set(index, newEntry);
            this.count = newCount;
            return null;
         } finally {
            this.unlock();
         }
      }

      @GuardedBy("this")
      void expand() {
         AtomicReferenceArray<E> oldTable = this.table;
         int oldCapacity = oldTable.length();
         if (oldCapacity < 1073741824) {
            int newCount = this.count;
            AtomicReferenceArray<E> newTable = this.newEntryArray(oldCapacity << 1);
            this.threshold = newTable.length() * 3 / 4;
            int newMask = newTable.length() - 1;

            for (int oldIndex = 0; oldIndex < oldCapacity; oldIndex++) {
               E head = oldTable.get(oldIndex);
               if (head != null) {
                  E next = head.getNext();
                  int headIndex = head.getHash() & newMask;
                  if (next == null) {
                     newTable.set(headIndex, head);
                  } else {
                     E tail = head;
                     int tailIndex = headIndex;

                     for (E e = next; e != null; e = e.getNext()) {
                        int newIndex = e.getHash() & newMask;
                        if (newIndex != tailIndex) {
                           tailIndex = newIndex;
                           tail = e;
                        }
                     }

                     newTable.set(tailIndex, tail);

                     for (E ex = head; ex != tail; ex = ex.getNext()) {
                        int newIndex = ex.getHash() & newMask;
                        E newNext = newTable.get(newIndex);
                        E newFirst = this.copyEntry(ex, newNext);
                        if (newFirst != null) {
                           newTable.set(newIndex, newFirst);
                        } else {
                           newCount--;
                        }
                     }
                  }
               }
            }

            this.table = newTable;
            this.count = newCount;
         }
      }

      boolean replace(K key, int hash, V oldValue, V newValue) {
         this.lock();

         try {
            this.preWriteCleanup();
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  if (entryValue != null) {
                     if (!this.map.valueEquivalence().equivalent(oldValue, entryValue)) {
                        return false;
                     }

                     this.modCount++;
                     this.setValue(e, newValue);
                     return true;
                  }

                  if (isCollected(e)) {
                     int newCount = this.count - 1;
                     this.modCount++;
                     E newFirst = this.removeFromChain(first, e);
                     newCount = this.count - 1;
                     table.set(index, newFirst);
                     this.count = newCount;
                  }

                  return false;
               }
            }

            return false;
         } finally {
            this.unlock();
         }
      }

      V replace(K key, int hash, V newValue) {
         this.lock();

         try {
            this.preWriteCleanup();
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  if (entryValue != null) {
                     this.modCount++;
                     this.setValue(e, newValue);
                     return entryValue;
                  }

                  if (isCollected(e)) {
                     int newCount = this.count - 1;
                     this.modCount++;
                     E newFirst = this.removeFromChain(first, e);
                     newCount = this.count - 1;
                     table.set(index, newFirst);
                     this.count = newCount;
                  }

                  return null;
               }
            }

            return null;
         } finally {
            this.unlock();
         }
      }

      @CanIgnoreReturnValue
      V remove(Object key, int hash) {
         this.lock();

         try {
            this.preWriteCleanup();
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  if (entryValue != null || isCollected(e)) {
                     this.modCount++;
                     E newFirst = this.removeFromChain(first, e);
                     newCount = this.count - 1;
                     table.set(index, newFirst);
                     this.count = newCount;
                     return entryValue;
                  }

                  return null;
               }
            }

            return null;
         } finally {
            this.unlock();
         }
      }

      boolean remove(Object key, int hash, Object value) {
         this.lock();

         try {
            this.preWriteCleanup();
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  V entryValue = e.getValue();
                  boolean explicitRemoval = false;
                  if (this.map.valueEquivalence().equivalent(value, entryValue)) {
                     explicitRemoval = true;
                  } else if (!isCollected(e)) {
                     return false;
                  }

                  this.modCount++;
                  E newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  return explicitRemoval;
               }
            }

            return false;
         } finally {
            this.unlock();
         }
      }

      void clear() {
         if (this.count != 0) {
            this.lock();

            try {
               AtomicReferenceArray<E> table = this.table;

               for (int i = 0; i < table.length(); i++) {
                  table.set(i, null);
               }

               this.maybeClearReferenceQueues();
               this.readCount.set(0);
               this.modCount++;
               this.count = 0;
            } finally {
               this.unlock();
            }
         }
      }

      @GuardedBy("this")
      E removeFromChain(E first, E entry) {
         int newCount = this.count;
         E newFirst = entry.getNext();

         for (E e = first; e != entry; e = e.getNext()) {
            E next = this.copyEntry(e, newFirst);
            if (next != null) {
               newFirst = next;
            } else {
               newCount--;
            }
         }

         this.count = newCount;
         return newFirst;
      }

      @CanIgnoreReturnValue
      boolean reclaimKey(E entry, int hash) {
         this.lock();

         try {
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               if (e == entry) {
                  this.modCount++;
                  E newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  return true;
               }
            }

            return false;
         } finally {
            this.unlock();
         }
      }

      @CanIgnoreReturnValue
      boolean reclaimValue(K key, int hash, MapMakerInternalMap.WeakValueReference<K, V, E> valueReference) {
         this.lock();

         try {
            int newCount = this.count - 1;
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  MapMakerInternalMap.WeakValueReference<K, V, E> v = ((MapMakerInternalMap.WeakValueEntry)e).getValueReference();
                  if (v != valueReference) {
                     return false;
                  }

                  this.modCount++;
                  E newFirst = this.removeFromChain(first, e);
                  newCount = this.count - 1;
                  table.set(index, newFirst);
                  this.count = newCount;
                  return true;
               }
            }

            return false;
         } finally {
            this.unlock();
         }
      }

      @CanIgnoreReturnValue
      boolean clearValueForTesting(
         K key, int hash, MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference
      ) {
         this.lock();

         try {
            AtomicReferenceArray<E> table = this.table;
            int index = hash & table.length() - 1;
            E first = table.get(index);

            for (E e = first; e != null; e = e.getNext()) {
               K entryKey = e.getKey();
               if (e.getHash() == hash && entryKey != null && this.map.keyEquivalence.equivalent(key, entryKey)) {
                  MapMakerInternalMap.WeakValueReference<K, V, E> v = ((MapMakerInternalMap.WeakValueEntry)e).getValueReference();
                  if (v != valueReference) {
                     return false;
                  }

                  E newFirst = this.removeFromChain(first, e);
                  table.set(index, newFirst);
                  return true;
               }
            }

            return false;
         } finally {
            this.unlock();
         }
      }

      @GuardedBy("this")
      boolean removeEntryForTesting(E entry) {
         int hash = entry.getHash();
         int newCount = this.count - 1;
         AtomicReferenceArray<E> table = this.table;
         int index = hash & table.length() - 1;
         E first = table.get(index);

         for (E e = first; e != null; e = e.getNext()) {
            if (e == entry) {
               this.modCount++;
               E newFirst = this.removeFromChain(first, e);
               newCount = this.count - 1;
               table.set(index, newFirst);
               this.count = newCount;
               return true;
            }
         }

         return false;
      }

      static <K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> boolean isCollected(E entry) {
         return entry.getValue() == null;
      }

      @Nullable
      V getLiveValue(E entry) {
         if (entry.getKey() == null) {
            this.tryDrainReferenceQueues();
            return null;
         } else {
            V value = entry.getValue();
            if (value == null) {
               this.tryDrainReferenceQueues();
               return null;
            } else {
               return value;
            }
         }
      }

      void postReadCleanup() {
         if ((this.readCount.incrementAndGet() & 63) == 0) {
            this.runCleanup();
         }
      }

      @GuardedBy("this")
      void preWriteCleanup() {
         this.runLockedCleanup();
      }

      void runCleanup() {
         this.runLockedCleanup();
      }

      void runLockedCleanup() {
         if (this.tryLock()) {
            try {
               this.maybeDrainReferenceQueues();
               this.readCount.set(0);
            } finally {
               this.unlock();
            }
         }
      }
   }

   private static final class SerializationProxy<K, V> extends MapMakerInternalMap.AbstractSerializationProxy<K, V> {
      private static final long serialVersionUID = 3L;

      SerializationProxy(
         MapMakerInternalMap.Strength keyStrength,
         MapMakerInternalMap.Strength valueStrength,
         Equivalence<Object> keyEquivalence,
         Equivalence<Object> valueEquivalence,
         int concurrencyLevel,
         ConcurrentMap<K, V> delegate
      ) {
         super(keyStrength, valueStrength, keyEquivalence, valueEquivalence, concurrencyLevel, delegate);
      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
         this.writeMapTo(out);
      }

      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         MapMaker mapMaker = this.readMapMaker(in);
         this.delegate = mapMaker.makeMap();
         this.readEntries(in);
      }

      private Object readResolve() {
         return this.delegate;
      }
   }

   static enum Strength {
      STRONG {
         @Override
         Equivalence<Object> defaultEquivalence() {
            return Equivalence.equals();
         }
      },
      WEAK {
         @Override
         Equivalence<Object> defaultEquivalence() {
            return Equivalence.identity();
         }
      };

      private Strength() {
      }

      abstract Equivalence<Object> defaultEquivalence();
   }

   static final class StrongKeyStrongValueEntry<K, V>
      extends MapMakerInternalMap.AbstractStrongKeyEntry<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>>
      implements MapMakerInternalMap.StrongValueEntry<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>> {
      @Nullable
      private volatile V value = (V)null;

      StrongKeyStrongValueEntry(K key, int hash, @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> next) {
         super(key, hash, next);
      }

      @Nullable
      @Override
      public V getValue() {
         return this.value;
      }

      void setValue(V value) {
         this.value = value;
      }

      MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> copy(MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newNext) {
         MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newEntry = new MapMakerInternalMap.StrongKeyStrongValueEntry<>(this.key, this.hash, newNext);
         newEntry.value = this.value;
         return newEntry;
      }

      static final class Helper<K, V>
         implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> {
         private static final MapMakerInternalMap.StrongKeyStrongValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.StrongKeyStrongValueEntry.Helper();

         static <K, V> MapMakerInternalMap.StrongKeyStrongValueEntry.Helper<K, V> instance() {
            return (MapMakerInternalMap.StrongKeyStrongValueEntry.Helper<K, V>)INSTANCE;
         }

         @Override
         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         @Override
         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> newSegment(
            MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> map,
            int initialCapacity,
            int maxSegmentSize
         ) {
            return new MapMakerInternalMap.StrongKeyStrongValueSegment<>(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> copy(
            MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment,
            MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> entry,
            @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newNext
         ) {
            return entry.copy(newNext);
         }

         public void setValue(MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> entry, V value) {
            entry.setValue(value);
         }

         public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> newEntry(
            MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> next
         ) {
            return new MapMakerInternalMap.StrongKeyStrongValueEntry<>(key, hash, next);
         }
      }
   }

   static final class StrongKeyStrongValueSegment<K, V>
      extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> {
      StrongKeyStrongValueSegment(
         MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>, MapMakerInternalMap.StrongKeyStrongValueSegment<K, V>> map,
         int initialCapacity,
         int maxSegmentSize
      ) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.StrongKeyStrongValueSegment<K, V> self() {
         return this;
      }

      public MapMakerInternalMap.StrongKeyStrongValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.StrongKeyStrongValueEntry<K, V>)entry;
      }
   }

   static final class StrongKeyWeakValueEntry<K, V>
      extends MapMakerInternalMap.AbstractStrongKeyEntry<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>>
      implements MapMakerInternalMap.WeakValueEntry<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> {
      private volatile MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

      StrongKeyWeakValueEntry(K key, int hash, @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> next) {
         super(key, hash, next);
      }

      @Override
      public V getValue() {
         return this.valueReference.get();
      }

      @Override
      public void clearValue() {
         this.valueReference.clear();
      }

      void setValue(V value, ReferenceQueue<V> queueForValues) {
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> previous = this.valueReference;
         this.valueReference = new MapMakerInternalMap.WeakValueReferenceImpl<>(queueForValues, value, this);
         previous.clear();
      }

      MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> copy(ReferenceQueue<V> queueForValues, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newNext) {
         MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newEntry = new MapMakerInternalMap.StrongKeyWeakValueEntry<>(this.key, this.hash, newNext);
         newEntry.valueReference = this.valueReference.copyFor(queueForValues, newEntry);
         return newEntry;
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> getValueReference() {
         return this.valueReference;
      }

      static final class Helper<K, V>
         implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> {
         private static final MapMakerInternalMap.StrongKeyWeakValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.StrongKeyWeakValueEntry.Helper();

         static <K, V> MapMakerInternalMap.StrongKeyWeakValueEntry.Helper<K, V> instance() {
            return (MapMakerInternalMap.StrongKeyWeakValueEntry.Helper<K, V>)INSTANCE;
         }

         @Override
         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         @Override
         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> newSegment(
            MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> map,
            int initialCapacity,
            int maxSegmentSize
         ) {
            return new MapMakerInternalMap.StrongKeyWeakValueSegment<>(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> copy(
            MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment,
            MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry,
            @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newNext
         ) {
            return MapMakerInternalMap.Segment.isCollected(entry) ? null : entry.copy(segment.queueForValues, newNext);
         }

         public void setValue(MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry, V value) {
            entry.setValue(value, segment.queueForValues);
         }

         public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> newEntry(
            MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> next
         ) {
            return new MapMakerInternalMap.StrongKeyWeakValueEntry<>(key, hash, next);
         }
      }
   }

   static final class StrongKeyWeakValueSegment<K, V>
      extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> {
      private final ReferenceQueue<V> queueForValues = new ReferenceQueue<>();

      StrongKeyWeakValueSegment(
         MapMakerInternalMap<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>, MapMakerInternalMap.StrongKeyWeakValueSegment<K, V>> map,
         int initialCapacity,
         int maxSegmentSize
      ) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.StrongKeyWeakValueSegment<K, V> self() {
         return this;
      }

      @Override
      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         return this.queueForValues;
      }

      public MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>)entry;
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e
      ) {
         return this.castForTesting(e).getValueReference();
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e, V value
      ) {
         return new MapMakerInternalMap.WeakValueReferenceImpl<>(this.queueForValues, value, this.castForTesting(e));
      }

      @Override
      public void setWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e,
         MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference
      ) {
         MapMakerInternalMap.StrongKeyWeakValueEntry<K, V> entry = this.castForTesting(e);
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>> previous = entry.valueReference;
         entry.valueReference = (MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.StrongKeyWeakValueEntry<K, V>>)valueReference;
         previous.clear();
      }

      @Override
      void maybeDrainReferenceQueues() {
         this.drainValueReferenceQueue(this.queueForValues);
      }

      @Override
      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForValues);
      }
   }

   interface StrongValueEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends MapMakerInternalMap.InternalEntry<K, V, E> {
   }

   final class ValueIterator extends MapMakerInternalMap<K, V, E, S>.HashIterator<V> {
      @Override
      public V next() {
         return this.nextEntry().getValue();
      }
   }

   final class Values extends AbstractCollection<V> {
      @Override
      public Iterator<V> iterator() {
         return MapMakerInternalMap.this.new ValueIterator();
      }

      @Override
      public int size() {
         return MapMakerInternalMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return MapMakerInternalMap.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return MapMakerInternalMap.this.containsValue(o);
      }

      @Override
      public void clear() {
         MapMakerInternalMap.this.clear();
      }

      @Override
      public Object[] toArray() {
         return MapMakerInternalMap.toArrayList(this).toArray();
      }

      @Override
      public <E> E[] toArray(E[] a) {
         return MapMakerInternalMap.toArrayList(this).toArray(a);
      }
   }

   static final class WeakKeyStrongValueEntry<K, V>
      extends MapMakerInternalMap.AbstractWeakKeyEntry<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>>
      implements MapMakerInternalMap.StrongValueEntry<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>> {
      @Nullable
      private volatile V value = (V)null;

      WeakKeyStrongValueEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> next) {
         super(queue, key, hash, next);
      }

      @Nullable
      @Override
      public V getValue() {
         return this.value;
      }

      void setValue(V value) {
         this.value = value;
      }

      MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> copy(ReferenceQueue<K> queueForKeys, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newNext) {
         MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newEntry = new MapMakerInternalMap.WeakKeyStrongValueEntry<>(
            queueForKeys, this.getKey(), this.hash, newNext
         );
         newEntry.setValue(this.value);
         return newEntry;
      }

      static final class Helper<K, V>
         implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> {
         private static final MapMakerInternalMap.WeakKeyStrongValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.WeakKeyStrongValueEntry.Helper();

         static <K, V> MapMakerInternalMap.WeakKeyStrongValueEntry.Helper<K, V> instance() {
            return (MapMakerInternalMap.WeakKeyStrongValueEntry.Helper<K, V>)INSTANCE;
         }

         @Override
         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         @Override
         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.STRONG;
         }

         public MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> newSegment(
            MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> map,
            int initialCapacity,
            int maxSegmentSize
         ) {
            return new MapMakerInternalMap.WeakKeyStrongValueSegment<>(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> copy(
            MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment,
            MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> entry,
            @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newNext
         ) {
            return entry.getKey() == null ? null : entry.copy(segment.queueForKeys, newNext);
         }

         public void setValue(MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> entry, V value) {
            entry.setValue(value);
         }

         public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> newEntry(
            MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> next
         ) {
            return new MapMakerInternalMap.WeakKeyStrongValueEntry<>(segment.queueForKeys, key, hash, next);
         }
      }
   }

   static final class WeakKeyStrongValueSegment<K, V>
      extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> {
      private final ReferenceQueue<K> queueForKeys = new ReferenceQueue<>();

      WeakKeyStrongValueSegment(
         MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>, MapMakerInternalMap.WeakKeyStrongValueSegment<K, V>> map,
         int initialCapacity,
         int maxSegmentSize
      ) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.WeakKeyStrongValueSegment<K, V> self() {
         return this;
      }

      @Override
      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         return this.queueForKeys;
      }

      public MapMakerInternalMap.WeakKeyStrongValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.WeakKeyStrongValueEntry<K, V>)entry;
      }

      @Override
      void maybeDrainReferenceQueues() {
         this.drainKeyReferenceQueue(this.queueForKeys);
      }

      @Override
      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForKeys);
      }
   }

   static final class WeakKeyWeakValueEntry<K, V>
      extends MapMakerInternalMap.AbstractWeakKeyEntry<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>>
      implements MapMakerInternalMap.WeakValueEntry<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> {
      private volatile MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> valueReference = MapMakerInternalMap.unsetWeakValueReference();

      WeakKeyWeakValueEntry(ReferenceQueue<K> queue, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> next) {
         super(queue, key, hash, next);
      }

      @Override
      public V getValue() {
         return this.valueReference.get();
      }

      MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> copy(
         ReferenceQueue<K> queueForKeys, ReferenceQueue<V> queueForValues, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newNext
      ) {
         MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newEntry = new MapMakerInternalMap.WeakKeyWeakValueEntry<>(
            queueForKeys, this.getKey(), this.hash, newNext
         );
         newEntry.valueReference = this.valueReference.copyFor(queueForValues, newEntry);
         return newEntry;
      }

      @Override
      public void clearValue() {
         this.valueReference.clear();
      }

      void setValue(V value, ReferenceQueue<V> queueForValues) {
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> previous = this.valueReference;
         this.valueReference = new MapMakerInternalMap.WeakValueReferenceImpl<>(queueForValues, value, this);
         previous.clear();
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> getValueReference() {
         return this.valueReference;
      }

      static final class Helper<K, V>
         implements MapMakerInternalMap.InternalEntryHelper<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> {
         private static final MapMakerInternalMap.WeakKeyWeakValueEntry.Helper<?, ?> INSTANCE = new MapMakerInternalMap.WeakKeyWeakValueEntry.Helper();

         static <K, V> MapMakerInternalMap.WeakKeyWeakValueEntry.Helper<K, V> instance() {
            return (MapMakerInternalMap.WeakKeyWeakValueEntry.Helper<K, V>)INSTANCE;
         }

         @Override
         public MapMakerInternalMap.Strength keyStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         @Override
         public MapMakerInternalMap.Strength valueStrength() {
            return MapMakerInternalMap.Strength.WEAK;
         }

         public MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> newSegment(
            MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> map,
            int initialCapacity,
            int maxSegmentSize
         ) {
            return new MapMakerInternalMap.WeakKeyWeakValueSegment<>(map, initialCapacity, maxSegmentSize);
         }

         public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> copy(
            MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment,
            MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry,
            @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newNext
         ) {
            if (entry.getKey() == null) {
               return null;
            } else {
               return MapMakerInternalMap.Segment.isCollected(entry) ? null : entry.copy(segment.queueForKeys, segment.queueForValues, newNext);
            }
         }

         public void setValue(MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry, V value) {
            entry.setValue(value, segment.queueForValues);
         }

         public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> newEntry(
            MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> segment, K key, int hash, @Nullable MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> next
         ) {
            return new MapMakerInternalMap.WeakKeyWeakValueEntry<>(segment.queueForKeys, key, hash, next);
         }
      }
   }

   static final class WeakKeyWeakValueSegment<K, V>
      extends MapMakerInternalMap.Segment<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> {
      private final ReferenceQueue<K> queueForKeys = new ReferenceQueue<>();
      private final ReferenceQueue<V> queueForValues = new ReferenceQueue<>();

      WeakKeyWeakValueSegment(
         MapMakerInternalMap<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>, MapMakerInternalMap.WeakKeyWeakValueSegment<K, V>> map,
         int initialCapacity,
         int maxSegmentSize
      ) {
         super(map, initialCapacity, maxSegmentSize);
      }

      MapMakerInternalMap.WeakKeyWeakValueSegment<K, V> self() {
         return this;
      }

      @Override
      ReferenceQueue<K> getKeyReferenceQueueForTesting() {
         return this.queueForKeys;
      }

      @Override
      ReferenceQueue<V> getValueReferenceQueueForTesting() {
         return this.queueForValues;
      }

      public MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> castForTesting(MapMakerInternalMap.InternalEntry<K, V, ?> entry) {
         return (MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>)entry;
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> getWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e
      ) {
         return this.castForTesting(e).getValueReference();
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> newWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e, V value
      ) {
         return new MapMakerInternalMap.WeakValueReferenceImpl<>(this.queueForValues, value, this.castForTesting(e));
      }

      @Override
      public void setWeakValueReferenceForTesting(
         MapMakerInternalMap.InternalEntry<K, V, ?> e,
         MapMakerInternalMap.WeakValueReference<K, V, ? extends MapMakerInternalMap.InternalEntry<K, V, ?>> valueReference
      ) {
         MapMakerInternalMap.WeakKeyWeakValueEntry<K, V> entry = this.castForTesting(e);
         MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>> previous = entry.valueReference;
         entry.valueReference = (MapMakerInternalMap.WeakValueReference<K, V, MapMakerInternalMap.WeakKeyWeakValueEntry<K, V>>)valueReference;
         previous.clear();
      }

      @Override
      void maybeDrainReferenceQueues() {
         this.drainKeyReferenceQueue(this.queueForKeys);
         this.drainValueReferenceQueue(this.queueForValues);
      }

      @Override
      void maybeClearReferenceQueues() {
         this.clearReferenceQueue(this.queueForKeys);
      }
   }

   interface WeakValueEntry<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> extends MapMakerInternalMap.InternalEntry<K, V, E> {
      MapMakerInternalMap.WeakValueReference<K, V, E> getValueReference();

      void clearValue();
   }

   interface WeakValueReference<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>> {
      @Nullable
      V get();

      E getEntry();

      void clear();

      MapMakerInternalMap.WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> var1, E var2);
   }

   static final class WeakValueReferenceImpl<K, V, E extends MapMakerInternalMap.InternalEntry<K, V, E>>
      extends WeakReference<V>
      implements MapMakerInternalMap.WeakValueReference<K, V, E> {
      @Weak
      final E entry;

      WeakValueReferenceImpl(ReferenceQueue<V> queue, V referent, E entry) {
         super(referent, queue);
         this.entry = entry;
      }

      @Override
      public E getEntry() {
         return this.entry;
      }

      @Override
      public MapMakerInternalMap.WeakValueReference<K, V, E> copyFor(ReferenceQueue<V> queue, E entry) {
         return new MapMakerInternalMap.WeakValueReferenceImpl<>(queue, this.get(), entry);
      }
   }

   final class WriteThroughEntry extends AbstractMapEntry<K, V> {
      final K key;
      V value;

      WriteThroughEntry(K key, V value) {
         this.key = key;
         this.value = value;
      }

      @Override
      public K getKey() {
         return this.key;
      }

      @Override
      public V getValue() {
         return this.value;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (!(object instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> that = (Entry<?, ?>)object;
            return this.key.equals(that.getKey()) && this.value.equals(that.getValue());
         }
      }

      @Override
      public int hashCode() {
         return this.key.hashCode() ^ this.value.hashCode();
      }

      @Override
      public V setValue(V newValue) {
         V oldValue = MapMakerInternalMap.this.put(this.key, newValue);
         this.value = newValue;
         return oldValue;
      }
   }
}
