package org.spongepowered.libraries.com.google.common.graph;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.CompatibleWith;

@Beta
public interface MutableNetwork<N, E> extends Network<N, E> {
   @CanIgnoreReturnValue
   boolean addNode(N var1);

   @CanIgnoreReturnValue
   boolean addEdge(N var1, N var2, E var3);

   @CanIgnoreReturnValue
   boolean removeNode(@CompatibleWith("N") Object var1);

   @CanIgnoreReturnValue
   boolean removeEdge(@CompatibleWith("E") Object var1);
}
