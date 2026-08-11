package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Throwables;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import sun.misc.Unsafe;

@GwtCompatible(emulated = true)
public abstract class AbstractFuture<V> implements ListenableFuture<V> {
   private static final boolean GENERATE_CANCELLATION_CAUSES = Boolean.parseBoolean(System.getProperty("guava.concurrent.generate_cancellation_cause", "false"));
   private static final Logger log = Logger.getLogger(AbstractFuture.class.getName());
   private static final long SPIN_THRESHOLD_NANOS = 1000L;
   private static final AbstractFuture.AtomicHelper ATOMIC_HELPER;
   private static final Object NULL;
   private volatile Object value;
   private volatile AbstractFuture.Listener listeners;
   private volatile AbstractFuture.Waiter waiters;

   private void removeWaiter(AbstractFuture.Waiter node) {
      node.thread = null;

      label28:
      while (true) {
         AbstractFuture.Waiter pred = null;
         AbstractFuture.Waiter curr = this.waiters;
         if (curr == AbstractFuture.Waiter.TOMBSTONE) {
            return;
         }

         while (curr != null) {
            AbstractFuture.Waiter succ = curr.next;
            if (curr.thread != null) {
               pred = curr;
            } else if (pred != null) {
               pred.next = succ;
               if (pred.thread == null) {
                  continue label28;
               }
            } else if (!ATOMIC_HELPER.casWaiters(this, curr, succ)) {
               continue label28;
            }

            curr = succ;
         }

         return;
      }
   }

   protected AbstractFuture() {
   }

   @CanIgnoreReturnValue
   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
      long remainingNanos = unit.toNanos(timeout);
      if (Thread.interrupted()) {
         throw new InterruptedException();
      } else {
         Object localValue = this.value;
         if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
            return this.getDoneValue(localValue);
         } else {
            long endNanos = remainingNanos > 0L ? System.nanoTime() + remainingNanos : 0L;
            if (remainingNanos >= 1000L) {
               AbstractFuture.Waiter oldHead = this.waiters;
               if (oldHead == AbstractFuture.Waiter.TOMBSTONE) {
                  return this.getDoneValue(this.value);
               }

               AbstractFuture.Waiter node = new AbstractFuture.Waiter();

               while (true) {
                  node.setNext(oldHead);
                  if (ATOMIC_HELPER.casWaiters(this, oldHead, node)) {
                     do {
                        LockSupport.parkNanos(this, remainingNanos);
                        if (Thread.interrupted()) {
                           this.removeWaiter(node);
                           throw new InterruptedException();
                        }

                        localValue = this.value;
                        if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
                           return this.getDoneValue(localValue);
                        }

                        remainingNanos = endNanos - System.nanoTime();
                     } while (remainingNanos >= 1000L);

                     this.removeWaiter(node);
                     break;
                  }

                  oldHead = this.waiters;
                  if (oldHead == AbstractFuture.Waiter.TOMBSTONE) {
                     return this.getDoneValue(this.value);
                  }
               }
            }

            while (remainingNanos > 0L) {
               localValue = this.value;
               if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
                  return this.getDoneValue(localValue);
               }

               if (Thread.interrupted()) {
                  throw new InterruptedException();
               }

               remainingNanos = endNanos - System.nanoTime();
            }

            throw new TimeoutException();
         }
      }
   }

   @CanIgnoreReturnValue
   @Override
   public V get() throws InterruptedException, ExecutionException {
      if (Thread.interrupted()) {
         throw new InterruptedException();
      } else {
         Object localValue = this.value;
         if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
            return this.getDoneValue(localValue);
         } else {
            AbstractFuture.Waiter oldHead = this.waiters;
            if (oldHead != AbstractFuture.Waiter.TOMBSTONE) {
               AbstractFuture.Waiter node = new AbstractFuture.Waiter();

               do {
                  node.setNext(oldHead);
                  if (ATOMIC_HELPER.casWaiters(this, oldHead, node)) {
                     do {
                        LockSupport.park(this);
                        if (Thread.interrupted()) {
                           this.removeWaiter(node);
                           throw new InterruptedException();
                        }

                        localValue = this.value;
                     } while (!(localValue != null & !(localValue instanceof AbstractFuture.SetFuture)));

                     return this.getDoneValue(localValue);
                  }

                  oldHead = this.waiters;
               } while (oldHead != AbstractFuture.Waiter.TOMBSTONE);
            }

            return this.getDoneValue(this.value);
         }
      }
   }

   private V getDoneValue(Object obj) throws ExecutionException {
      if (obj instanceof AbstractFuture.Cancellation) {
         throw cancellationExceptionWithCause("Task was cancelled.", ((AbstractFuture.Cancellation)obj).cause);
      } else if (obj instanceof AbstractFuture.Failure) {
         throw new ExecutionException(((AbstractFuture.Failure)obj).exception);
      } else {
         return (V)(obj == NULL ? null : obj);
      }
   }

   @Override
   public boolean isDone() {
      Object localValue = this.value;
      return localValue != null & !(localValue instanceof AbstractFuture.SetFuture);
   }

   @Override
   public boolean isCancelled() {
      Object localValue = this.value;
      return localValue instanceof AbstractFuture.Cancellation;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      Object localValue = this.value;
      boolean rValue = false;
      if (localValue == null | localValue instanceof AbstractFuture.SetFuture) {
         Throwable cause = GENERATE_CANCELLATION_CAUSES ? new CancellationException("Future.cancel() was called.") : null;
         Object valueToSet = new AbstractFuture.Cancellation(mayInterruptIfRunning, cause);
         AbstractFuture<?> abstractFuture = this;

         while (true) {
            while (!ATOMIC_HELPER.casValue(abstractFuture, localValue, valueToSet)) {
               localValue = abstractFuture.value;
               if (!(localValue instanceof AbstractFuture.SetFuture)) {
                  return rValue;
               }
            }

            rValue = true;
            if (mayInterruptIfRunning) {
               abstractFuture.interruptTask();
            }

            complete(abstractFuture);
            if (!(localValue instanceof AbstractFuture.SetFuture)) {
               break;
            }

            ListenableFuture<?> futureToPropagateTo = ((AbstractFuture.SetFuture)localValue).future;
            if (!(futureToPropagateTo instanceof AbstractFuture.TrustedFuture)) {
               futureToPropagateTo.cancel(mayInterruptIfRunning);
               break;
            }

            AbstractFuture<?> trusted = (AbstractFuture<?>)futureToPropagateTo;
            localValue = trusted.value;
            if (!(localValue == null | localValue instanceof AbstractFuture.SetFuture)) {
               break;
            }

            abstractFuture = trusted;
         }
      }

      return rValue;
   }

   protected void interruptTask() {
   }

   protected final boolean wasInterrupted() {
      Object localValue = this.value;
      return localValue instanceof AbstractFuture.Cancellation && ((AbstractFuture.Cancellation)localValue).wasInterrupted;
   }

   @Override
   public void addListener(Runnable listener, Executor executor) {
      Preconditions.checkNotNull(listener, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");
      AbstractFuture.Listener oldHead = this.listeners;
      if (oldHead != AbstractFuture.Listener.TOMBSTONE) {
         AbstractFuture.Listener newNode = new AbstractFuture.Listener(listener, executor);

         do {
            newNode.next = oldHead;
            if (ATOMIC_HELPER.casListeners(this, oldHead, newNode)) {
               return;
            }

            oldHead = this.listeners;
         } while (oldHead != AbstractFuture.Listener.TOMBSTONE);
      }

      executeListener(listener, executor);
   }

   @CanIgnoreReturnValue
   protected boolean set(@Nullable V value) {
      Object valueToSet = value == null ? NULL : value;
      if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
         complete(this);
         return true;
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   protected boolean setException(Throwable throwable) {
      Object valueToSet = new AbstractFuture.Failure(Preconditions.checkNotNull(throwable));
      if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
         complete(this);
         return true;
      } else {
         return false;
      }
   }

   @Beta
   @CanIgnoreReturnValue
   protected boolean setFuture(ListenableFuture<? extends V> future) {
      Preconditions.checkNotNull(future);
      Object localValue = this.value;
      if (localValue == null) {
         if (future.isDone()) {
            Object value = getFutureValue(future);
            if (ATOMIC_HELPER.casValue(this, null, value)) {
               complete(this);
               return true;
            }

            return false;
         }

         AbstractFuture.SetFuture valueToSet = new AbstractFuture.SetFuture<>(this, future);
         if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
            try {
               future.addListener(valueToSet, MoreExecutors.directExecutor());
            } catch (Throwable var8) {
               Throwable t = var8;

               AbstractFuture.Failure failure;
               try {
                  failure = new AbstractFuture.Failure(t);
               } catch (Throwable var7) {
                  failure = AbstractFuture.Failure.FALLBACK_INSTANCE;
               }

               boolean oomMostLikely = ATOMIC_HELPER.casValue(this, valueToSet, failure);
            }

            return true;
         }

         localValue = this.value;
      }

      if (localValue instanceof AbstractFuture.Cancellation) {
         future.cancel(((AbstractFuture.Cancellation)localValue).wasInterrupted);
      }

      return false;
   }

   private static Object getFutureValue(ListenableFuture<?> future) {
      if (future instanceof AbstractFuture.TrustedFuture) {
         return ((AbstractFuture)future).value;
      } else {
         Object valueToSet;
         try {
            Object v = Futures.getDone((Future<V>)future);
            valueToSet = v == null ? NULL : v;
         } catch (ExecutionException var3) {
            valueToSet = new AbstractFuture.Failure(var3.getCause());
         } catch (CancellationException var4) {
            valueToSet = new AbstractFuture.Cancellation(false, var4);
         } catch (Throwable var5) {
            valueToSet = new AbstractFuture.Failure(var5);
         }

         return valueToSet;
      }
   }

   private static void complete(AbstractFuture<?> future) {
      AbstractFuture.Listener next = null;

      label23:
      while (true) {
         future.releaseWaiters();
         future.afterDone();
         next = future.clearListeners(next);
         AbstractFuture<?> var6 = null;

         while (next != null) {
            AbstractFuture.Listener curr = next;
            next = next.next;
            Runnable task = curr.task;
            if (task instanceof AbstractFuture.SetFuture) {
               AbstractFuture.SetFuture<?> setFuture = (AbstractFuture.SetFuture<?>)task;
               future = setFuture.owner;
               if (future.value == setFuture) {
                  Object valueToSet = getFutureValue(setFuture.future);
                  if (ATOMIC_HELPER.casValue(future, setFuture, valueToSet)) {
                     continue label23;
                  }
               }
            } else {
               executeListener(task, curr.executor);
            }
         }

         return;
      }
   }

   @Beta
   protected void afterDone() {
   }

   final Throwable trustedGetException() {
      return ((AbstractFuture.Failure)this.value).exception;
   }

   final void maybePropagateCancellation(@Nullable Future<?> related) {
      if (related != null & this.isCancelled()) {
         related.cancel(this.wasInterrupted());
      }
   }

   private void releaseWaiters() {
      AbstractFuture.Waiter head;
      do {
         head = this.waiters;
      } while (!ATOMIC_HELPER.casWaiters(this, head, AbstractFuture.Waiter.TOMBSTONE));

      for (AbstractFuture.Waiter currentWaiter = head; currentWaiter != null; currentWaiter = currentWaiter.next) {
         currentWaiter.unpark();
      }
   }

   private AbstractFuture.Listener clearListeners(AbstractFuture.Listener onto) {
      AbstractFuture.Listener head;
      do {
         head = this.listeners;
      } while (!ATOMIC_HELPER.casListeners(this, head, AbstractFuture.Listener.TOMBSTONE));

      AbstractFuture.Listener reversedList = onto;

      while (head != null) {
         AbstractFuture.Listener tmp = head;
         head = head.next;
         tmp.next = reversedList;
         reversedList = tmp;
      }

      return reversedList;
   }

   private static void executeListener(Runnable runnable, Executor executor) {
      try {
         executor.execute(runnable);
      } catch (RuntimeException var3) {
         log.log(Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, (Throwable)var3);
      }
   }

   private static CancellationException cancellationExceptionWithCause(@Nullable String message, @Nullable Throwable cause) {
      CancellationException exception = new CancellationException(message);
      exception.initCause(cause);
      return exception;
   }

   static {
      AbstractFuture.AtomicHelper helper;
      try {
         helper = new AbstractFuture.UnsafeAtomicHelper();
      } catch (Throwable var4) {
         try {
            helper = new AbstractFuture.SafeAtomicHelper(
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.Waiter.class, Thread.class, "thread"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.Waiter.class, AbstractFuture.Waiter.class, "next"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, AbstractFuture.Waiter.class, "waiters"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, AbstractFuture.Listener.class, "listeners"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, Object.class, "value")
            );
         } catch (Throwable var3) {
            log.log(Level.SEVERE, "UnsafeAtomicHelper is broken!", var4);
            log.log(Level.SEVERE, "SafeAtomicHelper is broken!", var3);
            helper = new AbstractFuture.SynchronizedHelper();
         }
      }

      ATOMIC_HELPER = helper;
      Class<LockSupport> unsafeFailure = LockSupport.class;
      NULL = new Object();
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void putThread(AbstractFuture.Waiter var1, Thread var2);

      abstract void putNext(AbstractFuture.Waiter var1, AbstractFuture.Waiter var2);

      abstract boolean casWaiters(AbstractFuture<?> var1, AbstractFuture.Waiter var2, AbstractFuture.Waiter var3);

      abstract boolean casListeners(AbstractFuture<?> var1, AbstractFuture.Listener var2, AbstractFuture.Listener var3);

      abstract boolean casValue(AbstractFuture<?> var1, Object var2, Object var3);
   }

   private static final class Cancellation {
      final boolean wasInterrupted;
      @Nullable
      final Throwable cause;

      Cancellation(boolean wasInterrupted, @Nullable Throwable cause) {
         this.wasInterrupted = wasInterrupted;
         this.cause = cause;
      }
   }

   private static final class Failure {
      static final AbstractFuture.Failure FALLBACK_INSTANCE = new AbstractFuture.Failure(new Throwable("Failure occurred while trying to finish a future.") {
         @Override
         public synchronized Throwable fillInStackTrace() {
            return this;
         }
      });
      final Throwable exception;

      Failure(Throwable exception) {
         this.exception = Preconditions.checkNotNull(exception);
      }
   }

   private static final class Listener {
      static final AbstractFuture.Listener TOMBSTONE = new AbstractFuture.Listener(null, null);
      final Runnable task;
      final Executor executor;
      @Nullable
      AbstractFuture.Listener next;

      Listener(Runnable task, Executor executor) {
         this.task = task;
         this.executor = executor;
      }
   }

   private static final class SafeAtomicHelper extends AbstractFuture.AtomicHelper {
      final AtomicReferenceFieldUpdater<AbstractFuture.Waiter, Thread> waiterThreadUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture.Waiter, AbstractFuture.Waiter> waiterNextUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Waiter> waitersUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Listener> listenersUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater;

      SafeAtomicHelper(
         AtomicReferenceFieldUpdater<AbstractFuture.Waiter, Thread> waiterThreadUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture.Waiter, AbstractFuture.Waiter> waiterNextUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Waiter> waitersUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Listener> listenersUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater
      ) {
         this.waiterThreadUpdater = waiterThreadUpdater;
         this.waiterNextUpdater = waiterNextUpdater;
         this.waitersUpdater = waitersUpdater;
         this.listenersUpdater = listenersUpdater;
         this.valueUpdater = valueUpdater;
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         this.waiterThreadUpdater.lazySet(waiter, newValue);
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, AbstractFuture.Waiter newValue) {
         this.waiterNextUpdater.lazySet(waiter, newValue);
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, AbstractFuture.Waiter expect, AbstractFuture.Waiter update) {
         return this.waitersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         return this.listenersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
         return this.valueUpdater.compareAndSet(future, expect, update);
      }
   }

   private static final class SetFuture<V> implements Runnable {
      final AbstractFuture<V> owner;
      final ListenableFuture<? extends V> future;

      SetFuture(AbstractFuture<V> owner, ListenableFuture<? extends V> future) {
         this.owner = owner;
         this.future = future;
      }

      @Override
      public void run() {
         if (this.owner.value == this) {
            Object valueToSet = AbstractFuture.getFutureValue(this.future);
            if (AbstractFuture.ATOMIC_HELPER.casValue(this.owner, this, valueToSet)) {
               AbstractFuture.complete(this.owner);
            }
         }
      }
   }

   private static final class SynchronizedHelper extends AbstractFuture.AtomicHelper {
      private SynchronizedHelper() {
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         waiter.thread = newValue;
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, AbstractFuture.Waiter newValue) {
         waiter.next = newValue;
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, AbstractFuture.Waiter expect, AbstractFuture.Waiter update) {
         synchronized (future) {
            if (future.waiters == expect) {
               future.waiters = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         synchronized (future) {
            if (future.listeners == expect) {
               future.listeners = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
         synchronized (future) {
            if (future.value == expect) {
               future.value = update;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   abstract static class TrustedFuture<V> extends AbstractFuture<V> {
      @CanIgnoreReturnValue
      @Override
      public final V get() throws InterruptedException, ExecutionException {
         return super.get();
      }

      @CanIgnoreReturnValue
      @Override
      public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         return super.get(timeout, unit);
      }

      @Override
      public final boolean isDone() {
         return super.isDone();
      }

      @Override
      public final boolean isCancelled() {
         return super.isCancelled();
      }

      @Override
      public final void addListener(Runnable listener, Executor executor) {
         super.addListener(listener, executor);
      }

      @CanIgnoreReturnValue
      @Override
      public final boolean cancel(boolean mayInterruptIfRunning) {
         return super.cancel(mayInterruptIfRunning);
      }
   }

   private static final class UnsafeAtomicHelper extends AbstractFuture.AtomicHelper {
      static final Unsafe UNSAFE;
      static final long LISTENERS_OFFSET;
      static final long WAITERS_OFFSET;
      static final long VALUE_OFFSET;
      static final long WAITER_THREAD_OFFSET;
      static final long WAITER_NEXT_OFFSET;

      private UnsafeAtomicHelper() {
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         UNSAFE.putObject(waiter, WAITER_THREAD_OFFSET, newValue);
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, AbstractFuture.Waiter newValue) {
         UNSAFE.putObject(waiter, WAITER_NEXT_OFFSET, newValue);
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, AbstractFuture.Waiter expect, AbstractFuture.Waiter update) {
         return UNSAFE.compareAndSwapObject(future, WAITERS_OFFSET, expect, update);
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         return UNSAFE.compareAndSwapObject(future, LISTENERS_OFFSET, expect, update);
      }

      @Override
      boolean casValue(AbstractFuture<?> future, Object expect, Object update) {
         return UNSAFE.compareAndSwapObject(future, VALUE_OFFSET, expect, update);
      }

      static {
         Unsafe unsafe = null;

         try {
            unsafe = Unsafe.getUnsafe();
         } catch (SecurityException var5) {
            try {
               unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                  public Unsafe run() throws Exception {
                     Class<Unsafe> k = Unsafe.class;

                     for (Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                           return k.cast(x);
                        }
                     }

                     throw new NoSuchFieldError("the Unsafe");
                  }
               });
            } catch (PrivilegedActionException var4) {
               throw new RuntimeException("Could not initialize intrinsics", var4.getCause());
            }
         }

         try {
            Class<?> abstractFuture = AbstractFuture.class;
            WAITERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("waiters"));
            LISTENERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("listeners"));
            VALUE_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("value"));
            WAITER_THREAD_OFFSET = unsafe.objectFieldOffset(AbstractFuture.Waiter.class.getDeclaredField("thread"));
            WAITER_NEXT_OFFSET = unsafe.objectFieldOffset(AbstractFuture.Waiter.class.getDeclaredField("next"));
            UNSAFE = unsafe;
         } catch (Exception var3) {
            Throwables.throwIfUnchecked(var3);
            throw new RuntimeException(var3);
         }
      }
   }

   private static final class Waiter {
      static final AbstractFuture.Waiter TOMBSTONE = new AbstractFuture.Waiter(false);
      @Nullable
      volatile Thread thread;
      @Nullable
      volatile AbstractFuture.Waiter next;

      Waiter(boolean unused) {
      }

      Waiter() {
         AbstractFuture.ATOMIC_HELPER.putThread(this, Thread.currentThread());
      }

      void setNext(AbstractFuture.Waiter next) {
         AbstractFuture.ATOMIC_HELPER.putNext(this, next);
      }

      void unpark() {
         Thread w = this.thread;
         if (w != null) {
            this.thread = null;
            LockSupport.unpark(w);
         }
      }
   }
}
