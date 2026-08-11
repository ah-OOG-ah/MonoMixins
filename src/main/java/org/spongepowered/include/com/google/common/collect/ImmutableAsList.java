package org.spongepowered.include.com.google.common.collect;

abstract class ImmutableAsList<E> extends ImmutableList<E> {
   abstract ImmutableCollection<E> delegateCollection();

   @Override
   public boolean contains(Object target) {
      return this.delegateCollection().contains(target);
   }

   @Override
   public int size() {
      return this.delegateCollection().size();
   }

   @Override
   public boolean isEmpty() {
      return this.delegateCollection().isEmpty();
   }
}
