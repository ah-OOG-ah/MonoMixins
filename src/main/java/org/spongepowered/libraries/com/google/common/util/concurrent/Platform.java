package org.spongepowered.libraries.com.google.common.util.concurrent;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
final class Platform {
   static boolean isInstanceOfThrowableClass(@Nullable Throwable t, Class<? extends Throwable> expectedClass) {
      return expectedClass.isInstance(t);
   }

   private Platform() {
   }
}
