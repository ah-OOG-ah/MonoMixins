package org.spongepowered.libraries.com.google.common.collect;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
final class CollectPreconditions {
   static void checkEntryNotNull(Object key, Object value) {
      if (key == null) {
         throw new NullPointerException("null key in entry: null=" + value);
      } else if (value == null) {
         throw new NullPointerException("null value in entry: " + key + "=null");
      }
   }

   @CanIgnoreReturnValue
   static int checkNonnegative(int value, String name) {
      if (value < 0) {
         throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
      } else {
         return value;
      }
   }

   @CanIgnoreReturnValue
   static long checkNonnegative(long value, String name) {
      if (value < 0L) {
         throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
      } else {
         return value;
      }
   }

   static void checkPositive(int value, String name) {
      if (value <= 0) {
         throw new IllegalArgumentException(name + " must be positive but was: " + value);
      }
   }

   static void checkRemove(boolean canRemove) {
      Preconditions.checkState(canRemove, "no calls to next() since the last call to remove()");
   }
}
