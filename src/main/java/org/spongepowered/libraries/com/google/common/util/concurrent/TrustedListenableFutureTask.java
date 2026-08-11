package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible
class TrustedListenableFutureTask<V> extends AbstractFuture.TrustedFuture<V> implements RunnableFuture<V> {
   private TrustedListenableFutureTask<V>.TrustedFutureInterruptibleTask task;

   static <V> TrustedListenableFutureTask<V> create(Callable<V> callable) {
      return new TrustedListenableFutureTask<>(callable);
   }

   static <V> TrustedListenableFutureTask<V> create(Runnable runnable, @Nullable V result) {
      return new TrustedListenableFutureTask<>(Executors.callable(runnable, result));
   }

   TrustedListenableFutureTask(Callable<V> callable) {
      this.task = new TrustedListenableFutureTask.TrustedFutureInterruptibleTask(callable);
   }

   @Override
   public void run() {
      TrustedListenableFutureTask<V>.TrustedFutureInterruptibleTask localTask = this.task;
      if (localTask != null) {
         localTask.run();
      }
   }

   @Override
   protected void afterDone() {
      super.afterDone();
      if (this.wasInterrupted()) {
         TrustedListenableFutureTask<V>.TrustedFutureInterruptibleTask localTask = this.task;
         if (localTask != null) {
            localTask.interruptTask();
         }
      }

      this.task = null;
   }

   @Override
   public String toString() {
      return super.toString() + " (delegate = " + this.task + ")";
   }

   private final class TrustedFutureInterruptibleTask extends InterruptibleTask {
      private final Callable<V> callable;

      TrustedFutureInterruptibleTask(Callable<V> callable) {
         this.callable = Preconditions.checkNotNull(callable);
      }

      @Override
      void runInterruptibly() {
         if (!TrustedListenableFutureTask.this.isDone()) {
            try {
               TrustedListenableFutureTask.this.set(this.callable.call());
            } catch (Throwable var2) {
               TrustedListenableFutureTask.this.setException(var2);
            }
         }
      }

      @Override
      boolean wasInterrupted() {
         return TrustedListenableFutureTask.this.wasInterrupted();
      }

      @Override
      public String toString() {
         return this.callable.toString();
      }
   }
}
