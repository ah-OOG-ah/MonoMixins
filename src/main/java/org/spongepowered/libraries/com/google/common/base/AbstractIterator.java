package org.spongepowered.libraries.com.google.common.base;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
abstract class AbstractIterator<T> implements Iterator<T> {
   private AbstractIterator.State state = AbstractIterator.State.NOT_READY;
   private T next;

   protected AbstractIterator() {
   }

   protected abstract T computeNext();

   @Nullable
   @CanIgnoreReturnValue
   protected final T endOfData() {
      this.state = AbstractIterator.State.DONE;
      return null;
   }

   @Override
   public final boolean hasNext() {
      Preconditions.checkState(this.state != AbstractIterator.State.FAILED);
      switch (this.state) {
         case READY:
            return true;
         case DONE:
            return false;
         default:
            return this.tryToComputeNext();
      }
   }

   private boolean tryToComputeNext() {
      this.state = AbstractIterator.State.FAILED;
      this.next = this.computeNext();
      if (this.state != AbstractIterator.State.DONE) {
         this.state = AbstractIterator.State.READY;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public final T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.state = AbstractIterator.State.NOT_READY;
         T result = this.next;
         this.next = null;
         return result;
      }
   }

   @Override
   public final void remove() {
      throw new UnsupportedOperationException();
   }

   private static enum State {
      READY,
      NOT_READY,
      DONE,
      FAILED;
   }
}
