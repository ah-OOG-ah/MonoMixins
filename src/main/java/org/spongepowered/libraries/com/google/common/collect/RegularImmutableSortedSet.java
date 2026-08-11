package org.spongepowered.libraries.com.google.common.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableSortedSet<E> extends ImmutableSortedSet<E> {
   static final RegularImmutableSortedSet<Comparable> NATURAL_EMPTY_SET = new RegularImmutableSortedSet<>(ImmutableList.of(), Ordering.natural());
   private final transient ImmutableList<E> elements;

   RegularImmutableSortedSet(ImmutableList<E> elements, Comparator<? super E> comparator) {
      super(comparator);
      this.elements = elements;
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return this.elements.iterator();
   }

   @GwtIncompatible
   @Override
   public UnmodifiableIterator<E> descendingIterator() {
      return this.elements.reverse().iterator();
   }

   @Override
   public Spliterator<E> spliterator() {
      return this.asList().spliterator();
   }

   @Override
   public void forEach(Consumer<? super E> action) {
      this.elements.forEach(action);
   }

   @Override
   public int size() {
      return this.elements.size();
   }

   @Override
   public boolean contains(@Nullable Object o) {
      try {
         return o != null && this.unsafeBinarySearch(o) >= 0;
      } catch (ClassCastException var3) {
         return false;
      }
   }

   @Override
   public boolean containsAll(Collection<?> targets) {
      if (targets instanceof Multiset) {
         targets = ((Multiset)targets).elementSet();
      }

      if (SortedIterables.hasSameComparator(this.comparator(), targets) && targets.size() > 1) {
         PeekingIterator<E> thisIterator = Iterators.peekingIterator(this.iterator());
         Iterator<?> thatIterator = targets.iterator();
         Object target = thatIterator.next();

         try {
            while (thisIterator.hasNext()) {
               int cmp = this.unsafeCompare(thisIterator.peek(), target);
               if (cmp < 0) {
                  thisIterator.next();
               } else if (cmp == 0) {
                  if (!thatIterator.hasNext()) {
                     return true;
                  }

                  target = thatIterator.next();
               } else if (cmp > 0) {
                  return false;
               }
            }

            return false;
         } catch (NullPointerException var6) {
            return false;
         } catch (ClassCastException var7) {
            return false;
         }
      } else {
         return super.containsAll(targets);
      }
   }

   private int unsafeBinarySearch(Object key) throws ClassCastException {
      return Collections.binarySearch(this.elements, (E)key, this.unsafeComparator());
   }

   @Override
   boolean isPartialView() {
      return this.elements.isPartialView();
   }

   @Override
   int copyIntoArray(Object[] dst, int offset) {
      return this.elements.copyIntoArray(dst, offset);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof Set)) {
         return false;
      } else {
         Set<?> that = (Set<?>)object;
         if (this.size() != that.size()) {
            return false;
         } else if (this.isEmpty()) {
            return true;
         } else if (SortedIterables.hasSameComparator(this.comparator, that)) {
            Iterator<?> otherIterator = that.iterator();

            try {
               for (Object element : this) {
                  Object otherElement = otherIterator.next();
                  if (otherElement == null || this.unsafeCompare(element, otherElement) != 0) {
                     return false;
                  }
               }

               return true;
            } catch (ClassCastException var7) {
               return false;
            } catch (NoSuchElementException var8) {
               return false;
            }
         } else {
            return this.containsAll(that);
         }
      }
   }

   @Override
   public E first() {
      if (this.isEmpty()) {
         throw new NoSuchElementException();
      } else {
         return this.elements.get(0);
      }
   }

   @Override
   public E last() {
      if (this.isEmpty()) {
         throw new NoSuchElementException();
      } else {
         return this.elements.get(this.size() - 1);
      }
   }

   @Override
   public E lower(E element) {
      int index = this.headIndex(element, false) - 1;
      return index == -1 ? null : this.elements.get(index);
   }

   @Override
   public E floor(E element) {
      int index = this.headIndex(element, true) - 1;
      return index == -1 ? null : this.elements.get(index);
   }

   @Override
   public E ceiling(E element) {
      int index = this.tailIndex(element, true);
      return index == this.size() ? null : this.elements.get(index);
   }

   @Override
   public E higher(E element) {
      int index = this.tailIndex(element, false);
      return index == this.size() ? null : this.elements.get(index);
   }

   @Override
   ImmutableSortedSet<E> headSetImpl(E toElement, boolean inclusive) {
      return this.getSubSet(0, this.headIndex(toElement, inclusive));
   }

   int headIndex(E toElement, boolean inclusive) {
      return SortedLists.binarySearch(
         this.elements,
         Preconditions.checkNotNull(toElement),
         this.comparator(),
         inclusive ? SortedLists.KeyPresentBehavior.FIRST_AFTER : SortedLists.KeyPresentBehavior.FIRST_PRESENT,
         SortedLists.KeyAbsentBehavior.NEXT_HIGHER
      );
   }

   @Override
   ImmutableSortedSet<E> subSetImpl(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
      return this.tailSetImpl(fromElement, fromInclusive).headSetImpl(toElement, toInclusive);
   }

   @Override
   ImmutableSortedSet<E> tailSetImpl(E fromElement, boolean inclusive) {
      return this.getSubSet(this.tailIndex(fromElement, inclusive), this.size());
   }

   int tailIndex(E fromElement, boolean inclusive) {
      return SortedLists.binarySearch(
         this.elements,
         Preconditions.checkNotNull(fromElement),
         this.comparator(),
         inclusive ? SortedLists.KeyPresentBehavior.FIRST_PRESENT : SortedLists.KeyPresentBehavior.FIRST_AFTER,
         SortedLists.KeyAbsentBehavior.NEXT_HIGHER
      );
   }

   Comparator<Object> unsafeComparator() {
      return this.comparator;
   }

   RegularImmutableSortedSet<E> getSubSet(int newFromIndex, int newToIndex) {
      if (newFromIndex == 0 && newToIndex == this.size()) {
         return this;
      } else {
         return newFromIndex < newToIndex
            ? new RegularImmutableSortedSet<>(this.elements.subList(newFromIndex, newToIndex), this.comparator)
            : emptySet(this.comparator);
      }
   }

   @Override
   int indexOf(@Nullable Object target) {
      if (target == null) {
         return -1;
      } else {
         int position;
         try {
            position = SortedLists.binarySearch(
               this.elements,
               (E)target,
               this.unsafeComparator(),
               SortedLists.KeyPresentBehavior.ANY_PRESENT,
               SortedLists.KeyAbsentBehavior.INVERTED_INSERTION_INDEX
            );
         } catch (ClassCastException var4) {
            return -1;
         }

         return position >= 0 ? position : -1;
      }
   }

   @Override
   ImmutableList<E> createAsList() {
      return (ImmutableList<E>)(this.size() <= 1 ? this.elements : new ImmutableSortedAsList<>(this, this.elements));
   }

   @Override
   ImmutableSortedSet<E> createDescendingSet() {
      Ordering<E> reversedOrder = Ordering.from(this.comparator).reverse();
      return this.isEmpty() ? emptySet(reversedOrder) : new RegularImmutableSortedSet<>(this.elements.reverse(), reversedOrder);
   }
}
