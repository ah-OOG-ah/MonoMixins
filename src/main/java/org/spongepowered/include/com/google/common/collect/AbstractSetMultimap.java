package org.spongepowered.include.com.google.common.collect;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

abstract class AbstractSetMultimap<K, V> extends AbstractMapBasedMultimap<K, V> implements SetMultimap<K, V> {
   protected AbstractSetMultimap(Map<K, Collection<V>> map) {
      super(map);
   }

   abstract Set<V> createCollection();

   Set<V> createUnmodifiableEmptyCollection() {
      return ImmutableSet.of();
   }

   @Override
   public Set<V> get(@Nullable K key) {
      return (Set<V>)super.get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public Set<V> removeAll(@Nullable Object key) {
      return (Set<V>)super.removeAll(key);
   }

   @Override
   public Map<K, Collection<V>> asMap() {
      return super.asMap();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@Nullable K key, @Nullable V value) {
      return super.put(key, value);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return super.equals(object);
   }
}
