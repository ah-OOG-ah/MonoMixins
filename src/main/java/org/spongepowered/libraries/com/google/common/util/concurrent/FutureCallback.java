package org.spongepowered.libraries.com.google.common.util.concurrent;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface FutureCallback<V> {
   void onSuccess(@Nullable V var1);

   void onFailure(Throwable var1);
}
