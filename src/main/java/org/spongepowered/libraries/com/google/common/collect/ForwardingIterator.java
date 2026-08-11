package org.spongepowered.libraries.com.google.common.collect;

import java.util.Iterator;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public abstract class ForwardingIterator<T> extends ForwardingObject implements Iterator<T> {
   protected ForwardingIterator() {
   }

   protected abstract Iterator<T> delegate();

   @Override
   public boolean hasNext() {
      return this.delegate().hasNext();
   }

   @CanIgnoreReturnValue
   @Override
   public T next() {
      return this.delegate().next();
   }

   @Override
   public void remove() {
      this.delegate().remove();
   }
}
