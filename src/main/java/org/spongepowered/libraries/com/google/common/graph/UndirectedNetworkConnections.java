package org.spongepowered.libraries.com.google.common.graph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.spongepowered.libraries.com.google.common.collect.BiMap;
import org.spongepowered.libraries.com.google.common.collect.HashBiMap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableBiMap;

final class UndirectedNetworkConnections<N, E> extends AbstractUndirectedNetworkConnections<N, E> {
   protected UndirectedNetworkConnections(Map<E, N> incidentEdgeMap) {
      super(incidentEdgeMap);
   }

   static <N, E> UndirectedNetworkConnections<N, E> of() {
      return new UndirectedNetworkConnections<>(HashBiMap.create(2));
   }

   static <N, E> UndirectedNetworkConnections<N, E> ofImmutable(Map<E, N> incidentEdges) {
      return new UndirectedNetworkConnections<>(ImmutableBiMap.copyOf(incidentEdges));
   }

   @Override
   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(((BiMap)this.incidentEdgeMap).values());
   }

   @Override
   public Set<E> edgesConnecting(Object node) {
      return new EdgesConnecting<>(((BiMap)this.incidentEdgeMap).inverse(), node);
   }
}
