package org.spongepowered.include.com.google.common.collect;

import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.errorprone.annotations.concurrent.LazyInit;
import org.spongepowered.include.com.google.j2objc.annotations.RetainedWith;

class RegularImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
   static final RegularImmutableBiMap<Object, Object> EMPTY = new RegularImmutableBiMap<>(
      null, null, (Entry<Object, Object>[])ImmutableMap.EMPTY_ENTRY_ARRAY, 0, 0
   );
   private final transient ImmutableMapEntry<K, V>[] keyTable;
   private final transient ImmutableMapEntry<K, V>[] valueTable;
   private final transient Entry<K, V>[] entries;
   private final transient int mask;
   private final transient int hashCode;
   @LazyInit
   @RetainedWith
   private transient ImmutableBiMap<V, K> inverse;

   private RegularImmutableBiMap(ImmutableMapEntry<K, V>[] keyTable, ImmutableMapEntry<K, V>[] valueTable, Entry<K, V>[] entries, int mask, int hashCode) {
      this.keyTable = keyTable;
      this.valueTable = valueTable;
      this.entries = entries;
      this.mask = mask;
      this.hashCode = hashCode;
   }

   @Nullable
   @Override
   public V get(@Nullable Object key) {
      return this.keyTable == null ? null : RegularImmutableMap.get(key, this.keyTable, this.mask);
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return (ImmutableSet<Entry<K, V>>)(this.isEmpty() ? ImmutableSet.of() : new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries));
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);

      for (Entry<K, V> entry : this.entries) {
         action.accept(entry.getKey(), entry.getValue());
      }
   }

   @Override
   boolean isHashCodeFast() {
      return true;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   public int size() {
      return this.entries.length;
   }

   @Override
   public ImmutableBiMap<V, K> inverse() {
      if (this.isEmpty()) {
         return ImmutableBiMap.of();
      } else {
         ImmutableBiMap<V, K> result = this.inverse;
         return result == null ? (this.inverse = new RegularImmutableBiMap.Inverse()) : result;
      }
   }

   private final class Inverse extends ImmutableBiMap<V, K> {
      private Inverse() {
      }

      @Override
      public int size() {
         return this.inverse().size();
      }

      @Override
      public ImmutableBiMap<K, V> inverse() {
         return RegularImmutableBiMap.this;
      }

      @Override
      public void forEach(BiConsumer<? super V, ? super K> action) {
         Preconditions.checkNotNull(action);
         RegularImmutableBiMap.this.forEach((k, v) -> action.accept(v, k));
      }

      @Override
      public K get(@Nullable Object value) {
         if (value != null && RegularImmutableBiMap.this.valueTable != null) {
            int bucket = Hashing.smear(value.hashCode()) & RegularImmutableBiMap.this.mask;

            for (ImmutableMapEntry<K, V> entry = RegularImmutableBiMap.this.valueTable[bucket]; entry != null; entry = entry.getNextInValueBucket()) {
               if (value.equals(entry.getValue())) {
                  return entry.getKey();
               }
            }

            return null;
         } else {
            return null;
         }
      }

      @Override
      ImmutableSet<Entry<V, K>> createEntrySet() {
         return new RegularImmutableBiMap.Inverse.InverseEntrySet();
      }

      final class InverseEntrySet extends ImmutableMapEntrySet<V, K> {
         @Override
         ImmutableMap<V, K> map() {
            return Inverse.this;
         }

         @Override
         boolean isHashCodeFast() {
            return true;
         }

         @Override
         public int hashCode() {
            return RegularImmutableBiMap.this.hashCode;
         }

         @Override
         public UnmodifiableIterator<Entry<V, K>> iterator() {
            return (UnmodifiableIterator<Entry<V, K>>)this.asList().iterator();
         }

         @Override
         public void forEach(Consumer<? super Entry<V, K>> action) {
            this.asList().forEach((Consumer<? super Entry<K, V>>)action);
         }

         @Override
         ImmutableList<Entry<V, K>> createAsList() {
            return new ImmutableAsList<Entry<V, K>>() {
               public Entry<V, K> get(int index) {
                  Entry<K, V> entry = RegularImmutableBiMap.this.entries[index];
                  return Maps.immutableEntry(entry.getValue(), entry.getKey());
               }

               @Override
               ImmutableCollection<Entry<V, K>> delegateCollection() {
                  return InverseEntrySet.this;
               }
            };
         }
      }
   }
}
