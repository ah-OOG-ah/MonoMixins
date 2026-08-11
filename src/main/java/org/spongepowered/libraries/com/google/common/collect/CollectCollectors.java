package org.spongepowered.libraries.com.google.common.collect;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible
final class CollectCollectors {
   static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         ImmutableBiMap.Builder::new,
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableBiMap.Builder::combine,
         ImmutableBiMap.Builder::build
      );
   }

   static <E> Collector<E, ?, ImmutableList<E>> toImmutableList() {
      return Collector.of(ImmutableList::builder, ImmutableList.Builder::add, ImmutableList.Builder::combine, ImmutableList.Builder::build);
   }

   static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         ImmutableMap.Builder::new,
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableMap.Builder::combine,
         ImmutableMap.Builder::build
      );
   }

   static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
      return Collector.of(ImmutableSet::builder, ImmutableSet.Builder::add, ImmutableSet.Builder::combine, ImmutableSet.Builder::build);
   }

   static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
      Comparator<? super K> comparator, Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(comparator);
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         () -> new ImmutableSortedMap.Builder<>(comparator),
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableSortedMap.Builder::combine,
         ImmutableSortedMap.Builder::build,
         Characteristics.UNORDERED
      );
   }

   static <E> Collector<E, ?, ImmutableSortedSet<E>> toImmutableSortedSet(Comparator<? super E> comparator) {
      Preconditions.checkNotNull(comparator);
      return Collector.of(
         () -> new ImmutableSortedSet.Builder<>(comparator),
         ImmutableSortedSet.Builder::add,
         ImmutableSortedSet.Builder::combine,
         ImmutableSortedSet.Builder::build
      );
   }
}
