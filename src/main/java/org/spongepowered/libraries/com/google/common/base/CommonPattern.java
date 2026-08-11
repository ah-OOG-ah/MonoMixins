package org.spongepowered.libraries.com.google.common.base;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
abstract class CommonPattern {
   abstract CommonMatcher matcher(CharSequence var1);

   abstract String pattern();

   abstract int flags();

   @Override
   public abstract String toString();

   @Override
   public abstract int hashCode();

   @Override
   public abstract boolean equals(Object var1);
}
