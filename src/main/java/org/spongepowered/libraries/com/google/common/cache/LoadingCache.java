package org.spongepowered.libraries.com.google.common.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;

@GwtCompatible
public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V> {
   V get(K var1) throws ExecutionException;

   V getUnchecked(K var1);

   ImmutableMap<K, V> getAll(Iterable<? extends K> var1) throws ExecutionException;

   @Deprecated
   @Override
   V apply(K var1);

   void refresh(K var1);

   @Override
   ConcurrentMap<K, V> asMap();
}
