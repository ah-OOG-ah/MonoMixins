package org.spongepowered.include.com.google.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.common.base.Throwables;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class Closer implements Closeable {
   private static final Closer.Suppressor SUPPRESSOR = (Closer.Suppressor)(Closer.SuppressingSuppressor.isAvailable()
      ? Closer.SuppressingSuppressor.INSTANCE
      : Closer.LoggingSuppressor.INSTANCE);
   final Closer.Suppressor suppressor;
   private final Deque<Closeable> stack = new ArrayDeque<>(4);
   private Throwable thrown;

   public static Closer create() {
      return new Closer(SUPPRESSOR);
   }

   Closer(Closer.Suppressor suppressor) {
      this.suppressor = Preconditions.checkNotNull(suppressor);
   }

   @CanIgnoreReturnValue
   public <C extends Closeable> C register(@Nullable C closeable) {
      if (closeable != null) {
         this.stack.addFirst(closeable);
      }

      return closeable;
   }

   public RuntimeException rethrow(Throwable e) throws IOException {
      Preconditions.checkNotNull(e);
      this.thrown = e;
      Throwables.propagateIfPossible(e, IOException.class);
      throw new RuntimeException(e);
   }

   @Override
   public void close() throws IOException {
      Throwable throwable = this.thrown;

      while (!this.stack.isEmpty()) {
         Closeable closeable = this.stack.removeFirst();

         try {
            closeable.close();
         } catch (Throwable var4) {
            if (throwable == null) {
               throwable = var4;
            } else {
               this.suppressor.suppress(closeable, throwable, var4);
            }
         }
      }

      if (this.thrown == null && throwable != null) {
         Throwables.propagateIfPossible(throwable, IOException.class);
         throw new AssertionError(throwable);
      }
   }

   static final class LoggingSuppressor implements Closer.Suppressor {
      static final Closer.LoggingSuppressor INSTANCE = new Closer.LoggingSuppressor();

      @Override
      public void suppress(Closeable closeable, Throwable thrown, Throwable suppressed) {
         Closeables.logger.log(Level.WARNING, "Suppressing exception thrown when closing " + closeable, suppressed);
      }
   }

   static final class SuppressingSuppressor implements Closer.Suppressor {
      static final Closer.SuppressingSuppressor INSTANCE = new Closer.SuppressingSuppressor();
      static final Method addSuppressed = getAddSuppressed();

      static boolean isAvailable() {
         return addSuppressed != null;
      }

      private static Method getAddSuppressed() {
         try {
            return Throwable.class.getMethod("addSuppressed", Throwable.class);
         } catch (Throwable var1) {
            return null;
         }
      }

      @Override
      public void suppress(Closeable closeable, Throwable thrown, Throwable suppressed) {
         if (thrown != suppressed) {
            try {
               addSuppressed.invoke(thrown, suppressed);
            } catch (Throwable var5) {
               Closer.LoggingSuppressor.INSTANCE.suppress(closeable, thrown, suppressed);
            }
         }
      }
   }

   interface Suppressor {
      void suppress(Closeable var1, Throwable var2, Throwable var3);
   }
}
