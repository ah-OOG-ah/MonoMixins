package org.spongepowered.libraries.com.google.common.collect;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Predicate;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(emulated = true)
public final class Iterables {
   private Iterables() {
   }

   public static <T> Iterable<T> unmodifiableIterable(Iterable<? extends T> iterable) {
      Preconditions.checkNotNull(iterable);
      return (Iterable<T>)(!(iterable instanceof Iterables.UnmodifiableIterable) && !(iterable instanceof ImmutableCollection)
         ? new Iterables.UnmodifiableIterable<>(iterable)
         : iterable);
   }

   @Deprecated
   public static <E> Iterable<E> unmodifiableIterable(ImmutableCollection<E> iterable) {
      return Preconditions.checkNotNull(iterable);
   }

   public static int size(Iterable<?> iterable) {
      return iterable instanceof Collection ? ((Collection)iterable).size() : Iterators.size(iterable.iterator());
   }

   public static boolean contains(Iterable<?> iterable, @Nullable Object element) {
      if (iterable instanceof Collection) {
         Collection<?> collection = (Collection<?>)iterable;
         return Collections2.safeContains(collection, element);
      } else {
         return Iterators.contains(iterable.iterator(), element);
      }
   }

   @CanIgnoreReturnValue
   public static boolean removeAll(Iterable<?> removeFrom, Collection<?> elementsToRemove) {
      return removeFrom instanceof Collection
         ? ((Collection)removeFrom).removeAll(Preconditions.checkNotNull(elementsToRemove))
         : Iterators.removeAll(removeFrom.iterator(), elementsToRemove);
   }

   @CanIgnoreReturnValue
   public static boolean retainAll(Iterable<?> removeFrom, Collection<?> elementsToRetain) {
      return removeFrom instanceof Collection
         ? ((Collection)removeFrom).retainAll(Preconditions.checkNotNull(elementsToRetain))
         : Iterators.retainAll(removeFrom.iterator(), elementsToRetain);
   }

   @CanIgnoreReturnValue
   public static <T> boolean removeIf(Iterable<T> removeFrom, Predicate<? super T> predicate) {
      return removeFrom instanceof Collection ? ((Collection)removeFrom).removeIf(predicate) : Iterators.removeIf(removeFrom.iterator(), predicate);
   }

   @Nullable
   static <T> T removeFirstMatching(Iterable<T> removeFrom, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate);
      Iterator<T> iterator = removeFrom.iterator();

      while (iterator.hasNext()) {
         T next = iterator.next();
         if (predicate.apply(next)) {
            iterator.remove();
            return next;
         }
      }

      return null;
   }

   public static boolean elementsEqual(Iterable<?> iterable1, Iterable<?> iterable2) {
      if (iterable1 instanceof Collection && iterable2 instanceof Collection) {
         Collection<?> collection1 = (Collection<?>)iterable1;
         Collection<?> collection2 = (Collection<?>)iterable2;
         if (collection1.size() != collection2.size()) {
            return false;
         }
      }

      return Iterators.elementsEqual(iterable1.iterator(), iterable2.iterator());
   }

   public static String toString(Iterable<?> iterable) {
      return Iterators.toString(iterable.iterator());
   }

   public static <T> T getOnlyElement(Iterable<T> iterable) {
      return Iterators.getOnlyElement(iterable.iterator());
   }

   @Nullable
   public static <T> T getOnlyElement(Iterable<? extends T> iterable, @Nullable T defaultValue) {
      return Iterators.getOnlyElement(iterable.iterator(), defaultValue);
   }

   @GwtIncompatible
   public static <T> T[] toArray(Iterable<? extends T> iterable, Class<T> type) {
      return toArray(iterable, ObjectArrays.newArray(type, 0));
   }

   static <T> T[] toArray(Iterable<? extends T> iterable, T[] array) {
      Collection<? extends T> collection = castOrCopyToCollection(iterable);
      return (T[])collection.toArray(array);
   }

   static Object[] toArray(Iterable<?> iterable) {
      return castOrCopyToCollection(iterable).toArray();
   }

   private static <E> Collection<E> castOrCopyToCollection(Iterable<E> iterable) {
      return (Collection<E>)(iterable instanceof Collection ? (Collection)iterable : Lists.newArrayList(iterable.iterator()));
   }

   @CanIgnoreReturnValue
   public static <T> boolean addAll(Collection<T> addTo, Iterable<? extends T> elementsToAdd) {
      if (elementsToAdd instanceof Collection) {
         Collection<? extends T> c = Collections2.cast(elementsToAdd);
         return addTo.addAll(c);
      } else {
         return Iterators.addAll(addTo, Preconditions.checkNotNull(elementsToAdd).iterator());
      }
   }

   public static int frequency(Iterable<?> iterable, @Nullable Object element) {
      if (iterable instanceof Multiset) {
         return ((Multiset)iterable).count(element);
      } else if (iterable instanceof Set) {
         return ((Set)iterable).contains(element) ? 1 : 0;
      } else {
         return Iterators.frequency(iterable.iterator(), element);
      }
   }

   public static <T> Iterable<T> cycle(final Iterable<T> iterable) {
      Preconditions.checkNotNull(iterable);
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.cycle(iterable);
         }

         @Override
         public Spliterator<T> spliterator() {
            return Stream.<Iterable<T>>generate(() -> iterable).flatMap(Streams::stream).spliterator();
         }

         @Override
         public String toString() {
            return iterable.toString() + " (cycled)";
         }
      };
   }

   public static <T> Iterable<T> cycle(T... elements) {
      return cycle(Lists.newArrayList(elements));
   }

   public static <T> Iterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b) {
      return FluentIterable.concat(a, b);
   }

   public static <T> Iterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b, Iterable<? extends T> c) {
      return FluentIterable.concat(a, b, c);
   }

   public static <T> Iterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b, Iterable<? extends T> c, Iterable<? extends T> d) {
      return FluentIterable.concat(a, b, c, d);
   }

   public static <T> Iterable<T> concat(Iterable<? extends T>... inputs) {
      return concat(ImmutableList.copyOf(inputs));
   }

   public static <T> Iterable<T> concat(Iterable<? extends Iterable<? extends T>> inputs) {
      return FluentIterable.concat(inputs);
   }

   public static <T> Iterable<List<T>> partition(final Iterable<T> iterable, final int size) {
      Preconditions.checkNotNull(iterable);
      Preconditions.checkArgument(size > 0);
      return new FluentIterable<List<T>>() {
         @Override
         public Iterator<List<T>> iterator() {
            return Iterators.partition(iterable.iterator(), size);
         }
      };
   }

   public static <T> Iterable<List<T>> paddedPartition(final Iterable<T> iterable, final int size) {
      Preconditions.checkNotNull(iterable);
      Preconditions.checkArgument(size > 0);
      return new FluentIterable<List<T>>() {
         @Override
         public Iterator<List<T>> iterator() {
            return Iterators.paddedPartition(iterable.iterator(), size);
         }
      };
   }

   public static <T> Iterable<T> filter(final Iterable<T> unfiltered, final Predicate<? super T> retainIfTrue) {
      Preconditions.checkNotNull(unfiltered);
      Preconditions.checkNotNull(retainIfTrue);
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.filter(unfiltered.iterator(), retainIfTrue);
         }

         @Override
         public void forEach(Consumer<? super T> action) {
            Preconditions.checkNotNull(action);
            unfiltered.forEach(a -> {
               if (retainIfTrue.test(a)) {
                  action.accept(a);
               }
            });
         }

         @Override
         public Spliterator<T> spliterator() {
            return CollectSpliterators.filter(unfiltered.spliterator(), retainIfTrue);
         }
      };
   }

   @GwtIncompatible
   public static <T> Iterable<T> filter(final Iterable<?> unfiltered, final Class<T> desiredType) {
      Preconditions.checkNotNull(unfiltered);
      Preconditions.checkNotNull(desiredType);
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.filter(unfiltered.iterator(), desiredType);
         }

         @Override
         public void forEach(Consumer<? super T> action) {
            Preconditions.checkNotNull(action);
            unfiltered.forEach(o -> {
               if (desiredType.isInstance(o)) {
                  action.accept(desiredType.cast(o));
               }
            });
         }

         @Override
         public Spliterator<T> spliterator() {
            return CollectSpliterators.filter((Spliterator<T>)unfiltered.spliterator(), desiredType::isInstance);
         }
      };
   }

   public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
      return Iterators.any(iterable.iterator(), predicate);
   }

   public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
      return Iterators.all(iterable.iterator(), predicate);
   }

   public static <T> T find(Iterable<T> iterable, Predicate<? super T> predicate) {
      return Iterators.find(iterable.iterator(), predicate);
   }

   @Nullable
   public static <T> T find(Iterable<? extends T> iterable, Predicate<? super T> predicate, @Nullable T defaultValue) {
      return Iterators.find(iterable.iterator(), predicate, defaultValue);
   }

   public static <T> Optional<T> tryFind(Iterable<T> iterable, Predicate<? super T> predicate) {
      return Iterators.tryFind(iterable.iterator(), predicate);
   }

   public static <T> int indexOf(Iterable<T> iterable, Predicate<? super T> predicate) {
      return Iterators.indexOf(iterable.iterator(), predicate);
   }

   public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable, final Function<? super F, ? extends T> function) {
      Preconditions.checkNotNull(fromIterable);
      Preconditions.checkNotNull(function);
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.transform(fromIterable.iterator(), function);
         }

         @Override
         public void forEach(Consumer<? super T> action) {
            Preconditions.checkNotNull(action);
            fromIterable.forEach(f -> action.accept((T)function.apply(f)));
         }

         @Override
         public Spliterator<T> spliterator() {
            return CollectSpliterators.map(fromIterable.spliterator(), function);
         }
      };
   }

   public static <T> T get(Iterable<T> iterable, int position) {
      Preconditions.checkNotNull(iterable);
      return (T)(iterable instanceof List ? ((List)iterable).get(position) : Iterators.get(iterable.iterator(), position));
   }

   @Nullable
   public static <T> T get(Iterable<? extends T> iterable, int position, @Nullable T defaultValue) {
      Preconditions.checkNotNull(iterable);
      Iterators.checkNonnegative(position);
      if (iterable instanceof List) {
         List<? extends T> list = Lists.cast(iterable);
         return (T)(position < list.size() ? list.get(position) : defaultValue);
      } else {
         Iterator<? extends T> iterator = iterable.iterator();
         Iterators.advance(iterator, position);
         return Iterators.getNext(iterator, defaultValue);
      }
   }

   @Nullable
   public static <T> T getFirst(Iterable<? extends T> iterable, @Nullable T defaultValue) {
      return Iterators.getNext(iterable.iterator(), defaultValue);
   }

   public static <T> T getLast(Iterable<T> iterable) {
      if (iterable instanceof List) {
         List<T> list = (List<T>)iterable;
         if (list.isEmpty()) {
            throw new NoSuchElementException();
         } else {
            return getLastInNonemptyList(list);
         }
      } else {
         return Iterators.getLast(iterable.iterator());
      }
   }

   @Nullable
   public static <T> T getLast(Iterable<? extends T> iterable, @Nullable T defaultValue) {
      if (iterable instanceof Collection) {
         Collection<? extends T> c = Collections2.cast(iterable);
         if (c.isEmpty()) {
            return defaultValue;
         }

         if (iterable instanceof List) {
            return getLastInNonemptyList(Lists.cast((Iterable<T>)iterable));
         }
      }

      return Iterators.getLast(iterable.iterator(), defaultValue);
   }

   private static <T> T getLastInNonemptyList(List<T> list) {
      return list.get(list.size() - 1);
   }

   public static <T> Iterable<T> skip(final Iterable<T> iterable, final int numberToSkip) {
      Preconditions.checkNotNull(iterable);
      Preconditions.checkArgument(numberToSkip >= 0, "number to skip cannot be negative");
      if (iterable instanceof List) {
         final List<T> list = (List<T>)iterable;
         return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
               int toSkip = Math.min(list.size(), numberToSkip);
               return list.subList(toSkip, list.size()).iterator();
            }
         };
      } else {
         return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
               final Iterator<T> iterator = iterable.iterator();
               Iterators.advance(iterator, numberToSkip);
               return new Iterator<T>() {
                  boolean atStart = true;

                  @Override
                  public boolean hasNext() {
                     return iterator.hasNext();
                  }

                  @Override
                  public T next() {
                     T result = iterator.next();
                     this.atStart = false;
                     return result;
                  }

                  @Override
                  public void remove() {
                     CollectPreconditions.checkRemove(!this.atStart);
                     iterator.remove();
                  }
               };
            }

            @Override
            public Spliterator<T> spliterator() {
               return Streams.stream(iterable).skip(numberToSkip).spliterator();
            }
         };
      }
   }

   public static <T> Iterable<T> limit(final Iterable<T> iterable, final int limitSize) {
      Preconditions.checkNotNull(iterable);
      Preconditions.checkArgument(limitSize >= 0, "limit is negative");
      return new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.limit(iterable.iterator(), limitSize);
         }

         @Override
         public Spliterator<T> spliterator() {
            return Streams.stream(iterable).limit(limitSize).spliterator();
         }
      };
   }

   public static <T> Iterable<T> consumingIterable(final Iterable<T> iterable) {
      if (iterable instanceof Queue) {
         return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
               return new ConsumingQueueIterator<>((Queue<T>)iterable);
            }

            @Override
            public String toString() {
               return "Iterables.consumingIterable(...)";
            }
         };
      } else {
         Preconditions.checkNotNull(iterable);
         return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
               return Iterators.consumingIterator(iterable.iterator());
            }

            @Override
            public String toString() {
               return "Iterables.consumingIterable(...)";
            }
         };
      }
   }

   public static boolean isEmpty(Iterable<?> iterable) {
      return iterable instanceof Collection ? ((Collection)iterable).isEmpty() : !iterable.iterator().hasNext();
   }

   @Beta
   public static <T> Iterable<T> mergeSorted(final Iterable<? extends Iterable<? extends T>> iterables, final Comparator<? super T> comparator) {
      Preconditions.checkNotNull(iterables, "iterables");
      Preconditions.checkNotNull(comparator, "comparator");
      Iterable<T> iterable = new FluentIterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return Iterators.mergeSorted(Iterables.transform(iterables, Iterables.toIterator()), comparator);
         }
      };
      return new Iterables.UnmodifiableIterable<>(iterable);
   }

   static <T> Function<Iterable<? extends T>, Iterator<? extends T>> toIterator() {
      return new Function<Iterable<? extends T>, Iterator<? extends T>>() {
         public Iterator<? extends T> apply(Iterable<? extends T> iterable) {
            return iterable.iterator();
         }
      };
   }

   private static final class UnmodifiableIterable<T> extends FluentIterable<T> {
      private final Iterable<? extends T> iterable;

      private UnmodifiableIterable(Iterable<? extends T> iterable) {
         this.iterable = iterable;
      }

      @Override
      public Iterator<T> iterator() {
         return Iterators.unmodifiableIterator(this.iterable.iterator());
      }

      @Override
      public void forEach(Consumer<? super T> action) {
         this.iterable.forEach(action);
      }

      @Override
      public Spliterator<T> spliterator() {
         return (Spliterator<T>)this.iterable.spliterator();
      }

      @Override
      public String toString() {
         return this.iterable.toString();
      }
   }
}
