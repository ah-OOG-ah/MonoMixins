package org.spongepowered.libraries.com.google.common.graph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.spongepowered.libraries.com.google.common.collect.BiMap;
import org.spongepowered.libraries.com.google.common.collect.HashBiMap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableBiMap;

final class DirectedNetworkConnections<N, E> extends AbstractDirectedNetworkConnections<N, E> {
   protected DirectedNetworkConnections(Map<E, N> inEdgeMap, Map<E, N> outEdgeMap, int selfLoopCount) {
      super(inEdgeMap, outEdgeMap, selfLoopCount);
   }

   static <N, E> DirectedNetworkConnections<N, E> of() {
      return new DirectedNetworkConnections<>(HashBiMap.create(2), HashBiMap.create(2), 0);
   }

   static <N, E> DirectedNetworkConnections<N, E> ofImmutable(Map<E, N> inEdges, Map<E, N> outEdges, int selfLoopCount) {
      return new DirectedNetworkConnections<>(ImmutableBiMap.copyOf(inEdges), ImmutableBiMap.copyOf(outEdges), selfLoopCount);
   }

   @Override
   public Set<N> predecessors() {
      return Collections.unmodifiableSet(((BiMap)this.inEdgeMap).values());
   }

   @Override
   public Set<N> successors() {
      return Collections.unmodifiableSet(((BiMap)this.outEdgeMap).values());
   }

   @Override
   public Set<E> edgesConnecting(Object node) {
      return new EdgesConnecting<>(((BiMap)this.outEdgeMap).inverse(), node);
   }
}
