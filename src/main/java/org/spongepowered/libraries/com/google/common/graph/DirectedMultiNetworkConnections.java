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

final class DirectedMultiNetworkConnections<N, E> extends AbstractDirectedNetworkConnections<N, E> {
   @LazyInit
   private transient Reference<Multiset<N>> predecessorsReference;
   @LazyInit
   private transient Reference<Multiset<N>> successorsReference;

   private DirectedMultiNetworkConnections(Map<E, N> inEdges, Map<E, N> outEdges, int selfLoopCount) {
      super(inEdges, outEdges, selfLoopCount);
   }

   static <N, E> DirectedMultiNetworkConnections<N, E> of() {
      return new DirectedMultiNetworkConnections<>(new HashMap<>(2, 1.0F), new HashMap<>(2, 1.0F), 0);
   }

   static <N, E> DirectedMultiNetworkConnections<N, E> ofImmutable(Map<E, N> inEdges, Map<E, N> outEdges, int selfLoopCount) {
      return new DirectedMultiNetworkConnections<>(ImmutableMap.copyOf(inEdges), ImmutableMap.copyOf(outEdges), selfLoopCount);
   }

   @Override
   public Set<N> predecessors() {
      return Collections.unmodifiableSet(this.predecessorsMultiset().elementSet());
   }

   private Multiset<N> predecessorsMultiset() {
      Multiset<N> predecessors = getReference(this.predecessorsReference);
      if (predecessors == null) {
         predecessors = HashMultiset.create(this.inEdgeMap.values());
         this.predecessorsReference = new SoftReference<>(predecessors);
      }

      return predecessors;
   }

   @Override
   public Set<N> successors() {
      return Collections.unmodifiableSet(this.successorsMultiset().elementSet());
   }

   private Multiset<N> successorsMultiset() {
      Multiset<N> successors = getReference(this.successorsReference);
      if (successors == null) {
         successors = HashMultiset.create(this.outEdgeMap.values());
         this.successorsReference = new SoftReference<>(successors);
      }

      return successors;
   }

   @Override
   public Set<E> edgesConnecting(final Object node) {
      return new MultiEdgesConnecting<E>(this.outEdgeMap, node) {
         @Override
         public int size() {
            return DirectedMultiNetworkConnections.this.successorsMultiset().count(node);
         }
      };
   }

   @Override
   public N removeInEdge(Object edge, boolean isSelfLoop) {
      N node = super.removeInEdge(edge, isSelfLoop);
      Multiset<N> predecessors = getReference(this.predecessorsReference);
      if (predecessors != null) {
         Preconditions.checkState(predecessors.remove(node));
      }

      return node;
   }

   @Override
   public N removeOutEdge(Object edge) {
      N node = super.removeOutEdge(edge);
      Multiset<N> successors = getReference(this.successorsReference);
      if (successors != null) {
         Preconditions.checkState(successors.remove(node));
      }

      return node;
   }

   @Override
   public void addInEdge(E edge, N node, boolean isSelfLoop) {
      super.addInEdge(edge, node, isSelfLoop);
      Multiset<N> predecessors = getReference(this.predecessorsReference);
      if (predecessors != null) {
         Preconditions.checkState(predecessors.add(node));
      }
   }

   @Override
   public void addOutEdge(E edge, N node) {
      super.addOutEdge(edge, node);
      Multiset<N> successors = getReference(this.successorsReference);
      if (successors != null) {
         Preconditions.checkState(successors.add(node));
      }
   }

   @Nullable
   private static <T> T getReference(@Nullable Reference<T> reference) {
      return reference == null ? null : reference.get();
   }
}
