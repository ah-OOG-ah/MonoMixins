package org.spongepowered.libraries.com.google.common.collect;

import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@Beta
@GwtIncompatible
public interface RangeSet<C extends Comparable> {
   boolean contains(C var1);

   Range<C> rangeContaining(C var1);

   boolean intersects(Range<C> var1);

   boolean encloses(Range<C> var1);

   boolean enclosesAll(RangeSet<C> var1);

   default boolean enclosesAll(Iterable<Range<C>> other) {
      for (Range<C> range : other) {
         if (!this.encloses(range)) {
            return false;
         }
      }

      return true;
   }

   boolean isEmpty();

   Range<C> span();

   Set<Range<C>> asRanges();

   Set<Range<C>> asDescendingSetOfRanges();

   RangeSet<C> complement();

   RangeSet<C> subRangeSet(Range<C> var1);

   void add(Range<C> var1);

   void remove(Range<C> var1);

   void clear();

   void addAll(RangeSet<C> var1);

   default void addAll(Iterable<Range<C>> ranges) {
      for (Range<C> range : ranges) {
         this.add(range);
      }
   }

   void removeAll(RangeSet<C> var1);

   default void removeAll(Iterable<Range<C>> ranges) {
      for (Range<C> range : ranges) {
         this.remove(range);
      }
   }

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();

   @Override
   String toString();
}
