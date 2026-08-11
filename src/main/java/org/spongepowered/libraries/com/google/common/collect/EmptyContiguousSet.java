package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@GwtCompatible(emulated = true)
final class EmptyContiguousSet<C extends Comparable> extends ContiguousSet<C> {
   EmptyContiguousSet(DiscreteDomain<C> domain) {
      super(domain);
   }

   public C first() {
      throw new NoSuchElementException();
   }

   public C last() {
      throw new NoSuchElementException();
   }

   @Override
   public int size() {
      return 0;
   }

   @Override
   public ContiguousSet<C> intersection(ContiguousSet<C> other) {
      return this;
   }

   @Override
   public Range<C> range() {
      throw new NoSuchElementException();
   }

   @Override
   public Range<C> range(BoundType lowerBoundType, BoundType upperBoundType) {
      throw new NoSuchElementException();
   }

   @Override
   ContiguousSet<C> headSetImpl(C toElement, boolean inclusive) {
      return this;
   }

   @Override
   ContiguousSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
      return this;
   }

   @Override
   ContiguousSet<C> tailSetImpl(C fromElement, boolean fromInclusive) {
      return this;
   }

   @Override
   public boolean contains(Object object) {
      return false;
   }

   @GwtIncompatible
   @Override
   int indexOf(Object target) {
      return -1;
   }

   @Override
   public UnmodifiableIterator<C> iterator() {
      return Iterators.emptyIterator();
   }

   @GwtIncompatible
   @Override
   public UnmodifiableIterator<C> descendingIterator() {
      return Iterators.emptyIterator();
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public ImmutableList<C> asList() {
      return ImmutableList.of();
   }

   @Override
   public String toString() {
      return "[]";
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object instanceof Set) {
         Set<?> that = (Set<?>)object;
         return that.isEmpty();
      } else {
         return false;
      }
   }

   @GwtIncompatible
   @Override
   boolean isHashCodeFast() {
      return true;
   }

   @Override
   public int hashCode() {
      return 0;
   }

   @GwtIncompatible
   @Override
   Object writeReplace() {
      return new EmptyContiguousSet.SerializedForm(this.domain);
   }

   @GwtIncompatible
   @Override
   ImmutableSortedSet<C> createDescendingSet() {
      return ImmutableSortedSet.emptySet(Ordering.natural().reverse());
   }

   @GwtIncompatible
   private static final class SerializedForm<C extends Comparable> implements Serializable {
      private final DiscreteDomain<C> domain;
      private static final long serialVersionUID = 0L;

      private SerializedForm(DiscreteDomain<C> domain) {
         this.domain = domain;
      }

      private Object readResolve() {
         return new EmptyContiguousSet<>(this.domain);
      }
   }
}
