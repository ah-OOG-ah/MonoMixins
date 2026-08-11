package org.spongepowered.libraries.com.google.common.base;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@FunctionalInterface
@GwtCompatible
public interface Predicate<T> extends java.util.function.Predicate<T> {
   @CanIgnoreReturnValue
   boolean apply(@Nullable T var1);

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   default boolean test(@Nullable T input) {
      return this.apply(input);
   }
}
