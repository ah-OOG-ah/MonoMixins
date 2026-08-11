package org.spongepowered.libraries.com.google.common.util.concurrent;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@FunctionalInterface
@GwtCompatible
public interface AsyncFunction<I, O> {
   ListenableFuture<O> apply(@Nullable I var1) throws Exception;
}
