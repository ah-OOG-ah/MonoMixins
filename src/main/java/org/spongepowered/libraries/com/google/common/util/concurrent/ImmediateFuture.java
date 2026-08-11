package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible(emulated = true)
abstract class ImmediateFuture<V> implements ListenableFuture<V> {
   private static final Logger log = Logger.getLogger(ImmediateFuture.class.getName());

   @Override
   public void addListener(Runnable listener, Executor executor) {
      Preconditions.checkNotNull(listener, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");

      try {
         executor.execute(listener);
      } catch (RuntimeException var4) {
         log.log(Level.SEVERE, "RuntimeException while executing runnable " + listener + " with executor " + executor, (Throwable)var4);
      }
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   @Override
   public abstract V get() throws ExecutionException;

   @Override
   public V get(long timeout, TimeUnit unit) throws ExecutionException {
      Preconditions.checkNotNull(unit);
      return this.get();
   }

   @Override
   public boolean isCancelled() {
      return false;
   }

   @Override
   public boolean isDone() {
      return true;
   }

   static final class ImmediateCancelledFuture<V> extends AbstractFuture.TrustedFuture<V> {
      ImmediateCancelledFuture() {
         this.cancel(false);
      }
   }

   @GwtIncompatible
   static class ImmediateFailedCheckedFuture<V, X extends Exception> extends ImmediateFuture<V> implements CheckedFuture<V, X> {
      private final X thrown;

      ImmediateFailedCheckedFuture(X thrown) {
         this.thrown = thrown;
      }

      @Override
      public V get() throws ExecutionException {
         throw new ExecutionException(this.thrown);
      }

      @Override
      public V checkedGet() throws X {
         throw this.thrown;
      }

      @Override
      public V checkedGet(long timeout, TimeUnit unit) throws X {
         Preconditions.checkNotNull(unit);
         throw this.thrown;
      }
   }

   static final class ImmediateFailedFuture<V> extends AbstractFuture.TrustedFuture<V> {
      ImmediateFailedFuture(Throwable thrown) {
         this.setException(thrown);
      }
   }

   @GwtIncompatible
   static class ImmediateSuccessfulCheckedFuture<V, X extends Exception> extends ImmediateFuture<V> implements CheckedFuture<V, X> {
      @Nullable
      private final V value;

      ImmediateSuccessfulCheckedFuture(@Nullable V value) {
         this.value = value;
      }

      @Override
      public V get() {
         return this.value;
      }

      @Override
      public V checkedGet() {
         return this.value;
      }

      @Override
      public V checkedGet(long timeout, TimeUnit unit) {
         Preconditions.checkNotNull(unit);
         return this.value;
      }
   }

   static class ImmediateSuccessfulFuture<V> extends ImmediateFuture<V> {
      static final ImmediateFuture.ImmediateSuccessfulFuture<Object> NULL = new ImmediateFuture.ImmediateSuccessfulFuture<>(null);
      @Nullable
      private final V value;

      ImmediateSuccessfulFuture(@Nullable V value) {
         this.value = value;
      }

      @Override
      public V get() {
         return this.value;
      }
   }
}
