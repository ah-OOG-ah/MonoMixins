package org.spongepowered.libraries.com.google.common.graph;

import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

interface GraphConnections<N, V> {
   Set<N> adjacentNodes();

   Set<N> predecessors();

   Set<N> successors();

   @Nullable
   V value(Object var1);

   void removePredecessor(Object var1);

   @CanIgnoreReturnValue
   V removeSuccessor(Object var1);

   void addPredecessor(N var1, V var2);

   @CanIgnoreReturnValue
   V addSuccessor(N var1, V var2);
}
