package org.spongepowered.libraries.com.google.common.collect;

import java.util.Iterator;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public abstract class UnmodifiableIterator<E> implements Iterator<E> {
   protected UnmodifiableIterator() {
   }

   @Deprecated
   @Override
   public final void remove() {
      throw new UnsupportedOperationException();
   }
}
