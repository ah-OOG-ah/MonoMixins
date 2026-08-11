package org.spongepowered.libraries.com.google.common.graph;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.errorprone.annotations.CompatibleWith;

@Beta
public interface ValueGraph<N, V> extends Graph<N> {
   V edgeValue(@CompatibleWith("N") Object var1, @CompatibleWith("N") Object var2);

   V edgeValueOrDefault(@CompatibleWith("N") Object var1, @CompatibleWith("N") Object var2, @Nullable V var3);

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();
}
