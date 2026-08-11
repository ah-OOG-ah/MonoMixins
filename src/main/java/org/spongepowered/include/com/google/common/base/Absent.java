package org.spongepowered.include.com.google.common.base;

import javax.annotation.Nullable;

final class Absent<T> extends Optional<T> {
   static final Absent<Object> INSTANCE = new Absent<>();

   static <T> Optional<T> withType() {
      return (Optional<T>)INSTANCE;
   }

   private Absent() {
   }

   @Override
   public T or(T defaultValue) {
      return Preconditions.checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return object == this;
   }

   @Override
   public int hashCode() {
      return 2040732332;
   }

   @Override
   public String toString() {
      return "Optional.absent()";
   }
}
