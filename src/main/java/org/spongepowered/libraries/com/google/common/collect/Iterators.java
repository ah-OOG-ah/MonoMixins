package org.spongepowered.libraries.com.google.common.collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Objects;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Predicate;
import org.spongepowered.libraries.com.google.common.base.Predicates;
import org.spongepowered.libraries.com.google.common.primitives.Ints;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(emulated = true)
public final class Iterators {
   static final UnmodifiableListIterator<Object> EMPTY_LIST_ITERATOR = new UnmodifiableListIterator<Object>() {
      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public Object next() {
         throw new NoSuchElementException();
      }

      @Override
      public boolean hasPrevious() {
         return false;
      }

      @Override
      public Object previous() {
         throw new NoSuchElementException();
      }

      @Override
      public int nextIndex() {
         return 0;
      }

      @Override
      public int previousIndex() {
         return -1;
      }
   };
   private static final Iterator<Object> EMPTY_MODIFIABLE_ITERATOR = new Iterator<Object>() {
      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public Object next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(false);
      }
   };

   private Iterators() {
   }

   static <T> UnmodifiableIterator<T> emptyIterator() {
      return emptyListIterator();
   }

   static <T> UnmodifiableListIterator<T> emptyListIterator() {
      return (UnmodifiableListIterator<T>)EMPTY_LIST_ITERATOR;
   }

   static <T> Iterator<T> emptyModifiableIterator() {
      return (Iterator<T>)EMPTY_MODIFIABLE_ITERATOR;
   }

   public static <T> UnmodifiableIterator<T> unmodifiableIterator(final Iterator<? extends T> iterator) {
      Preconditions.checkNotNull(iterator);
      return iterator instanceof UnmodifiableIterator ? (UnmodifiableIterator)iterator : new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @Override
         public T next() {
            return (T)iterator.next();
         }
      };
   }

   @Deprecated
   public static <T> UnmodifiableIterator<T> unmodifiableIterator(UnmodifiableIterator<T> iterator) {
      return Preconditions.checkNotNull(iterator);
   }

   public static int size(Iterator<?> iterator) {
      long count;
      for (count = 0L; iterator.hasNext(); count++) {
         iterator.next();
      }

      return Ints.saturatedCast(count);
   }

   public static boolean contains(Iterator<?> iterator, @Nullable Object element) {
      return any(iterator, Predicates.equalTo(element));
   }

   @CanIgnoreReturnValue
   public static boolean removeAll(Iterator<?> removeFrom, Collection<?> elementsToRemove) {
      return removeIf(removeFrom, Predicates.in(elementsToRemove));
   }

   @CanIgnoreReturnValue
   public static <T> boolean removeIf(Iterator<T> removeFrom, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate);
      boolean modified = false;

      while (removeFrom.hasNext()) {
         if (predicate.apply(removeFrom.next())) {
            removeFrom.remove();
            modified = true;
         }
      }

      return modified;
   }

   @CanIgnoreReturnValue
   public static boolean retainAll(Iterator<?> removeFrom, Collection<?> elementsToRetain) {
      return removeIf(removeFrom, Predicates.not(Predicates.in(elementsToRetain)));
   }

   public static boolean elementsEqual(Iterator<?> iterator1, Iterator<?> iterator2) {
      while (iterator1.hasNext()) {
         if (!iterator2.hasNext()) {
            return false;
         }

         Object o1 = iterator1.next();
         Object o2 = iterator2.next();
         if (!Objects.equal(o1, o2)) {
            return false;
         }
      }

      return !iterator2.hasNext();
   }

   public static String toString(Iterator<?> iterator) {
      return Collections2.STANDARD_JOINER.appendTo(new StringBuilder().append('['), iterator).append(']').toString();
   }

   @CanIgnoreReturnValue
   public static <T> T getOnlyElement(Iterator<T> iterator) {
      T first = iterator.next();
      if (!iterator.hasNext()) {
         return first;
      } else {
         StringBuilder sb = new StringBuilder().append("expected one element but was: <").append(first);

         for (int i = 0; i < 4 && iterator.hasNext(); i++) {
            sb.append(", ").append(iterator.next());
         }

         if (iterator.hasNext()) {
            sb.append(", ...");
         }

         sb.append('>');
         throw new IllegalArgumentException(sb.toString());
      }
   }

   @Nullable
   @CanIgnoreReturnValue
   public static <T> T getOnlyElement(Iterator<? extends T> iterator, @Nullable T defaultValue) {
      return iterator.hasNext() ? getOnlyElement((Iterator<T>)iterator) : defaultValue;
   }

   @GwtIncompatible
   public static <T> T[] toArray(Iterator<? extends T> iterator, Class<T> type) {
      List<T> list = Lists.newArrayList(iterator);
      return Iterables.toArray(list, type);
   }

   @CanIgnoreReturnValue
   public static <T> boolean addAll(Collection<T> addTo, Iterator<? extends T> iterator) {
      Preconditions.checkNotNull(addTo);
      Preconditions.checkNotNull(iterator);
      boolean wasModified = false;

      while (iterator.hasNext()) {
         wasModified |= addTo.add((T)iterator.next());
      }

      return wasModified;
   }

   public static int frequency(Iterator<?> iterator, @Nullable Object element) {
      return size(filter(iterator, Predicates.equalTo(element)));
   }

   public static <T> Iterator<T> cycle(final Iterable<T> iterable) {
      Preconditions.checkNotNull(iterable);
      return new Iterator<T>() {
         Iterator<T> iterator = Iterators.emptyModifiableIterator();

         @Override
         public boolean hasNext() {
            return this.iterator.hasNext() || iterable.iterator().hasNext();
         }

         @Override
         public T next() {
            if (!this.iterator.hasNext()) {
               this.iterator = iterable.iterator();
               if (!this.iterator.hasNext()) {
                  throw new NoSuchElementException();
               }
            }

            return this.iterator.next();
         }

         @Override
         public void remove() {
            this.iterator.remove();
         }
      };
   }

   @SafeVarargs
   public static <T> Iterator<T> cycle(T... elements) {
      return cycle(Lists.newArrayList(elements));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      return concat(new ConsumingQueueIterator<>(a, b));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b, Iterator<? extends T> c) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      Preconditions.checkNotNull(c);
      return concat(new ConsumingQueueIterator<>(a, b, c));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b, Iterator<? extends T> c, Iterator<? extends T> d) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      Preconditions.checkNotNull(c);
      Preconditions.checkNotNull(d);
      return concat(new ConsumingQueueIterator<>(a, b, c, d));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T>... inputs) {
      for (Iterator<? extends T> input : Preconditions.checkNotNull(inputs)) {
         Preconditions.checkNotNull(input);
      }

      return concat(new ConsumingQueueIterator<>(inputs));
   }

   public static <T> Iterator<T> concat(Iterator<? extends Iterator<? extends T>> inputs) {
      return new Iterators.ConcatenatedIterator<>(inputs);
   }

   public static <T> UnmodifiableIterator<List<T>> partition(Iterator<T> iterator, int size) {
      return partitionImpl(iterator, size, false);
   }

   public static <T> UnmodifiableIterator<List<T>> paddedPartition(Iterator<T> iterator, int size) {
      return partitionImpl(iterator, size, true);
   }

   private static <T> UnmodifiableIterator<List<T>> partitionImpl(final Iterator<T> iterator, final int size, final boolean pad) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(size > 0);
      return new UnmodifiableIterator<List<T>>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         public List<T> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               Object[] array = new Object[size];

               int count;
               for (count = 0; count < size && iterator.hasNext(); count++) {
                  array[count] = iterator.next();
               }

               for (int i = count; i < size; i++) {
                  array[i] = null;
               }

               List<T> list = Collections.unmodifiableList(Arrays.asList((T[])array));
               return !pad && count != size ? list.subList(0, count) : list;
            }
         }
      };
   }

   public static <T> UnmodifiableIterator<T> filter(final Iterator<T> unfiltered, final Predicate<? super T> retainIfTrue) {
      Preconditions.checkNotNull(unfiltered);
      Preconditions.checkNotNull(retainIfTrue);
      return new AbstractIterator<T>() {
         @Override
         protected T computeNext() {
            while (unfiltered.hasNext()) {
               T element = unfiltered.next();
               if (retainIfTrue.apply(element)) {
                  return element;
               }
            }

            return (T)this.endOfData();
         }
      };
   }

   @GwtIncompatible
   public static <T> UnmodifiableIterator<T> filter(Iterator<?> unfiltered, Class<T> desiredType) {
      return filter((Iterator<T>)unfiltered, Predicates.instanceOf(desiredType));
   }

   public static <T> boolean any(Iterator<T> iterator, Predicate<? super T> predicate) {
      return indexOf(iterator, predicate) != -1;
   }

   public static <T> boolean all(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate);

      while (iterator.hasNext()) {
         T element = iterator.next();
         if (!predicate.apply(element)) {
            return false;
         }
      }

      return true;
   }

   public static <T> T find(Iterator<T> iterator, Predicate<? super T> predicate) {
      return filter(iterator, predicate).next();
   }

   @Nullable
   public static <T> T find(Iterator<? extends T> iterator, Predicate<? super T> predicate, @Nullable T defaultValue) {
      return getNext(filter(iterator, predicate), defaultValue);
   }

   public static <T> Optional<T> tryFind(Iterator<T> iterator, Predicate<? super T> predicate) {
      UnmodifiableIterator<T> filteredIterator = filter(iterator, predicate);
      return filteredIterator.hasNext() ? Optional.of(filteredIterator.next()) : Optional.absent();
   }

   public static <T> int indexOf(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate, "predicate");

      for (int i = 0; iterator.hasNext(); i++) {
         T current = iterator.next();
         if (predicate.apply(current)) {
            return i;
         }
      }

      return -1;
   }

   public static <F, T> Iterator<T> transform(Iterator<F> fromIterator, final Function<? super F, ? extends T> function) {
      Preconditions.checkNotNull(function);
      return new TransformedIterator<F, T>(fromIterator) {
         @Override
         T transform(F from) {
            return (T)function.apply(from);
         }
      };
   }

   public static <T> T get(Iterator<T> iterator, int position) {
      checkNonnegative(position);
      int skipped = advance(iterator, position);
      if (!iterator.hasNext()) {
         throw new IndexOutOfBoundsException("position (" + position + ") must be less than the number of elements that remained (" + skipped + ")");
      } else {
         return iterator.next();
      }
   }

   static void checkNonnegative(int position) {
      if (position < 0) {
         throw new IndexOutOfBoundsException("position (" + position + ") must not be negative");
      }
   }

   @Nullable
   public static <T> T get(Iterator<? extends T> iterator, int position, @Nullable T defaultValue) {
      checkNonnegative(position);
      advance(iterator, position);
      return getNext(iterator, defaultValue);
   }

   @Nullable
   public static <T> T getNext(Iterator<? extends T> iterator, @Nullable T defaultValue) {
      return (T)(iterator.hasNext() ? iterator.next() : defaultValue);
   }

   public static <T> T getLast(Iterator<T> iterator) {
      T current;
      do {
         current = iterator.next();
      } while (iterator.hasNext());

      return current;
   }

   @Nullable
   public static <T> T getLast(Iterator<? extends T> iterator, @Nullable T defaultValue) {
      return iterator.hasNext() ? getLast((Iterator<T>)iterator) : defaultValue;
   }

   @CanIgnoreReturnValue
   public static int advance(Iterator<?> iterator, int numberToAdvance) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(numberToAdvance >= 0, "numberToAdvance must be nonnegative");

      int i;
      for (i = 0; i < numberToAdvance && iterator.hasNext(); i++) {
         iterator.next();
      }

      return i;
   }

   public static <T> Iterator<T> limit(final Iterator<T> iterator, final int limitSize) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(limitSize >= 0, "limit is negative");
      return new Iterator<T>() {
         private int count;

         @Override
         public boolean hasNext() {
            return this.count < limitSize && iterator.hasNext();
         }

         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               this.count++;
               return iterator.next();
            }
         }

         @Override
         public void remove() {
            iterator.remove();
         }
      };
   }

   public static <T> Iterator<T> consumingIterator(final Iterator<T> iterator) {
      Preconditions.checkNotNull(iterator);
      return new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @Override
         public T next() {
            T next = iterator.next();
            iterator.remove();
            return next;
         }

         @Override
         public String toString() {
            return "Iterators.consumingIterator(...)";
         }
      };
   }

   @Nullable
   static <T> T pollNext(Iterator<T> iterator) {
      if (iterator.hasNext()) {
         T result = iterator.next();
         iterator.remove();
         return result;
      } else {
         return null;
      }
   }

   static void clear(Iterator<?> iterator) {
      Preconditions.checkNotNull(iterator);

      while (iterator.hasNext()) {
         iterator.next();
         iterator.remove();
      }
   }

   @SafeVarargs
   public static <T> UnmodifiableIterator<T> forArray(T... array) {
      return forArray(array, 0, array.length, 0);
   }

   static <T> UnmodifiableListIterator<T> forArray(final T[] array, final int offset, int length, int index) {
      Preconditions.checkArgument(length >= 0);
      int end = offset + length;
      Preconditions.checkPositionIndexes(offset, end, array.length);
      Preconditions.checkPositionIndex(index, length);
      return (UnmodifiableListIterator<T>)(length == 0 ? emptyListIterator() : new AbstractIndexedListIterator<T>(length, index) {
         @Override
         protected T get(int index) {
            return array[offset + index];
         }
      });
   }

   public static <T> UnmodifiableIterator<T> singletonIterator(@Nullable final T value) {
      return new UnmodifiableIterator<T>() {
         boolean done;

         @Override
         public boolean hasNext() {
            return !this.done;
         }

         @Override
         public T next() {
            if (this.done) {
               throw new NoSuchElementException();
            } else {
               this.done = true;
               return value;
            }
         }
      };
   }

   public static <T> UnmodifiableIterator<T> forEnumeration(final Enumeration<T> enumeration) {
      Preconditions.checkNotNull(enumeration);
      return new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return enumeration.hasMoreElements();
         }

         @Override
         public T next() {
            return enumeration.nextElement();
         }
      };
   }

   public static <T> Enumeration<T> asEnumeration(final Iterator<T> iterator) {
      Preconditions.checkNotNull(iterator);
      return new Enumeration<T>() {
         @Override
         public boolean hasMoreElements() {
            return iterator.hasNext();
         }

         @Override
         public T nextElement() {
            return iterator.next();
         }
      };
   }

   public static <T> PeekingIterator<T> peekingIterator(Iterator<? extends T> iterator) {
      return iterator instanceof Iterators.PeekingImpl ? (Iterators.PeekingImpl)iterator : new Iterators.PeekingImpl<>(iterator);
   }

   @Deprecated
   public static <T> PeekingIterator<T> peekingIterator(PeekingIterator<T> iterator) {
      return Preconditions.checkNotNull(iterator);
   }

   @Beta
   public static <T> UnmodifiableIterator<T> mergeSorted(Iterable<? extends Iterator<? extends T>> iterators, Comparator<? super T> comparator) {
      Preconditions.checkNotNull(iterators, "iterators");
      Preconditions.checkNotNull(comparator, "comparator");
      return new Iterators.MergingIterator<>(iterators, comparator);
   }

   static <T> ListIterator<T> cast(Iterator<T> iterator) {
      return (ListIterator<T>)iterator;
   }

   private static class ConcatenatedIterator<T> extends MultitransformedIterator<Iterator<? extends T>, T> {
      public ConcatenatedIterator(Iterator<? extends Iterator<? extends T>> iterators) {
         super(getComponentIterators(iterators));
      }

      Iterator<? extends T> transform(Iterator<? extends T> iterator) {
         return iterator;
      }

      private static <T> Iterator<Iterator<? extends T>> getComponentIterators(Iterator<? extends Iterator<? extends T>> iterators) {
         return new MultitransformedIterator<Iterator<? extends T>, Iterator<? extends T>>(iterators) {
            Iterator<? extends Iterator<? extends T>> transform(Iterator<? extends T> iterator) {
               if (iterator instanceof Iterators.ConcatenatedIterator) {
                  Iterators.ConcatenatedIterator<? extends T> concatIterator = (Iterators.ConcatenatedIterator<? extends T>)iterator;
                  return Iterators.ConcatenatedIterator.getComponentIterators(concatIterator.backingIterator);
               } else {
                  return Iterators.singletonIterator(iterator);
               }
            }
         };
      }
   }

   private static class MergingIterator<T> extends UnmodifiableIterator<T> {
      final Queue<PeekingIterator<T>> queue;

      public MergingIterator(Iterable<? extends Iterator<? extends T>> iterators, final Comparator<? super T> itemComparator) {
         Comparator<PeekingIterator<T>> heapComparator = new Comparator<PeekingIterator<T>>() {
            public int compare(PeekingIterator<T> o1, PeekingIterator<T> o2) {
               return itemComparator.compare(o1.peek(), o2.peek());
            }
         };
         this.queue = new PriorityQueue<>(2, heapComparator);

         for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
               this.queue.add(Iterators.peekingIterator(iterator));
            }
         }
      }

      @Override
      public boolean hasNext() {
         return !this.queue.isEmpty();
      }

      @Override
      public T next() {
         PeekingIterator<T> nextIter = this.queue.remove();
         T next = nextIter.next();
         if (nextIter.hasNext()) {
            this.queue.add(nextIter);
         }

         return next;
      }
   }

   private static class PeekingImpl<E> implements PeekingIterator<E> {
      private final Iterator<? extends E> iterator;
      private boolean hasPeeked;
      private E peekedElement;

      public PeekingImpl(Iterator<? extends E> iterator) {
         this.iterator = Preconditions.checkNotNull(iterator);
      }

      @Override
      public boolean hasNext() {
         return this.hasPeeked || this.iterator.hasNext();
      }

      @Override
      public E next() {
         if (!this.hasPeeked) {
            return (E)this.iterator.next();
         } else {
            E result = this.peekedElement;
            this.hasPeeked = false;
            this.peekedElement = null;
            return result;
         }
      }

      @Override
      public void remove() {
         Preconditions.checkState(!this.hasPeeked, "Can't remove after you've peeked at next");
         this.iterator.remove();
      }

      @Override
      public E peek() {
         if (!this.hasPeeked) {
            this.peekedElement = (E)this.iterator.next();
            this.hasPeeked = true;
         }

         return this.peekedElement;
      }
   }
}
