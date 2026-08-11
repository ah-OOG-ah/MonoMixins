package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Supplier;

@GwtCompatible(emulated = true)
public final class Callables {
   private Callables() {
   }

   public static <T> Callable<T> returning(@Nullable final T value) {
      return new Callable<T>() {
         @Override
         public T call() {
            return value;
         }
      };
   }

   @Beta
   @GwtIncompatible
   public static <T> AsyncCallable<T> asAsyncCallable(final Callable<T> callable, final ListeningExecutorService listeningExecutorService) {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(listeningExecutorService);
      return new AsyncCallable<T>() {
         @Override
         public ListenableFuture<T> call() throws Exception {
            return listeningExecutorService.submit(callable);
         }
      };
   }

   @GwtIncompatible
   static <T> Callable<T> threadRenaming(final Callable<T> callable, final Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(callable);
      return new Callable<T>() {
         @Override
         public T call() throws Exception {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            boolean restoreName = Callables.trySetName(nameSupplier.get(), currentThread);

            Object var4;
            try {
               var4 = callable.call();
            } finally {
               if (restoreName) {
                  boolean var7 = Callables.trySetName(oldName, currentThread);
               }
            }

            return (T)var4;
         }
      };
   }

   @GwtIncompatible
   static Runnable threadRenaming(final Runnable task, final Supplier<String> nameSupplier) {
      Preconditions.checkNotNull(nameSupplier);
      Preconditions.checkNotNull(task);
      return new Runnable() {
         @Override
         public void run() {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            boolean restoreName = Callables.trySetName(nameSupplier.get(), currentThread);

            try {
               task.run();
            } finally {
               if (restoreName) {
                  boolean var6 = Callables.trySetName(oldName, currentThread);
               }
            }
         }
      };
   }

   @GwtIncompatible
   private static boolean trySetName(String threadName, Thread currentThread) {
      try {
         currentThread.setName(threadName);
         return true;
      } catch (SecurityException var3) {
         return false;
      }
   }
}
