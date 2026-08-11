package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtCompatible(emulated = true)
public final class Uninterruptibles {
   @GwtIncompatible
   public static void awaitUninterruptibly(CountDownLatch latch) {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               latch.await();
               return;
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @CanIgnoreReturnValue
   @GwtIncompatible
   public static boolean awaitUninterruptibly(CountDownLatch latch, long timeout, TimeUnit unit) {
      boolean interrupted = false;

      try {
         long remainingNanos = unit.toNanos(timeout);
         long end = System.nanoTime() + remainingNanos;

         while (true) {
            try {
               return latch.await(remainingNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException var13) {
               interrupted = true;
               remainingNanos = end - System.nanoTime();
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static void joinUninterruptibly(Thread toJoin) {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               toJoin.join();
               return;
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @CanIgnoreReturnValue
   public static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               return future.get();
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @CanIgnoreReturnValue
   @GwtIncompatible
   public static <V> V getUninterruptibly(Future<V> future, long timeout, TimeUnit unit) throws ExecutionException, TimeoutException {
      boolean interrupted = false;

      try {
         long remainingNanos = unit.toNanos(timeout);
         long end = System.nanoTime() + remainingNanos;

         while (true) {
            try {
               return future.get(remainingNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException var13) {
               interrupted = true;
               remainingNanos = end - System.nanoTime();
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static void joinUninterruptibly(Thread toJoin, long timeout, TimeUnit unit) {
      Preconditions.checkNotNull(toJoin);
      boolean interrupted = false;

      try {
         long remainingNanos = unit.toNanos(timeout);
         long end = System.nanoTime() + remainingNanos;

         while (true) {
            try {
               TimeUnit.NANOSECONDS.timedJoin(toJoin, remainingNanos);
               return;
            } catch (InterruptedException var13) {
               interrupted = true;
               remainingNanos = end - System.nanoTime();
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static <E> E takeUninterruptibly(BlockingQueue<E> queue) {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               return queue.take();
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static <E> void putUninterruptibly(BlockingQueue<E> queue, E element) {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               queue.put(element);
               return;
            } catch (InterruptedException var7) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
      boolean interrupted = false;

      try {
         long remainingNanos = unit.toNanos(sleepFor);
         long end = System.nanoTime() + remainingNanos;

         while (true) {
            try {
               TimeUnit.NANOSECONDS.sleep(remainingNanos);
               return;
            } catch (InterruptedException var12) {
               interrupted = true;
               remainingNanos = end - System.nanoTime();
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @GwtIncompatible
   public static boolean tryAcquireUninterruptibly(Semaphore semaphore, long timeout, TimeUnit unit) {
      return tryAcquireUninterruptibly(semaphore, 1, timeout, unit);
   }

   @GwtIncompatible
   public static boolean tryAcquireUninterruptibly(Semaphore semaphore, int permits, long timeout, TimeUnit unit) {
      boolean interrupted = false;

      try {
         long remainingNanos = unit.toNanos(timeout);
         long end = System.nanoTime() + remainingNanos;

         while (true) {
            try {
               return semaphore.tryAcquire(permits, remainingNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException var14) {
               interrupted = true;
               remainingNanos = end - System.nanoTime();
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private Uninterruptibles() {
   }
}
