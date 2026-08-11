package org.spongepowered.libraries.com.google.common.util.concurrent;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@Beta
@GwtCompatible
public interface AsyncCallable<V> {
   ListenableFuture<V> call() throws Exception;
}
