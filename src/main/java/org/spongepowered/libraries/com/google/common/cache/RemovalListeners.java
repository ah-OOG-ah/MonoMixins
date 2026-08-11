package org.spongepowered.libraries.com.google.common.cache;

import java.util.concurrent.Executor;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtIncompatible
public final class RemovalListeners {
   private RemovalListeners() {
   }

   public static <K, V> RemovalListener<K, V> asynchronous(final RemovalListener<K, V> listener, final Executor executor) {
      Preconditions.checkNotNull(listener);
      Preconditions.checkNotNull(executor);
      return new RemovalListener<K, V>() {
         @Override
         public void onRemoval(final RemovalNotification<K, V> notification) {
            executor.execute(new Runnable() {
               @Override
               public void run() {
                  listener.onRemoval(notification);
               }
            });
         }
      };
   }
}
