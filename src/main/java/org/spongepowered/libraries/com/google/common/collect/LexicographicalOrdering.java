package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(serializable = true)
final class LexicographicalOrdering<T> extends Ordering<Iterable<T>> implements Serializable {
   final Comparator<? super T> elementOrder;
   private static final long serialVersionUID = 0L;

   LexicographicalOrdering(Comparator<? super T> elementOrder) {
      this.elementOrder = elementOrder;
   }

   public int compare(Iterable<T> leftIterable, Iterable<T> rightIterable) {
      Iterator<T> left = leftIterable.iterator();
      Iterator<T> right = rightIterable.iterator();

      while (left.hasNext()) {
         if (!right.hasNext()) {
            return 1;
         }

         int result = this.elementOrder.compare(left.next(), right.next());
         if (result != 0) {
            return result;
         }
      }

      return right.hasNext() ? -1 : 0;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else if (object instanceof LexicographicalOrdering) {
         LexicographicalOrdering<?> that = (LexicographicalOrdering<?>)object;
         return this.elementOrder.equals(that.elementOrder);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.elementOrder.hashCode() ^ 2075626741;
   }

   @Override
   public String toString() {
      return this.elementOrder + ".lexicographical()";
   }
}
