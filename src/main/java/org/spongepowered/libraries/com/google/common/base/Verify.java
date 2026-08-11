package org.spongepowered.libraries.com.google.common.base;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtCompatible
public final class Verify {
   public static void verify(boolean expression) {
      if (!expression) {
         throw new VerifyException();
      }
   }

   public static void verify(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
      if (!expression) {
         throw new VerifyException(Preconditions.format(errorMessageTemplate, errorMessageArgs));
      }
   }

   @CanIgnoreReturnValue
   public static <T> T verifyNotNull(@Nullable T reference) {
      return verifyNotNull(reference, "expected a non-null reference");
   }

   @CanIgnoreReturnValue
   public static <T> T verifyNotNull(@Nullable T reference, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
      verify(reference != null, errorMessageTemplate, errorMessageArgs);
      return reference;
   }

   private Verify() {
   }
}
