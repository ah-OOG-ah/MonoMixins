package org.spongepowered.libraries.com.google.common.collect;

import java.util.ListIterator;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public abstract class UnmodifiableListIterator<E> extends UnmodifiableIterator<E> implements ListIterator<E> {
   protected UnmodifiableListIterator() {
   }

   @Deprecated
   @Override
   public final void add(E e) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void set(E e) {
      throw new UnsupportedOperationException();
   }
}
