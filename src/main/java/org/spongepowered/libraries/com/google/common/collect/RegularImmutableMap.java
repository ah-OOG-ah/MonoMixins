package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.j2objc.annotations.Weak;

@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {
   private final transient Entry<K, V>[] entries;
   private final transient ImmutableMapEntry<K, V>[] table;
   private final transient int mask;
   private static final double MAX_LOAD_FACTOR = 1.2;
   private static final long serialVersionUID = 0L;

   static <K, V> RegularImmutableMap<K, V> fromEntries(Entry<K, V>... entries) {
      return fromEntryArray(entries.length, entries);
   }

   static <K, V> RegularImmutableMap<K, V> fromEntryArray(int n, Entry<K, V>[] entryArray) {
      Preconditions.checkPositionIndex(n, entryArray.length);
      Entry<K, V>[] entries;
      if (n == entryArray.length) {
         entries = entryArray;
      } else {
         entries = ImmutableMapEntry.createEntryArray(n);
      }

      int tableSize = Hashing.closedTableSize(n, 1.2);
      ImmutableMapEntry<K, V>[] table = ImmutableMapEntry.createEntryArray(tableSize);
      int mask = tableSize - 1;

      for (int entryIndex = 0; entryIndex < n; entryIndex++) {
         Entry<K, V> entry = entryArray[entryIndex];
         K key = entry.getKey();
         V value = entry.getValue();
         CollectPreconditions.checkEntryNotNull(key, value);
         int tableIndex = Hashing.smear(key.hashCode()) & mask;
         ImmutableMapEntry<K, V> existing = table[tableIndex];
         ImmutableMapEntry<K, V> newEntry;
         if (existing == null) {
            boolean reusable = entry instanceof ImmutableMapEntry && ((ImmutableMapEntry)entry).isReusable();
            newEntry = reusable ? (ImmutableMapEntry)entry : new ImmutableMapEntry<>(key, value);
         } else {
            newEntry = new ImmutableMapEntry.NonTerminalImmutableMapEntry<>(key, value, existing);
         }

         table[tableIndex] = newEntry;
         entries[entryIndex] = newEntry;
         checkNoConflictInKeyBucket(key, newEntry, existing);
      }

      return new RegularImmutableMap<>(entries, table, mask);
   }

   private RegularImmutableMap(Entry<K, V>[] entries, ImmutableMapEntry<K, V>[] table, int mask) {
      this.entries = entries;
      this.table = table;
      this.mask = mask;
   }

   static void checkNoConflictInKeyBucket(Object key, Entry<?, ?> entry, @Nullable ImmutableMapEntry<?, ?> keyBucketHead) {
      while (keyBucketHead != null) {
         checkNoConflict(!key.equals(keyBucketHead.getKey()), "key", entry, keyBucketHead);
         keyBucketHead = keyBucketHead.getNextInKeyBucket();
      }
   }

   @Override
   public V get(@Nullable Object key) {
      return get(key, this.table, this.mask);
   }

   @Nullable
   static <V> V get(@Nullable Object key, ImmutableMapEntry<?, V>[] keyTable, int mask) {
      if (key == null) {
         return null;
      } else {
         int index = Hashing.smear(key.hashCode()) & mask;

         for (ImmutableMapEntry<?, V> entry = keyTable[index]; entry != null; entry = entry.getNextInKeyBucket()) {
            Object candidateKey = entry.getKey();
            if (key.equals(candidateKey)) {
               return entry.getValue();
            }
         }

         return null;
      }
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);

      for (Entry<K, V> entry : this.entries) {
         action.accept(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public int size() {
      return this.entries.length;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries);
   }

   @Override
   ImmutableSet<K> createKeySet() {
      return new RegularImmutableMap.KeySet<>(this);
   }

   @Override
   ImmutableCollection<V> createValues() {
      return new RegularImmutableMap.Values<>(this);
   }

   @GwtCompatible(emulated = true)
   private static final class KeySet<K, V> extends ImmutableSet.Indexed<K> {
      @Weak
      private final RegularImmutableMap<K, V> map;

      KeySet(RegularImmutableMap<K, V> map) {
         this.map = map;
      }

      @Override
      K get(int index) {
         return this.map.entries[index].getKey();
      }

      @Override
      public boolean contains(Object object) {
         return this.map.containsKey(object);
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @Override
      public int size() {
         return this.map.size();
      }

      @GwtIncompatible
      @Override
      Object writeReplace() {
         return new RegularImmutableMap.KeySet.SerializedForm<>(this.map);
      }

      @GwtIncompatible
      private static class SerializedForm<K> implements Serializable {
         final ImmutableMap<K, ?> map;
         private static final long serialVersionUID = 0L;

         SerializedForm(ImmutableMap<K, ?> map) {
            this.map = map;
         }

         Object readResolve() {
            return this.map.keySet();
         }
      }
   }

   @GwtCompatible(emulated = true)
   private static final class Values<K, V> extends ImmutableList<V> {
      @Weak
      final RegularImmutableMap<K, V> map;

      Values(RegularImmutableMap<K, V> map) {
         this.map = map;
      }

      @Override
      public V get(int index) {
         return this.map.entries[index].getValue();
      }

      @Override
      public int size() {
         return this.map.size();
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @GwtIncompatible
      @Override
      Object writeReplace() {
         return new RegularImmutableMap.Values.SerializedForm<>(this.map);
      }

      @GwtIncompatible
      private static class SerializedForm<V> implements Serializable {
         final ImmutableMap<?, V> map;
         private static final long serialVersionUID = 0L;

         SerializedForm(ImmutableMap<?, V> map) {
            this.map = map;
         }

         Object readResolve() {
            return this.map.values();
         }
      }
   }
}
