package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Supplier;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtIncompatible
public abstract class AbstractIdleService implements Service {
   private final Supplier<String> threadNameSupplier = new AbstractIdleService.ThreadNameSupplier();
   private final Service delegate = new AbstractIdleService.DelegateService();

   protected AbstractIdleService() {
   }

   protected abstract void startUp() throws Exception;

   protected abstract void shutDown() throws Exception;

   protected Executor executor() {
      return new Executor() {
         @Override
         public void execute(Runnable command) {
            MoreExecutors.newThread(AbstractIdleService.this.threadNameSupplier.get(), command).start();
         }
      };
   }

   @Override
   public String toString() {
      return this.serviceName() + " [" + this.state() + "]";
   }

   @Override
   public final boolean isRunning() {
      return this.delegate.isRunning();
   }

   @Override
   public final Service.State state() {
      return this.delegate.state();
   }

   @Override
   public final void addListener(Service.Listener listener, Executor executor) {
      this.delegate.addListener(listener, executor);
   }

   @Override
   public final Throwable failureCause() {
      return this.delegate.failureCause();
   }

   @CanIgnoreReturnValue
   @Override
   public final Service startAsync() {
      this.delegate.startAsync();
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public final Service stopAsync() {
      this.delegate.stopAsync();
      return this;
   }

   @Override
   public final void awaitRunning() {
      this.delegate.awaitRunning();
   }

   @Override
   public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
      this.delegate.awaitRunning(timeout, unit);
   }

   @Override
   public final void awaitTerminated() {
      this.delegate.awaitTerminated();
   }

   @Override
   public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
      this.delegate.awaitTerminated(timeout, unit);
   }

   protected String serviceName() {
      return this.getClass().getSimpleName();
   }

   private final class DelegateService extends AbstractService {
      private DelegateService() {
      }

      @Override
      protected final void doStart() {
         MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable() {
            @Override
            public void run() {
               try {
                  AbstractIdleService.this.startUp();
                  DelegateService.this.notifyStarted();
               } catch (Throwable var2) {
                  DelegateService.this.notifyFailed(var2);
               }
            }
         });
      }

      @Override
      protected final void doStop() {
         MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable() {
            @Override
            public void run() {
               try {
                  AbstractIdleService.this.shutDown();
                  DelegateService.this.notifyStopped();
               } catch (Throwable var2) {
                  DelegateService.this.notifyFailed(var2);
               }
            }
         });
      }

      @Override
      public String toString() {
         return AbstractIdleService.this.toString();
      }
   }

   private final class ThreadNameSupplier implements Supplier<String> {
      private ThreadNameSupplier() {
      }

      public String get() {
         return AbstractIdleService.this.serviceName() + " " + AbstractIdleService.this.state();
      }
   }
}
