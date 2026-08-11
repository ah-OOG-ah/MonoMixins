package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.ForOverride;

@GwtCompatible
abstract class AbstractTransformFuture<I, O, F, T> extends AbstractFuture.TrustedFuture<O> implements Runnable {
   @Nullable
   ListenableFuture<? extends I> inputFuture;
   @Nullable
   F function;

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
      AbstractTransformFuture.AsyncTransformFuture<I, O> output = new AbstractTransformFuture.AsyncTransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.directExecutor());
      return output;
   }

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
      Preconditions.checkNotNull(executor);
      AbstractTransformFuture.AsyncTransformFuture<I, O> output = new AbstractTransformFuture.AsyncTransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, Function<? super I, ? extends O> function) {
      Preconditions.checkNotNull(function);
      AbstractTransformFuture.TransformFuture<I, O> output = new AbstractTransformFuture.TransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.directExecutor());
      return output;
   }

   static <I, O> ListenableFuture<O> create(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
      Preconditions.checkNotNull(function);
      AbstractTransformFuture.TransformFuture<I, O> output = new AbstractTransformFuture.TransformFuture<>(input, function);
      input.addListener(output, MoreExecutors.rejectionPropagatingExecutor(executor, output));
      return output;
   }

   AbstractTransformFuture(ListenableFuture<? extends I> inputFuture, F function) {
      this.inputFuture = Preconditions.checkNotNull(inputFuture);
      this.function = Preconditions.checkNotNull(function);
   }

   @Override
   public final void run() {
      ListenableFuture<? extends I> localInputFuture = this.inputFuture;
      F localFunction = this.function;
      if (!(this.isCancelled() | localInputFuture == null | localFunction == null)) {
         this.inputFuture = null;
         this.function = null;

         I sourceResult;
         try {
            sourceResult = Futures.getDone((Future<I>)localInputFuture);
         } catch (CancellationException var8) {
            this.cancel(false);
            return;
         } catch (ExecutionException var9) {
            this.setException(var9.getCause());
            return;
         } catch (RuntimeException var10) {
            this.setException(var10);
            return;
         } catch (Error var11) {
            this.setException(var11);
            return;
         }

         T transformResult;
         try {
            transformResult = this.doTransform(localFunction, sourceResult);
         } catch (UndeclaredThrowableException var6) {
            this.setException(var6.getCause());
            return;
         } catch (Throwable var7) {
            this.setException(var7);
            return;
         }

         this.setResult(transformResult);
      }
   }

   @Nullable
   @ForOverride
   abstract T doTransform(F var1, @Nullable I var2) throws Exception;

   @ForOverride
   abstract void setResult(@Nullable T var1);

   @Override
   protected final void afterDone() {
      this.maybePropagateCancellation(this.inputFuture);
      this.inputFuture = null;
      this.function = null;
   }

   private static final class AsyncTransformFuture<I, O>
      extends AbstractTransformFuture<I, O, AsyncFunction<? super I, ? extends O>, ListenableFuture<? extends O>> {
      AsyncTransformFuture(ListenableFuture<? extends I> inputFuture, AsyncFunction<? super I, ? extends O> function) {
         super(inputFuture, function);
      }

      ListenableFuture<? extends O> doTransform(AsyncFunction<? super I, ? extends O> function, @Nullable I input) throws Exception {
         ListenableFuture<? extends O> outputFuture = function.apply(input);
         Preconditions.checkNotNull(outputFuture, "AsyncFunction.apply returned null instead of a Future. Did you mean to return immediateFuture(null)?");
         return outputFuture;
      }

      void setResult(ListenableFuture<? extends O> result) {
         this.setFuture(result);
      }
   }

   private static final class TransformFuture<I, O> extends AbstractTransformFuture<I, O, Function<? super I, ? extends O>, O> {
      TransformFuture(ListenableFuture<? extends I> inputFuture, Function<? super I, ? extends O> function) {
         super(inputFuture, function);
      }

      @Nullable
      O doTransform(Function<? super I, ? extends O> function, @Nullable I input) {
         return (O)function.apply(input);
      }

      @Override
      void setResult(@Nullable O result) {
         this.set(result);
      }
   }
}
