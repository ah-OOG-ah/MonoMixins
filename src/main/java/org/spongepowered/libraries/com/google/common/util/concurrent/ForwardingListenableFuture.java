package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Executor;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@CanIgnoreReturnValue
@GwtCompatible
public abstract class ForwardingListenableFuture<V> extends ForwardingFuture<V> implements ListenableFuture<V> {
   protected ForwardingListenableFuture() {
   }

   protected abstract ListenableFuture<? extends V> delegate();

   @Override
   public void addListener(Runnable listener, Executor exec) {
      this.delegate().addListener(listener, exec);
   }

   public abstract static class SimpleForwardingListenableFuture<V> extends ForwardingListenableFuture<V> {
      private final ListenableFuture<V> delegate;

      protected SimpleForwardingListenableFuture(ListenableFuture<V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected final ListenableFuture<V> delegate() {
         return this.delegate;
      }
   }
}
