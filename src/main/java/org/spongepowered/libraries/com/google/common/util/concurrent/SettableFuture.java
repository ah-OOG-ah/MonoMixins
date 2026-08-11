package org.spongepowered.libraries.com.google.common.util.concurrent;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public final class SettableFuture<V> extends AbstractFuture.TrustedFuture<V> {
   public static <V> SettableFuture<V> create() {
      return new SettableFuture<>();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean set(@Nullable V value) {
      return super.set(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setException(Throwable throwable) {
      return super.setException(throwable);
   }

   @Beta
   @CanIgnoreReturnValue
   @Override
   public boolean setFuture(ListenableFuture<? extends V> future) {
      return super.setFuture(future);
   }

   private SettableFuture() {
   }
}
