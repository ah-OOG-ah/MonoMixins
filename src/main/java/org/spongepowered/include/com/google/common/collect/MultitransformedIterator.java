package org.spongepowered.include.com.google.common.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.spongepowered.include.com.google.common.base.Preconditions;

abstract class MultitransformedIterator<F, T> implements Iterator<T> {
   final Iterator<? extends F> backingIterator;
   private Iterator<? extends T> current = Iterators.emptyIterator();
   private Iterator<? extends T> removeFrom;

   MultitransformedIterator(Iterator<? extends F> backingIterator) {
      this.backingIterator = Preconditions.checkNotNull(backingIterator);
   }

   abstract Iterator<? extends T> transform(F var1);

   @Override
   public boolean hasNext() {
      Preconditions.checkNotNull(this.current);
      if (this.current.hasNext()) {
         return true;
      } else {
         while (this.backingIterator.hasNext()) {
            Preconditions.checkNotNull(this.current = this.transform((F)this.backingIterator.next()));
            if (this.current.hasNext()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.removeFrom = this.current;
         return (T)this.current.next();
      }
   }

   @Override
   public void remove() {
      CollectPreconditions.checkRemove(this.removeFrom != null);
      this.removeFrom.remove();
      this.removeFrom = null;
   }
}
