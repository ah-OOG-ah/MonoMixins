package org.spongepowered.include.com.google.common.collect;

import java.util.Collection;
import java.util.Iterator;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public abstract class ForwardingCollection<E> extends ForwardingObject implements Collection<E> {
   protected ForwardingCollection() {
   }

   protected abstract Collection<E> delegate();

   @Override
   public Iterator<E> iterator() {
      return this.delegate().iterator();
   }

   @Override
   public int size() {
      return this.delegate().size();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean removeAll(Collection<?> collection) {
      return this.delegate().removeAll(collection);
   }

   @Override
   public boolean isEmpty() {
      return this.delegate().isEmpty();
   }

   @Override
   public boolean contains(Object object) {
      return this.delegate().contains(object);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean add(E element) {
      return this.delegate().add(element);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(Object object) {
      return this.delegate().remove(object);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      return this.delegate().containsAll(collection);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean addAll(Collection<? extends E> collection) {
      return this.delegate().addAll(collection);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean retainAll(Collection<?> collection) {
      return this.delegate().retainAll(collection);
   }

   @Override
   public void clear() {
      this.delegate().clear();
   }

   @Override
   public Object[] toArray() {
      return this.delegate().toArray();
   }

   @CanIgnoreReturnValue
   @Override
   public <T> T[] toArray(T[] array) {
      return (T[])this.delegate().toArray(array);
   }
}
