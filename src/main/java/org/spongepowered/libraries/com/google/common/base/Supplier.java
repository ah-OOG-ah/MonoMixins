package org.spongepowered.libraries.com.google.common.base;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@FunctionalInterface
@GwtCompatible
public interface Supplier<T> extends java.util.function.Supplier<T> {
   @CanIgnoreReturnValue
   @Override
   T get();
}
