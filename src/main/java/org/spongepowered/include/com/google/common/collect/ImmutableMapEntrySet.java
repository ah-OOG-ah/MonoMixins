package org.spongepowered.include.com.google.common.collect;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.j2objc.annotations.Weak;

abstract class ImmutableMapEntrySet<K, V> extends ImmutableSet<Entry<K, V>> {
   abstract ImmutableMap<K, V> map();

   @Override
   public int size() {
      return this.map().size();
   }

   @Override
   public boolean contains(@Nullable Object object) {
      if (!(object instanceof Entry)) {
         return false;
      } else {
         Entry<?, ?> entry = (Entry<?, ?>)object;
         V value = this.map().get(entry.getKey());
         return value != null && value.equals(entry.getValue());
      }
   }

   @Override
   boolean isHashCodeFast() {
      return this.map().isHashCodeFast();
   }

   @Override
   public int hashCode() {
      return this.map().hashCode();
   }

   static final class RegularEntrySet<K, V> extends ImmutableMapEntrySet<K, V> {
      @Weak
      private final transient ImmutableMap<K, V> map;
      private final transient Entry<K, V>[] entries;

      RegularEntrySet(ImmutableMap<K, V> map, Entry<K, V>[] entries) {
         this.map = map;
         this.entries = entries;
      }

      @Override
      ImmutableMap<K, V> map() {
         return this.map;
      }

      @Override
      public UnmodifiableIterator<Entry<K, V>> iterator() {
         return Iterators.forArray(this.entries);
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         return Spliterators.spliterator(this.entries, 1297);
      }

      @Override
      public void forEach(Consumer<? super Entry<K, V>> action) {
         Preconditions.checkNotNull(action);

         for (Entry<K, V> entry : this.entries) {
            action.accept(entry);
         }
      }

      @Override
      ImmutableList<Entry<K, V>> createAsList() {
         return new RegularImmutableAsList<>(this, this.entries);
      }
   }
}
