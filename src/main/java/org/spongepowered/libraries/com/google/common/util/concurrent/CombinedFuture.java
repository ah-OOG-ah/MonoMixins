package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.ImmutableCollection;

@GwtCompatible
final class CombinedFuture<V> extends AggregateFuture<Object, V> {
   CombinedFuture(ImmutableCollection<? extends ListenableFuture<?>> futures, boolean allMustSucceed, Executor listenerExecutor, AsyncCallable<V> callable) {
      this.init(
         new CombinedFuture.CombinedFutureRunningState(futures, allMustSucceed, new CombinedFuture.AsyncCallableInterruptibleTask(callable, listenerExecutor))
      );
   }

   CombinedFuture(ImmutableCollection<? extends ListenableFuture<?>> futures, boolean allMustSucceed, Executor listenerExecutor, Callable<V> callable) {
      this.init(
         new CombinedFuture.CombinedFutureRunningState(futures, allMustSucceed, new CombinedFuture.CallableInterruptibleTask(callable, listenerExecutor))
      );
   }

   private final class AsyncCallableInterruptibleTask extends CombinedFuture<V>.CombinedFutureInterruptibleTask {
      private final AsyncCallable<V> callable;

      public AsyncCallableInterruptibleTask(AsyncCallable<V> callable, Executor listenerExecutor) {
         super(listenerExecutor);
         this.callable = Preconditions.checkNotNull(callable);
      }

      @Override
      void setValue() throws Exception {
         CombinedFuture.this.setFuture(this.callable.call());
      }
   }

   private final class CallableInterruptibleTask extends CombinedFuture<V>.CombinedFutureInterruptibleTask {
      private final Callable<V> callable;

      public CallableInterruptibleTask(Callable<V> callable, Executor listenerExecutor) {
         super(listenerExecutor);
         this.callable = Preconditions.checkNotNull(callable);
      }

      @Override
      void setValue() throws Exception {
         CombinedFuture.this.set(this.callable.call());
      }
   }

   private abstract class CombinedFutureInterruptibleTask extends InterruptibleTask {
      private final Executor listenerExecutor;
      volatile boolean thrownByExecute = true;

      public CombinedFutureInterruptibleTask(Executor listenerExecutor) {
         this.listenerExecutor = Preconditions.checkNotNull(listenerExecutor);
      }

      @Override
      final void runInterruptibly() {
         this.thrownByExecute = false;
         if (!CombinedFuture.this.isDone()) {
            try {
               this.setValue();
            } catch (ExecutionException var2) {
               CombinedFuture.this.setException(var2.getCause());
            } catch (CancellationException var3) {
               CombinedFuture.this.cancel(false);
            } catch (Throwable var4) {
               CombinedFuture.this.setException(var4);
            }
         }
      }

      @Override
      final boolean wasInterrupted() {
         return CombinedFuture.this.wasInterrupted();
      }

      final void execute() {
         try {
            this.listenerExecutor.execute(this);
         } catch (RejectedExecutionException var2) {
            if (this.thrownByExecute) {
               CombinedFuture.this.setException(var2);
            }
         }
      }

      abstract void setValue() throws Exception;
   }

   private final class CombinedFutureRunningState extends AggregateFuture<Object, V>.RunningState {
      private CombinedFuture<V>.CombinedFutureInterruptibleTask task;

      CombinedFutureRunningState(
         ImmutableCollection<? extends ListenableFuture<? extends Object>> futures,
         boolean allMustSucceed,
         CombinedFuture<V>.CombinedFutureInterruptibleTask task
      ) {
         super(futures, allMustSucceed, false);
         this.task = task;
      }

      @Override
      void collectOneValue(boolean allMustSucceed, int index, @Nullable Object returnValue) {
      }

      @Override
      void handleAllCompleted() {
         CombinedFuture<V>.CombinedFutureInterruptibleTask localTask = this.task;
         if (localTask != null) {
            localTask.execute();
         } else {
            Preconditions.checkState(CombinedFuture.this.isDone());
         }
      }

      @Override
      void releaseResourcesAfterFailure() {
         super.releaseResourcesAfterFailure();
         this.task = null;
      }

      @Override
      void interruptTask() {
         CombinedFuture<V>.CombinedFutureInterruptibleTask localTask = this.task;
         if (localTask != null) {
            localTask.interruptTask();
         }
      }
   }
}
