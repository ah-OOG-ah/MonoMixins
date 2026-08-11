package org.spongepowered.include.com.google.common.collect;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.include.com.google.errorprone.annotations.concurrent.LazyInit;

public abstract class ImmutableSortedSet<E> extends ImmutableSortedSetFauxverideShim<E> implements NavigableSet<E>, SortedIterable<E> {
   final transient Comparator<? super E> comparator;
   @LazyInit
   transient ImmutableSortedSet<E> descendingSet;

   ImmutableSortedSet(Comparator<? super E> comparator) {
      this.comparator = comparator;
   }

   @Override
   public Comparator<? super E> comparator() {
      return this.comparator;
   }

   @Override
   public abstract UnmodifiableIterator<E> iterator();

   public ImmutableSortedSet<E> headSet(E toElement) {
      return this.headSet(toElement, false);
   }

   public ImmutableSortedSet<E> headSet(E toElement, boolean inclusive) {
      return this.headSetImpl(Preconditions.checkNotNull(toElement), inclusive);
   }

   public ImmutableSortedSet<E> subSet(E fromElement, E toElement) {
      return this.subSet(fromElement, true, toElement, false);
   }

   public ImmutableSortedSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      Preconditions.checkNotNull(fromElement);
      Preconditions.checkNotNull(toElement);
      Preconditions.checkArgument(this.comparator.compare(fromElement, toElement) <= 0);
      return this.subSetImpl(fromElement, fromInclusive, toElement, toInclusive);
   }

   public ImmutableSortedSet<E> tailSet(E fromElement) {
      return this.tailSet(fromElement, true);
   }

   public ImmutableSortedSet<E> tailSet(E fromElement, boolean inclusive) {
      return this.tailSetImpl(Preconditions.checkNotNull(fromElement), inclusive);
   }

   abstract ImmutableSortedSet<E> headSetImpl(E var1, boolean var2);

   abstract ImmutableSortedSet<E> subSetImpl(E var1, boolean var2, E var3, boolean var4);

   abstract ImmutableSortedSet<E> tailSetImpl(E var1, boolean var2);

   @Override
   public E lower(E e) {
      return Iterators.getNext(this.headSet(e, false).descendingIterator(), null);
   }

   @Override
   public E floor(E e) {
      return Iterators.getNext(this.headSet(e, true).descendingIterator(), null);
   }

   @Override
   public E ceiling(E e) {
      return Iterables.getFirst(this.tailSet(e, true), null);
   }

   @Override
   public E higher(E e) {
      return Iterables.getFirst(this.tailSet(e, false), null);
   }

   @Override
   public E first() {
      return this.iterator().next();
   }

   @Override
   public E last() {
      return this.descendingIterator().next();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final E pollFirst() {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final E pollLast() {
      throw new UnsupportedOperationException();
   }

   public ImmutableSortedSet<E> descendingSet() {
      ImmutableSortedSet<E> result = this.descendingSet;
      if (result == null) {
         result = this.descendingSet = this.createDescendingSet();
         result.descendingSet = this;
      }

      return result;
   }

   ImmutableSortedSet<E> createDescendingSet() {
      return new DescendingImmutableSortedSet<>(this);
   }

   @Override
   public Spliterator<E> spliterator() {
      return new AbstractSpliterator<E>(this.size(), 1365) {
         final UnmodifiableIterator<E> iterator = ImmutableSortedSet.this.iterator();

         @Override
         public boolean tryAdvance(Consumer<? super E> action) {
            if (this.iterator.hasNext()) {
               action.accept(this.iterator.next());
               return true;
            } else {
               return false;
            }
         }

         @Override
         public Comparator<? super E> getComparator() {
            return ImmutableSortedSet.this.comparator;
         }
      };
   }

   public abstract UnmodifiableIterator<E> descendingIterator();
}
