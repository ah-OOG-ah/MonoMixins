package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtIncompatible
final class TimeoutFuture<V> extends AbstractFuture.TrustedFuture<V> {
   @Nullable
   private ListenableFuture<V> delegateRef;
   @Nullable
   private Future<?> timer;

   static <V> ListenableFuture<V> create(ListenableFuture<V> delegate, long time, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
      TimeoutFuture<V> result = new TimeoutFuture<>(delegate);
      TimeoutFuture.Fire<V> fire = new TimeoutFuture.Fire<>(result);
      result.timer = scheduledExecutor.schedule(fire, time, unit);
      delegate.addListener(fire, MoreExecutors.directExecutor());
      return result;
   }

   private TimeoutFuture(ListenableFuture<V> delegate) {
      this.delegateRef = Preconditions.checkNotNull(delegate);
   }

   @Override
   protected void afterDone() {
      this.maybePropagateCancellation(this.delegateRef);
      Future<?> localTimer = this.timer;
      if (localTimer != null) {
         localTimer.cancel(false);
      }

      this.delegateRef = null;
      this.timer = null;
   }

   private static final class Fire<V> implements Runnable {
      @Nullable
      TimeoutFuture<V> timeoutFutureRef;

      Fire(TimeoutFuture<V> timeoutFuture) {
         this.timeoutFutureRef = timeoutFuture;
      }

      @Override
      public void run() {
         TimeoutFuture<V> timeoutFuture = this.timeoutFutureRef;
         if (timeoutFuture != null) {
            ListenableFuture<V> delegate = timeoutFuture.delegateRef;
            if (delegate != null) {
               this.timeoutFutureRef = null;
               if (delegate.isDone()) {
                  timeoutFuture.setFuture(delegate);
               } else {
                  try {
                     timeoutFuture.setException(new TimeoutException("Future timed out: " + delegate));
                  } finally {
                     delegate.cancel(true);
                  }
               }
            }
         }
      }
   }
}
