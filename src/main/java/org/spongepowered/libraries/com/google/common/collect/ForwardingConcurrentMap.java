package org.spongepowered.libraries.com.google.common.collect;

import java.util.concurrent.ConcurrentMap;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public abstract class ForwardingConcurrentMap<K, V> extends ForwardingMap<K, V> implements ConcurrentMap<K, V> {
   protected ForwardingConcurrentMap() {
   }

   protected abstract ConcurrentMap<K, V> delegate();

   @CanIgnoreReturnValue
   @Override
   public V putIfAbsent(K key, V value) {
      return this.delegate().putIfAbsent(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(Object key, Object value) {
      return this.delegate().remove(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public V replace(K key, V value) {
      return this.delegate().replace(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return this.delegate().replace(key, oldValue, newValue);
   }
}
