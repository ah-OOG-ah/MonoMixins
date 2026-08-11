package org.spongepowered.libraries.com.google.common.graph;

import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.errorprone.annotations.CompatibleWith;

@Beta
public interface Network<N, E> {
   Set<N> nodes();

   Set<E> edges();

   Graph<N> asGraph();

   boolean isDirected();

   boolean allowsParallelEdges();

   boolean allowsSelfLoops();

   ElementOrder<N> nodeOrder();

   ElementOrder<E> edgeOrder();

   Set<N> adjacentNodes(@CompatibleWith("N") Object var1);

   Set<N> predecessors(@CompatibleWith("N") Object var1);

   Set<N> successors(@CompatibleWith("N") Object var1);

   Set<E> incidentEdges(@CompatibleWith("N") Object var1);

   Set<E> inEdges(@CompatibleWith("N") Object var1);

   Set<E> outEdges(@CompatibleWith("N") Object var1);

   int degree(@CompatibleWith("N") Object var1);

   int inDegree(@CompatibleWith("N") Object var1);

   int outDegree(@CompatibleWith("N") Object var1);

   EndpointPair<N> incidentNodes(@CompatibleWith("E") Object var1);

   Set<E> adjacentEdges(@CompatibleWith("E") Object var1);

   Set<E> edgesConnecting(@CompatibleWith("N") Object var1, @CompatibleWith("N") Object var2);

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();
}
