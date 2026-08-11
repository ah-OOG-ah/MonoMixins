package org.spongepowered.libraries.com.google.common.collect;

import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public abstract class ForwardingListMultimap<K, V> extends ForwardingMultimap<K, V> implements ListMultimap<K, V> {
   protected ForwardingListMultimap() {
   }

   protected abstract ListMultimap<K, V> delegate();

   @Override
   public List<V> get(@Nullable K key) {
      return this.delegate().get(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> removeAll(@Nullable Object key) {
      return this.delegate().removeAll(key);
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> replaceValues(K key, Iterable<? extends V> values) {
      return this.delegate().replaceValues(key, values);
   }
}
