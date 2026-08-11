package org.spongepowered.libraries.com.google.common.collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Joiner;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Predicate;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(emulated = true)
public abstract class FluentIterable<E> implements Iterable<E> {
   private final Optional<Iterable<E>> iterableDelegate;

   protected FluentIterable() {
      this.iterableDelegate = Optional.absent();
   }

   FluentIterable(Iterable<E> iterable) {
      Preconditions.checkNotNull(iterable);
      this.iterableDelegate = Optional.fromNullable(this != iterable ? iterable : null);
   }

   private Iterable<E> getDelegate() {
      return this.iterableDelegate.or(this);
   }

   public static <E> FluentIterable<E> from(final Iterable<E> iterable) {
      return iterable instanceof FluentIterable ? (FluentIterable)iterable : new FluentIterable<E>(iterable) {
         @Override
         public Iterator<E> iterator() {
            return iterable.iterator();
         }
      };
   }

   @Beta
   public static <E> FluentIterable<E> from(E[] elements) {
      return from(Arrays.asList(elements));
   }

   @Deprecated
   public static <E> FluentIterable<E> from(FluentIterable<E> iterable) {
      return Preconditions.checkNotNull(iterable);
   }

   @Beta
   public static <T> FluentIterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b) {
      return concat(ImmutableList.of(a, b));
   }

   @Beta
   public static <T> FluentIterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b, Iterable<? extends T> c) {
      return concat(ImmutableList.of(a, b, c));
   }

   @Beta
   public static <T> FluentIterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b, Iterable<? extends T> c, Iterable<? extends T> d) {
      return concat(ImmutableList.of(a, b, c, d));
   }

   @Beta
   public static <T> FluentIterable<T> concat(Iterable<? extends T>... inputs) {
      return concat(ImmutableList.copyOf(inputs));
   }

   @Beta
   public static <T> FluentIterable<T> concat(final Iterable<? extends Iterable<? extends T>> inputs) {
      Preconditions.checkNotNull(inputs);
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.concat(Iterables.transform(inputs, Iterables.toIterator()).iterator());
         }
      };
   }

   @Beta
   public static <E> FluentIterable<E> of() {
      return from(ImmutableList.of());
   }

   @Deprecated
   @Beta
   public static <E> FluentIterable<E> of(E[] elements) {
      return from(Lists.newArrayList(elements));
   }

   @Beta
   public static <E> FluentIterable<E> of(@Nullable E element, E... elements) {
      return from(Lists.asList(element, elements));
   }

   @Override
   public String toString() {
      return Iterables.toString(this.getDelegate());
   }

   public final int size() {
      return Iterables.size(this.getDelegate());
   }

   public final boolean contains(@Nullable Object target) {
      return Iterables.contains(this.getDelegate(), target);
   }

   public final FluentIterable<E> cycle() {
      return from(Iterables.cycle(this.getDelegate()));
   }

   @Beta
   public final FluentIterable<E> append(Iterable<? extends E> other) {
      return from(concat(this.getDelegate(), other));
   }

   @Beta
   public final FluentIterable<E> append(E... elements) {
      return from(concat(this.getDelegate(), Arrays.asList(elements)));
   }

   public final FluentIterable<E> filter(Predicate<? super E> predicate) {
      return from(Iterables.filter(this.getDelegate(), predicate));
   }

   @GwtIncompatible
   public final <T> FluentIterable<T> filter(Class<T> type) {
      return from(Iterables.filter(this.getDelegate(), type));
   }

   public final boolean anyMatch(Predicate<? super E> predicate) {
      return Iterables.any(this.getDelegate(), predicate);
   }

   public final boolean allMatch(Predicate<? super E> predicate) {
      return Iterables.all(this.getDelegate(), predicate);
   }

   public final Optional<E> firstMatch(Predicate<? super E> predicate) {
      return Iterables.tryFind(this.getDelegate(), predicate);
   }

   public final <T> FluentIterable<T> transform(Function<? super E, T> function) {
      return from(Iterables.transform(this.getDelegate(), function));
   }

   public <T> FluentIterable<T> transformAndConcat(Function<? super E, ? extends Iterable<? extends T>> function) {
      return from(concat(this.transform(function)));
   }

   public final Optional<E> first() {
      Iterator<E> iterator = this.getDelegate().iterator();
      return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.absent();
   }

   public final Optional<E> last() {
      Iterable<E> iterable = this.getDelegate();
      if (iterable instanceof List) {
         List<E> list = (List<E>)iterable;
         return list.isEmpty() ? Optional.absent() : Optional.of(list.get(list.size() - 1));
      } else {
         Iterator<E> iterator = iterable.iterator();
         if (!iterator.hasNext()) {
            return Optional.absent();
         } else if (iterable instanceof SortedSet) {
            SortedSet<E> sortedSet = (SortedSet<E>)iterable;
            return Optional.of(sortedSet.last());
         } else {
            E current;
            do {
               current = iterator.next();
            } while (iterator.hasNext());

            return Optional.of(current);
         }
      }
   }

   public final FluentIterable<E> skip(int numberToSkip) {
      return from(Iterables.skip(this.getDelegate(), numberToSkip));
   }

   public final FluentIterable<E> limit(int maxSize) {
      return from(Iterables.limit(this.getDelegate(), maxSize));
   }

   public final boolean isEmpty() {
      return !this.getDelegate().iterator().hasNext();
   }

   public final ImmutableList<E> toList() {
      return ImmutableList.copyOf(this.getDelegate());
   }

   public final ImmutableList<E> toSortedList(Comparator<? super E> comparator) {
      return Ordering.from(comparator).immutableSortedCopy(this.getDelegate());
   }

   public final ImmutableSet<E> toSet() {
      return ImmutableSet.copyOf(this.getDelegate());
   }

   public final ImmutableSortedSet<E> toSortedSet(Comparator<? super E> comparator) {
      return ImmutableSortedSet.copyOf(comparator, this.getDelegate());
   }

   public final ImmutableMultiset<E> toMultiset() {
      return ImmutableMultiset.copyOf(this.getDelegate());
   }

   public final <V> ImmutableMap<E, V> toMap(Function<? super E, V> valueFunction) {
      return Maps.toMap(this.getDelegate(), valueFunction);
   }

   public final <K> ImmutableListMultimap<K, E> index(Function<? super E, K> keyFunction) {
      return Multimaps.index(this.getDelegate(), keyFunction);
   }

   public final <K> ImmutableMap<K, E> uniqueIndex(Function<? super E, K> keyFunction) {
      return Maps.uniqueIndex(this.getDelegate(), keyFunction);
   }

   @GwtIncompatible
   public final E[] toArray(Class<E> type) {
      return Iterables.toArray(this.getDelegate(), type);
   }

   @CanIgnoreReturnValue
   public final <C extends Collection<? super E>> C copyInto(C collection) {
      Preconditions.checkNotNull(collection);
      Iterable<E> iterable = this.getDelegate();
      if (iterable instanceof Collection) {
         collection.addAll(Collections2.cast(iterable));
      } else {
         for (E item : iterable) {
            collection.add(item);
         }
      }

      return collection;
   }

   @Beta
   public final String join(Joiner joiner) {
      return joiner.join(this);
   }

   public final E get(int position) {
      return Iterables.get(this.getDelegate(), position);
   }

   public final Stream<E> stream() {
      return Streams.stream(this.getDelegate());
   }

   private static class FromIterableFunction<E> implements Function<Iterable<E>, FluentIterable<E>> {
      public FluentIterable<E> apply(Iterable<E> fromObject) {
         return FluentIterable.from(fromObject);
      }
   }
}
