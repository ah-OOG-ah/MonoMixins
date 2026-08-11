package org.spongepowered.libraries.com.google.common.collect;

import java.util.NoSuchElementException;
import java.util.Queue;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public abstract class ForwardingQueue<E> extends ForwardingCollection<E> implements Queue<E> {
   protected ForwardingQueue() {
   }

   protected abstract Queue<E> delegate();

   @CanIgnoreReturnValue
   @Override
   public boolean offer(E o) {
      return this.delegate().offer(o);
   }

   @CanIgnoreReturnValue
   @Override
   public E poll() {
      return this.delegate().poll();
   }

   @CanIgnoreReturnValue
   @Override
   public E remove() {
      return this.delegate().remove();
   }

   @Override
   public E peek() {
      return this.delegate().peek();
   }

   @Override
   public E element() {
      return this.delegate().element();
   }

   protected boolean standardOffer(E e) {
      try {
         return this.add(e);
      } catch (IllegalStateException var3) {
         return false;
      }
   }

   protected E standardPeek() {
      try {
         return this.element();
      } catch (NoSuchElementException var2) {
         return null;
      }
   }

   protected E standardPoll() {
      try {
         return this.remove();
      } catch (NoSuchElementException var2) {
         return null;
      }
   }
}
