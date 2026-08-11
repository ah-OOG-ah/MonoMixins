package org.spongepowered.include.com.google.common.collect;

import java.util.function.Consumer;

class RegularImmutableAsList<E> extends ImmutableAsList<E> {
   private final ImmutableCollection<E> delegate;
   private final ImmutableList<? extends E> delegateList;

   RegularImmutableAsList(ImmutableCollection<E> delegate, ImmutableList<? extends E> delegateList) {
      this.delegate = delegate;
      this.delegateList = delegateList;
   }

   RegularImmutableAsList(ImmutableCollection<E> delegate, Object[] array) {
      this(delegate, ImmutableList.asImmutableList(array));
   }

   @Override
   ImmutableCollection<E> delegateCollection() {
      return this.delegate;
   }

   @Override
   public UnmodifiableListIterator<E> listIterator(int index) {
      return (UnmodifiableListIterator<E>)this.delegateList.listIterator(index);
   }

   @Override
   public void forEach(Consumer<? super E> action) {
      this.delegateList.forEach(action);
   }

   @Override
   int copyIntoArray(Object[] dst, int offset) {
      return this.delegateList.copyIntoArray(dst, offset);
   }

   @Override
   public E get(int index) {
      return (E)this.delegateList.get(index);
   }
}
