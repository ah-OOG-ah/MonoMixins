package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.primitives.Ints;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;

@Beta
@GwtIncompatible
public final class ImmutableRangeSet<C extends Comparable> extends AbstractRangeSet<C> implements Serializable {
   private static final ImmutableRangeSet<Comparable<?>> EMPTY = new ImmutableRangeSet<>(ImmutableList.of());
   private static final ImmutableRangeSet<Comparable<?>> ALL = new ImmutableRangeSet<>(ImmutableList.of(Range.all()));
   private final transient ImmutableList<Range<C>> ranges;
   @LazyInit
   private transient ImmutableRangeSet<C> complement;

   public static <C extends Comparable> ImmutableRangeSet<C> of() {
      return EMPTY;
   }

   static <C extends Comparable> ImmutableRangeSet<C> all() {
      return ALL;
   }

   public static <C extends Comparable> ImmutableRangeSet<C> of(Range<C> range) {
      Preconditions.checkNotNull(range);
      if (range.isEmpty()) {
         return of();
      } else {
         return range.equals(Range.all()) ? all() : new ImmutableRangeSet<>(ImmutableList.of(range));
      }
   }

   public static <C extends Comparable> ImmutableRangeSet<C> copyOf(RangeSet<C> rangeSet) {
      Preconditions.checkNotNull(rangeSet);
      if (rangeSet.isEmpty()) {
         return of();
      } else if (rangeSet.encloses(Range.all())) {
         return all();
      } else {
         if (rangeSet instanceof ImmutableRangeSet) {
            ImmutableRangeSet<C> immutableRangeSet = (ImmutableRangeSet<C>)rangeSet;
            if (!immutableRangeSet.isPartialView()) {
               return immutableRangeSet;
            }
         }

         return new ImmutableRangeSet<>(ImmutableList.copyOf(rangeSet.asRanges()));
      }
   }

   public static <C extends Comparable<?>> ImmutableRangeSet<C> unionOf(Iterable<Range<C>> ranges) {
      return copyOf(TreeRangeSet.create(ranges));
   }

   public static <C extends Comparable<?>> ImmutableRangeSet<C> copyOf(Iterable<Range<C>> ranges) {
      return new ImmutableRangeSet.Builder<C>().addAll(ranges).build();
   }

   ImmutableRangeSet(ImmutableList<Range<C>> ranges) {
      this.ranges = ranges;
   }

   private ImmutableRangeSet(ImmutableList<Range<C>> ranges, ImmutableRangeSet<C> complement) {
      this.ranges = ranges;
      this.complement = complement;
   }

   @Override
   public boolean intersects(Range<C> otherRange) {
      int ceilingIndex = SortedLists.binarySearch(
         this.ranges,
         Range.lowerBoundFn(),
         otherRange.lowerBound,
         Ordering.natural(),
         SortedLists.KeyPresentBehavior.ANY_PRESENT,
         SortedLists.KeyAbsentBehavior.NEXT_HIGHER
      );
      return ceilingIndex < this.ranges.size()
            && this.ranges.get(ceilingIndex).isConnected(otherRange)
            && !this.ranges.get(ceilingIndex).intersection(otherRange).isEmpty()
         ? true
         : ceilingIndex > 0
            && this.ranges.get(ceilingIndex - 1).isConnected(otherRange)
            && !this.ranges.get(ceilingIndex - 1).intersection(otherRange).isEmpty();
   }

   @Override
   public boolean encloses(Range<C> otherRange) {
      int index = SortedLists.binarySearch(
         this.ranges,
         Range.lowerBoundFn(),
         otherRange.lowerBound,
         Ordering.natural(),
         SortedLists.KeyPresentBehavior.ANY_PRESENT,
         SortedLists.KeyAbsentBehavior.NEXT_LOWER
      );
      return index != -1 && this.ranges.get(index).encloses(otherRange);
   }

   @Override
   public Range<C> rangeContaining(C value) {
      int index = SortedLists.binarySearch(
         this.ranges,
         Range.lowerBoundFn(),
         Cut.belowValue(value),
         Ordering.natural(),
         SortedLists.KeyPresentBehavior.ANY_PRESENT,
         SortedLists.KeyAbsentBehavior.NEXT_LOWER
      );
      if (index != -1) {
         Range<C> range = this.ranges.get(index);
         return range.contains(value) ? range : null;
      } else {
         return null;
      }
   }

   @Override
   public Range<C> span() {
      if (this.ranges.isEmpty()) {
         throw new NoSuchElementException();
      } else {
         return Range.create(this.ranges.get(0).lowerBound, this.ranges.get(this.ranges.size() - 1).upperBound);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.ranges.isEmpty();
   }

   @Deprecated
   @Override
   public void add(Range<C> range) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void addAll(RangeSet<C> other) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void addAll(Iterable<Range<C>> other) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void remove(Range<C> range) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void removeAll(RangeSet<C> other) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void removeAll(Iterable<Range<C>> other) {
      throw new UnsupportedOperationException();
   }

   public ImmutableSet<Range<C>> asRanges() {
      return (ImmutableSet<Range<C>>)(this.ranges.isEmpty() ? ImmutableSet.of() : new RegularImmutableSortedSet<>(this.ranges, Range.RANGE_LEX_ORDERING));
   }

   public ImmutableSet<Range<C>> asDescendingSetOfRanges() {
      return (ImmutableSet<Range<C>>)(this.ranges.isEmpty()
         ? ImmutableSet.of()
         : new RegularImmutableSortedSet<>(this.ranges.reverse(), Range.RANGE_LEX_ORDERING.reverse()));
   }

   public ImmutableRangeSet<C> complement() {
      ImmutableRangeSet<C> result = this.complement;
      if (result != null) {
         return result;
      } else if (this.ranges.isEmpty()) {
         return this.complement = all();
      } else if (this.ranges.size() == 1 && this.ranges.get(0).equals(Range.all())) {
         return this.complement = of();
      } else {
         ImmutableList<Range<C>> complementRanges = new ImmutableRangeSet.ComplementRanges();
         return this.complement = new ImmutableRangeSet<>(complementRanges, this);
      }
   }

   public ImmutableRangeSet<C> union(RangeSet<C> other) {
      return unionOf(Iterables.concat(this.asRanges(), other.asRanges()));
   }

   public ImmutableRangeSet<C> intersection(RangeSet<C> other) {
      RangeSet<C> copy = TreeRangeSet.create(this);
      copy.removeAll(other.complement());
      return copyOf(copy);
   }

   public ImmutableRangeSet<C> difference(RangeSet<C> other) {
      RangeSet<C> copy = TreeRangeSet.create(this);
      copy.removeAll(other);
      return copyOf(copy);
   }

   private ImmutableList<Range<C>> intersectRanges(final Range<C> range) {
      if (this.ranges.isEmpty() || range.isEmpty()) {
         return ImmutableList.of();
      } else if (range.encloses(this.span())) {
         return this.ranges;
      } else {
         final int fromIndex;
         if (range.hasLowerBound()) {
            fromIndex = SortedLists.binarySearch(
               this.ranges, Range.upperBoundFn(), range.lowerBound, SortedLists.KeyPresentBehavior.FIRST_AFTER, SortedLists.KeyAbsentBehavior.NEXT_HIGHER
            );
         } else {
            fromIndex = 0;
         }

         int toIndex;
         if (range.hasUpperBound()) {
            toIndex = SortedLists.binarySearch(
               this.ranges, Range.lowerBoundFn(), range.upperBound, SortedLists.KeyPresentBehavior.FIRST_PRESENT, SortedLists.KeyAbsentBehavior.NEXT_HIGHER
            );
         } else {
            toIndex = this.ranges.size();
         }

         final int length = toIndex - fromIndex;
         return length == 0
            ? ImmutableList.of()
            : new ImmutableList<Range<C>>() {
               @Override
               public int size() {
                  return length;
               }

               public Range<C> get(int index) {
                  Preconditions.checkElementIndex(index, length);
                  return index != 0 && index != length - 1
                     ? ImmutableRangeSet.this.ranges.get(index + fromIndex)
                     : ImmutableRangeSet.this.ranges.get(index + fromIndex).intersection(range);
               }

               @Override
               boolean isPartialView() {
                  return true;
               }
            };
      }
   }

   public ImmutableRangeSet<C> subRangeSet(Range<C> range) {
      if (!this.isEmpty()) {
         Range<C> span = this.span();
         if (range.encloses(span)) {
            return this;
         }

         if (range.isConnected(span)) {
            return new ImmutableRangeSet<>(this.intersectRanges(range));
         }
      }

      return of();
   }

   public ImmutableSortedSet<C> asSet(DiscreteDomain<C> domain) {
      Preconditions.checkNotNull(domain);
      if (this.isEmpty()) {
         return ImmutableSortedSet.of();
      } else {
         Range<C> span = this.span().canonical(domain);
         if (!span.hasLowerBound()) {
            throw new IllegalArgumentException("Neither the DiscreteDomain nor this range set are bounded below");
         } else {
            if (!span.hasUpperBound()) {
               try {
                  domain.maxValue();
               } catch (NoSuchElementException var4) {
                  throw new IllegalArgumentException("Neither the DiscreteDomain nor this range set are bounded above");
               }
            }

            return new ImmutableRangeSet.AsSet(domain);
         }
      }
   }

   boolean isPartialView() {
      return this.ranges.isPartialView();
   }

   public static <C extends Comparable<?>> ImmutableRangeSet.Builder<C> builder() {
      return new ImmutableRangeSet.Builder<>();
   }

   Object writeReplace() {
      return new ImmutableRangeSet.SerializedForm<>(this.ranges);
   }

   private final class AsSet extends ImmutableSortedSet<C> {
      private final DiscreteDomain<C> domain;
      private transient Integer size;

      AsSet(DiscreteDomain<C> domain) {
         super(Ordering.natural());
         this.domain = domain;
      }

      @Override
      public int size() {
         Integer result = this.size;
         if (result == null) {
            long total = 0L;

            for (Range<C> range : ImmutableRangeSet.this.ranges) {
               total += ContiguousSet.create(range, this.domain).size();
               if (total >= 2147483647L) {
                  break;
               }
            }

            result = this.size = Ints.saturatedCast(total);
         }

         return result;
      }

      @Override
      public UnmodifiableIterator<C> iterator() {
         return new AbstractIterator<C>() {
            final Iterator<Range<C>> rangeItr = ImmutableRangeSet.this.ranges.iterator();
            Iterator<C> elemItr = Iterators.emptyIterator();

            protected C computeNext() {
               while (!this.elemItr.hasNext()) {
                  if (!this.rangeItr.hasNext()) {
                     return (C)this.endOfData();
                  }

                  this.elemItr = ContiguousSet.create(this.rangeItr.next(), AsSet.this.domain).iterator();
               }

               return this.elemItr.next();
            }
         };
      }

      @GwtIncompatible("NavigableSet")
      @Override
      public UnmodifiableIterator<C> descendingIterator() {
         return new AbstractIterator<C>() {
            final Iterator<Range<C>> rangeItr = ImmutableRangeSet.this.ranges.reverse().iterator();
            Iterator<C> elemItr = Iterators.emptyIterator();

            protected C computeNext() {
               while (!this.elemItr.hasNext()) {
                  if (!this.rangeItr.hasNext()) {
                     return (C)this.endOfData();
                  }

                  this.elemItr = ContiguousSet.create(this.rangeItr.next(), AsSet.this.domain).descendingIterator();
               }

               return this.elemItr.next();
            }
         };
      }

      ImmutableSortedSet<C> subSet(Range<C> range) {
         return ImmutableRangeSet.this.subRangeSet(range).asSet(this.domain);
      }

      ImmutableSortedSet<C> headSetImpl(C toElement, boolean inclusive) {
         return this.subSet(Range.upTo(toElement, BoundType.forBoolean(inclusive)));
      }

      ImmutableSortedSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
         return !fromInclusive && !toInclusive && Range.compareOrThrow(fromElement, toElement) == 0
            ? ImmutableSortedSet.of()
            : this.subSet(Range.range(fromElement, BoundType.forBoolean(fromInclusive), toElement, BoundType.forBoolean(toInclusive)));
      }

      ImmutableSortedSet<C> tailSetImpl(C fromElement, boolean inclusive) {
         return this.subSet(Range.downTo(fromElement, BoundType.forBoolean(inclusive)));
      }

      @Override
      public boolean contains(@Nullable Object o) {
         if (o == null) {
            return false;
         } else {
            try {
               C c = (C)o;
               return ImmutableRangeSet.this.contains(c);
            } catch (ClassCastException var3) {
               return false;
            }
         }
      }

      @Override
      int indexOf(Object target) {
         if (this.contains(target)) {
            C c = (C)target;
            long total = 0L;

            for (Range<C> range : ImmutableRangeSet.this.ranges) {
               if (range.contains(c)) {
                  return Ints.saturatedCast(total + ContiguousSet.create(range, this.domain).indexOf(c));
               }

               total += ContiguousSet.create(range, this.domain).size();
            }

            throw new AssertionError("impossible");
         } else {
            return -1;
         }
      }

      @Override
      boolean isPartialView() {
         return ImmutableRangeSet.this.ranges.isPartialView();
      }

      @Override
      public String toString() {
         return ImmutableRangeSet.this.ranges.toString();
      }

      @Override
      Object writeReplace() {
         return new ImmutableRangeSet.AsSetSerializedForm<>(ImmutableRangeSet.this.ranges, this.domain);
      }
   }

   private static class AsSetSerializedForm<C extends Comparable> implements Serializable {
      private final ImmutableList<Range<C>> ranges;
      private final DiscreteDomain<C> domain;

      AsSetSerializedForm(ImmutableList<Range<C>> ranges, DiscreteDomain<C> domain) {
         this.ranges = ranges;
         this.domain = domain;
      }

      Object readResolve() {
         return new ImmutableRangeSet<>(this.ranges).asSet(this.domain);
      }
   }

   public static class Builder<C extends Comparable<?>> {
      private final List<Range<C>> ranges = Lists.newArrayList();

      @CanIgnoreReturnValue
      public ImmutableRangeSet.Builder<C> add(Range<C> range) {
         Preconditions.checkArgument(!range.isEmpty(), "range must not be empty, but was %s", range);
         this.ranges.add(range);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableRangeSet.Builder<C> addAll(RangeSet<C> ranges) {
         return this.addAll(ranges.asRanges());
      }

      @CanIgnoreReturnValue
      public ImmutableRangeSet.Builder<C> addAll(Iterable<Range<C>> ranges) {
         for (Range<C> range : ranges) {
            this.add(range);
         }

         return this;
      }

      public ImmutableRangeSet<C> build() {
         ImmutableList.Builder<Range<C>> mergedRangesBuilder = new ImmutableList.Builder<>(this.ranges.size());
         Collections.sort(this.ranges, Range.RANGE_LEX_ORDERING);
         PeekingIterator<Range<C>> peekingItr = Iterators.peekingIterator(this.ranges.iterator());

         while (peekingItr.hasNext()) {
            Range<C> range;
            for (range = peekingItr.next(); peekingItr.hasNext(); range = range.span(peekingItr.next())) {
               Range<C> nextRange = peekingItr.peek();
               if (!range.isConnected(nextRange)) {
                  break;
               }

               Preconditions.checkArgument(
                  range.intersection(nextRange).isEmpty(), "Overlapping ranges not permitted but found %s overlapping %s", range, nextRange
               );
            }

            mergedRangesBuilder.add(range);
         }

         ImmutableList<Range<C>> mergedRanges = mergedRangesBuilder.build();
         if (mergedRanges.isEmpty()) {
            return ImmutableRangeSet.of();
         } else {
            return mergedRanges.size() == 1 && Iterables.getOnlyElement(mergedRanges).equals(Range.all())
               ? ImmutableRangeSet.all()
               : new ImmutableRangeSet<>(mergedRanges);
         }
      }
   }

   private final class ComplementRanges extends ImmutableList<Range<C>> {
      private final boolean positiveBoundedBelow = ImmutableRangeSet.this.ranges.get(0).hasLowerBound();
      private final boolean positiveBoundedAbove = Iterables.getLast(ImmutableRangeSet.this.ranges).hasUpperBound();
      private final int size;

      ComplementRanges() {
         int size = ImmutableRangeSet.this.ranges.size() - 1;
         if (this.positiveBoundedBelow) {
            size++;
         }

         if (this.positiveBoundedAbove) {
            size++;
         }

         this.size = size;
      }

      @Override
      public int size() {
         return this.size;
      }

      public Range<C> get(int index) {
         Preconditions.checkElementIndex(index, this.size);
         Cut<C> lowerBound;
         if (this.positiveBoundedBelow) {
            lowerBound = index == 0 ? Cut.belowAll() : ImmutableRangeSet.this.ranges.get(index - 1).upperBound;
         } else {
            lowerBound = ImmutableRangeSet.this.ranges.get(index).upperBound;
         }

         Cut<C> upperBound;
         if (this.positiveBoundedAbove && index == this.size - 1) {
            upperBound = Cut.aboveAll();
         } else {
            upperBound = ImmutableRangeSet.this.ranges.get(index + (this.positiveBoundedBelow ? 0 : 1)).lowerBound;
         }

         return Range.create(lowerBound, upperBound);
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }

   private static final class SerializedForm<C extends Comparable> implements Serializable {
      private final ImmutableList<Range<C>> ranges;

      SerializedForm(ImmutableList<Range<C>> ranges) {
         this.ranges = ranges;
      }

      Object readResolve() {
         if (this.ranges.isEmpty()) {
            return ImmutableRangeSet.of();
         } else {
            return this.ranges.equals(ImmutableList.of(Range.all())) ? ImmutableRangeSet.all() : new ImmutableRangeSet<>(this.ranges);
         }
      }
   }
}
