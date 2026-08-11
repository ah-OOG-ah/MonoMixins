package org.spongepowered.include.com.google.common.base;

import javax.annotation.Nullable;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

@FunctionalInterface
public interface Function<F, T> extends java.util.function.Function<F, T> {
   @Nullable
   @CanIgnoreReturnValue
   @Override
   T apply(@Nullable F var1);

   @Override
   boolean equals(@Nullable Object var1);
}
