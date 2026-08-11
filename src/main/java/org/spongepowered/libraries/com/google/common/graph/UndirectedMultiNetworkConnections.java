package org.spongepowered.libraries.com.google.common.graph;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.HashMultiset;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.Multiset;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;

final class UndirectedMultiNetworkConnections<N, E> extends AbstractUndirectedNetworkConnections<N, E> {
   @LazyInit
   private transient Reference<Multiset<N>> adjacentNodesReference;

   private UndirectedMultiNetworkConnections(Map<E, N> incidentEdges) {
      super(incidentEdges);
   }

   static <N, E> UndirectedMultiNetworkConnections<N, E> of() {
      return new UndirectedMultiNetworkConnections<>(new HashMap<>(2, 1.0F));
   }

   static <N, E> UndirectedMultiNetworkConnections<N, E> ofImmutable(Map<E, N> incidentEdges) {
      return new UndirectedMultiNetworkConnections<>(ImmutableMap.copyOf(incidentEdges));
   }

   @Override
   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(this.adjacentNodesMultiset().elementSet());
   }

   private Multiset<N> adjacentNodesMultiset() {
      Multiset<N> adjacentNodes = getReference(this.adjacentNodesReference);
      if (adjacentNodes == null) {
         adjacentNodes = HashMultiset.create(this.incidentEdgeMap.values());
         this.adjacentNodesReference = new SoftReference<>(adjacentNodes);
      }

      return adjacentNodes;
   }

   @Override
   public Set<E> edgesConnecting(final Object node) {
      return new MultiEdgesConnecting<E>(this.incidentEdgeMap, node) {
         @Override
         public int size() {
            return UndirectedMultiNetworkConnections.this.adjacentNodesMultiset().count(node);
         }
      };
   }

   @Override
   public N removeInEdge(Object edge, boolean isSelfLoop) {
      return !isSelfLoop ? this.removeOutEdge(edge) : null;
   }

   @Override
   public N removeOutEdge(Object edge) {
      N node = super.removeOutEdge(edge);
      Multiset<N> adjacentNodes = getReference(this.adjacentNodesReference);
      if (adjacentNodes != null) {
         Preconditions.checkState(adjacentNodes.remove(node));
      }

      return node;
   }

   @Override
   public void addInEdge(E edge, N node, boolean isSelfLoop) {
      if (!isSelfLoop) {
         this.addOutEdge(edge, node);
      }
   }

   @Override
   public void addOutEdge(E edge, N node) {
      super.addOutEdge(edge, node);
      Multiset<N> adjacentNodes = getReference(this.adjacentNodesReference);
      if (adjacentNodes != null) {
         Preconditions.checkState(adjacentNodes.add(node));
      }
   }

   @Nullable
   private static <T> T getReference(@Nullable Reference<T> reference) {
      return reference == null ? null : reference.get();
   }
}
