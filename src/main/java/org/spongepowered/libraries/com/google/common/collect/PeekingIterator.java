package org.spongepowered.libraries.com.google.common.collect;

import java.util.Iterator;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public interface PeekingIterator<E> extends Iterator<E> {
   E peek();

   @CanIgnoreReturnValue
   @Override
   E next();

   @Override
   void remove();
}
