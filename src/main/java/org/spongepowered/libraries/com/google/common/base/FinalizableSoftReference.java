package org.spongepowered.libraries.com.google.common.base;

import java.lang.ref.SoftReference;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@GwtIncompatible
public abstract class FinalizableSoftReference<T> extends SoftReference<T> implements FinalizableReference {
   protected FinalizableSoftReference(T referent, FinalizableReferenceQueue queue) {
      super(referent, queue.queue);
      queue.cleanUp();
   }
}
