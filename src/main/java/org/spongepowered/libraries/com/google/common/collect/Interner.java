package org.spongepowered.libraries.com.google.common.collect;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtIncompatible
public interface Interner<E> {
   @CanIgnoreReturnValue
   E intern(E var1);
}
