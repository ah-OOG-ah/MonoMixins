package org.spongepowered.libraries.com.google.common.base;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@Beta
@GwtCompatible
public class VerifyException extends RuntimeException {
   public VerifyException() {
   }

   public VerifyException(@Nullable String message) {
      super(message);
   }

   public VerifyException(@Nullable Throwable cause) {
      super(cause);
   }

   public VerifyException(@Nullable String message, @Nullable Throwable cause) {
      super(message, cause);
   }
}
