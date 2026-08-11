package org.spongepowered.libraries.com.google.common.base;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtCompatible
public abstract class Ticker {
   private static final Ticker SYSTEM_TICKER = new Ticker() {
      @Override
      public long read() {
         return Platform.systemNanoTime();
      }
   };

   protected Ticker() {
   }

   @CanIgnoreReturnValue
   public abstract long read();

   public static Ticker systemTicker() {
      return SYSTEM_TICKER;
   }
}
