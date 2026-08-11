package org.spongepowered.include.com.google.common.collect;

import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

final class ComparatorOrdering<T> extends Ordering<T> implements Serializable {
   final Comparator<T> comparator;

   ComparatorOrdering(Comparator<T> comparator) {
      this.comparator = Preconditions.checkNotNull(comparator);
   }

   @Override
   public int compare(T a, T b) {
      return this.comparator.compare(a, b);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else if (object instanceof ComparatorOrdering) {
         ComparatorOrdering<?> that = (ComparatorOrdering<?>)object;
         return this.comparator.equals(that.comparator);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.comparator.hashCode();
   }

   @Override
   public String toString() {
      return this.comparator.toString();
   }
}
