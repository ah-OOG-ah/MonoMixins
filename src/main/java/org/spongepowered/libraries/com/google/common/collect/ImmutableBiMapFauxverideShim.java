package org.spongepowered.libraries.com.google.common.collect;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@GwtIncompatible
abstract class ImmutableBiMapFauxverideShim<K, V> extends ImmutableMap<K, V> {
   @Deprecated
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      throw new UnsupportedOperationException();
   }
}
