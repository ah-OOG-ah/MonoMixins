package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.ImmutableList;
import org.spongepowered.libraries.com.google.common.collect.Queues;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtCompatible(emulated = true)
public final class Futures extends GwtFuturesCatchingSpecialization {
   private static final AsyncFunction<ListenableFuture<Object>, Object> DEREFERENCER = new AsyncFunction<ListenableFuture<Object>, Object>() {
      public ListenableFuture<Object> apply(ListenableFuture<Object> input) {
         return input;
      }
   };

   private Futures() {
   }

   @GwtIncompatible
   public static <V, X extends Exception> CheckedFuture<V, X> makeChecked(ListenableFuture<V> future, Function<? super Exception, X> mapper) {
      return new Futures.MappingCheckedFuture<>(Preconditions.checkNotNull(future), mapper);
   }

   public static <V> ListenableFuture<V> immediateFuture(@Nullable V value) {
      if (value == null) {
         ListenableFuture<V> typedNull = (ListenableFuture<V>)ImmediateFuture.ImmediateSuccessfulFuture.NULL;
         return typedNull;
      } else {
         return new ImmediateFuture.ImmediateSuccessfulFuture<>(value);
      }
   }

   @GwtIncompatible
   public static <V, X extends Exception> CheckedFuture<V, X> immediateCheckedFuture(@Nullable V value) {
      return new ImmediateFuture.ImmediateSuccessfulCheckedFuture<>(value);
   }

   public static <V> ListenableFuture<V> immediateFailedFuture(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      return new ImmediateFuture.ImmediateFailedFuture<>(throwable);
   }

   public static <V> ListenableFuture<V> immediateCancelledFuture() {
      return new ImmediateFuture.ImmediateCancelledFuture<>();
   }

   @GwtIncompatible
   public static <V, X extends Exception> CheckedFuture<V, X> immediateFailedCheckedFuture(X exception) {
      Preconditions.checkNotNull(exception);
      return new ImmediateFuture.ImmediateFailedCheckedFuture<>(exception);
   }

   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catching(
      ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback
   ) {
      return AbstractCatchingFuture.create(input, exceptionType, fallback);
   }

   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catching(
      ListenableFuture<? extends V> input, Class<X> exceptionType, Function<? super X, ? extends V> fallback, Executor executor
   ) {
      return AbstractCatchingFuture.create(input, exceptionType, fallback, executor);
   }

   @CanIgnoreReturnValue
   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catchingAsync(
      ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback
   ) {
      return AbstractCatchingFuture.create(input, exceptionType, fallback);
   }

   @CanIgnoreReturnValue
   @Partially.GwtIncompatible("AVAILABLE but requires exceptionType to be Throwable.class")
   public static <V, X extends Throwable> ListenableFuture<V> catchingAsync(
      ListenableFuture<? extends V> input, Class<X> exceptionType, AsyncFunction<? super X, ? extends V> fallback, Executor executor
   ) {
      return AbstractCatchingFuture.create(input, exceptionType, fallback, executor);
   }

   @GwtIncompatible
   public static <V> ListenableFuture<V> withTimeout(ListenableFuture<V> delegate, long time, TimeUnit unit, ScheduledExecutorService scheduledExecutor) {
      return TimeoutFuture.create(delegate, time, unit, scheduledExecutor);
   }

   public static <I, O> ListenableFuture<O> transformAsync(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
      return AbstractTransformFuture.create(input, function);
   }

   public static <I, O> ListenableFuture<O> transformAsync(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
      return AbstractTransformFuture.create(input, function, executor);
   }

   public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function) {
      return AbstractTransformFuture.create(input, function);
   }

   public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
      return AbstractTransformFuture.create(input, function, executor);
   }

   @GwtIncompatible
   public static <I, O> Future<O> lazyTransform(final Future<I> input, final Function<? super I, ? extends O> function) {
      Preconditions.checkNotNull(input);
      Preconditions.checkNotNull(function);
      return new Future<O>() {
         @Override
         public boolean cancel(boolean mayInterruptIfRunning) {
            return input.cancel(mayInterruptIfRunning);
         }

         @Override
         public boolean isCancelled() {
            return input.isCancelled();
         }

         @Override
         public boolean isDone() {
            return input.isDone();
         }

         @Override
         public O get() throws InterruptedException, ExecutionException {
            return (O)this.applyTransformation(input.get());
         }

         @Override
         public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return (O)this.applyTransformation(input.get(timeout, unit));
         }

         private O applyTransformation(I inputx) throws ExecutionException {
            try {
               return (O)function.apply(input);
            } catch (Throwable var3) {
               throw new ExecutionException(var3);
            }
         }
      };
   }

   public static <V> ListenableFuture<V> dereference(ListenableFuture<? extends ListenableFuture<? extends V>> nested) {
      return transformAsync(nested, DEREFERENCER);
   }

   @SafeVarargs
   @Beta
   public static <V> ListenableFuture<List<V>> allAsList(ListenableFuture<? extends V>... futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), true);
   }

   @Beta
   public static <V> ListenableFuture<List<V>> allAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), true);
   }

   @SafeVarargs
   public static <V> Futures.FutureCombiner<V> whenAllComplete(ListenableFuture<? extends V>... futures) {
      return new Futures.FutureCombiner<>(false, ImmutableList.copyOf(futures));
   }

   public static <V> Futures.FutureCombiner<V> whenAllComplete(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new Futures.FutureCombiner<>(false, ImmutableList.copyOf(futures));
   }

   @SafeVarargs
   public static <V> Futures.FutureCombiner<V> whenAllSucceed(ListenableFuture<? extends V>... futures) {
      return new Futures.FutureCombiner<>(true, ImmutableList.copyOf(futures));
   }

   public static <V> Futures.FutureCombiner<V> whenAllSucceed(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new Futures.FutureCombiner<>(true, ImmutableList.copyOf(futures));
   }

   public static <V> ListenableFuture<V> nonCancellationPropagating(ListenableFuture<V> future) {
      return (ListenableFuture<V>)(future.isDone() ? future : new Futures.NonCancellationPropagatingFuture<>(future));
   }

   @SafeVarargs
   @Beta
   public static <V> ListenableFuture<List<V>> successfulAsList(ListenableFuture<? extends V>... futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), false);
   }

   @Beta
   public static <V> ListenableFuture<List<V>> successfulAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
      return new CollectionFuture.ListFuture<>(ImmutableList.copyOf(futures), false);
   }

   @Beta
   @GwtIncompatible
   public static <T> ImmutableList<ListenableFuture<T>> inCompletionOrder(Iterable<? extends ListenableFuture<? extends T>> futures) {
      final ConcurrentLinkedQueue<SettableFuture<T>> delegates = Queues.newConcurrentLinkedQueue();
      ImmutableList.Builder<ListenableFuture<T>> listBuilder = ImmutableList.builder();
      SerializingExecutor executor = new SerializingExecutor(MoreExecutors.directExecutor());

      for (final ListenableFuture<? extends T> future : futures) {
         SettableFuture<T> delegate = SettableFuture.create();
         delegates.add(delegate);
         future.addListener(new Runnable() {
            @Override
            public void run() {
               delegates.remove().setFuture(future);
            }
         }, executor);
         listBuilder.add(delegate);
      }

      return listBuilder.build();
   }

   public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback) {
      addCallback(future, callback, MoreExecutors.directExecutor());
   }

   public static <V> void addCallback(final ListenableFuture<V> future, final FutureCallback<? super V> callback, Executor executor) {
      Preconditions.checkNotNull(callback);
      Runnable callbackListener = new Runnable() {
         @Override
         public void run() {
            V value;
            try {
               value = Futures.getDone(future);
            } catch (ExecutionException var3) {
               callback.onFailure(var3.getCause());
               return;
            } catch (RuntimeException var4) {
               callback.onFailure(var4);
               return;
            } catch (Error var5) {
               callback.onFailure(var5);
               return;
            }

            callback.onSuccess(value);
         }
      };
      future.addListener(callbackListener, executor);
   }

   @CanIgnoreReturnValue
   public static <V> V getDone(Future<V> future) throws ExecutionException {
      Preconditions.checkState(future.isDone(), "Future was expected to be done: %s", future);
      return Uninterruptibles.getUninterruptibly(future);
   }

   @CanIgnoreReturnValue
   @GwtIncompatible
   public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass) throws X {
      return FuturesGetChecked.getChecked(future, exceptionClass);
   }

   @CanIgnoreReturnValue
   @GwtIncompatible
   public static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, long timeout, TimeUnit unit) throws X {
      return FuturesGetChecked.getChecked(future, exceptionClass, timeout, unit);
   }

   @CanIgnoreReturnValue
   @GwtIncompatible
   public static <V> V getUnchecked(Future<V> future) {
      Preconditions.checkNotNull(future);

      try {
         return Uninterruptibles.getUninterruptibly(future);
      } catch (ExecutionException var2) {
         wrapAndThrowUnchecked(var2.getCause());
         throw new AssertionError();
      }
   }

   @GwtIncompatible
   private static void wrapAndThrowUnchecked(Throwable cause) {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else {
         throw new UncheckedExecutionException(cause);
      }
   }

   @Beta
   @CanIgnoreReturnValue
   @GwtCompatible
   public static final class FutureCombiner<V> {
      private final boolean allMustSucceed;
      private final ImmutableList<ListenableFuture<? extends V>> futures;

      private FutureCombiner(boolean allMustSucceed, ImmutableList<ListenableFuture<? extends V>> futures) {
         this.allMustSucceed = allMustSucceed;
         this.futures = futures;
      }

      public <C> ListenableFuture<C> callAsync(AsyncCallable<C> combiner, Executor executor) {
         return new CombinedFuture<>(this.futures, this.allMustSucceed, executor, combiner);
      }

      public <C> ListenableFuture<C> callAsync(AsyncCallable<C> combiner) {
         return this.callAsync(combiner, MoreExecutors.directExecutor());
      }

      @CanIgnoreReturnValue
      public <C> ListenableFuture<C> call(Callable<C> combiner, Executor executor) {
         return new CombinedFuture<>(this.futures, this.allMustSucceed, executor, combiner);
      }

      @CanIgnoreReturnValue
      public <C> ListenableFuture<C> call(Callable<C> combiner) {
         return this.call(combiner, MoreExecutors.directExecutor());
      }
   }

   @GwtIncompatible
   private static class MappingCheckedFuture<V, X extends Exception> extends AbstractCheckedFuture<V, X> {
      final Function<? super Exception, X> mapper;

      MappingCheckedFuture(ListenableFuture<V> delegate, Function<? super Exception, X> mapper) {
         super(delegate);
         this.mapper = Preconditions.checkNotNull(mapper);
      }

      @Override
      protected X mapException(Exception e) {
         return this.mapper.apply(e);
      }
   }

   private static final class NonCancellationPropagatingFuture<V> extends AbstractFuture.TrustedFuture<V> {
      NonCancellationPropagatingFuture(final ListenableFuture<V> delegate) {
         delegate.addListener(new Runnable() {
            @Override
            public void run() {
               NonCancellationPropagatingFuture.this.setFuture(delegate);
            }
         }, MoreExecutors.directExecutor());
      }
   }
}
