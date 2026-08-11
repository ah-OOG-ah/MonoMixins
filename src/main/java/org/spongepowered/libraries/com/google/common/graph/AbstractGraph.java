package org.spongepowered.libraries.com.google.common.graph;

import java.util.AbstractSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.UnmodifiableIterator;
import org.spongepowered.libraries.com.google.common.math.IntMath;
import org.spongepowered.libraries.com.google.common.primitives.Ints;

@Beta
public abstract class AbstractGraph<N> implements Graph<N> {
   protected long edgeCount() {
      long degreeSum = 0L;

      for (N node : this.nodes()) {
         degreeSum += this.degree(node);
      }

      Preconditions.checkState((degreeSum & 1L) == 0L);
      return degreeSum >>> 1;
   }

   @Override
   public Set<EndpointPair<N>> edges() {
      return new AbstractSet<EndpointPair<N>>() {
         public UnmodifiableIterator<EndpointPair<N>> iterator() {
            return EndpointPairIterator.of(AbstractGraph.this);
         }

         @Override
         public int size() {
            return Ints.saturatedCast(AbstractGraph.this.edgeCount());
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            if (!(obj instanceof EndpointPair)) {
               return false;
            } else {
               EndpointPair<?> endpointPair = (EndpointPair<?>)obj;
               return AbstractGraph.this.isDirected() == endpointPair.isOrdered()
                  && AbstractGraph.this.nodes().contains(endpointPair.nodeU())
                  && AbstractGraph.this.successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
            }
         }
      };
   }

   @Override
   public int degree(Object node) {
      if (this.isDirected()) {
         return IntMath.saturatedAdd(this.predecessors(node).size(), this.successors(node).size());
      } else {
         Set<N> neighbors = this.adjacentNodes(node);
         int selfLoopCount = this.allowsSelfLoops() && neighbors.contains(node) ? 1 : 0;
         return IntMath.saturatedAdd(neighbors.size(), selfLoopCount);
      }
   }

   @Override
   public int inDegree(Object node) {
      return this.isDirected() ? this.predecessors(node).size() : this.degree(node);
   }

   @Override
   public int outDegree(Object node) {
      return this.isDirected() ? this.successors(node).size() : this.degree(node);
   }

   @Override
   public String toString() {
      String propertiesString = String.format("isDirected: %s, allowsSelfLoops: %s", this.isDirected(), this.allowsSelfLoops());
      return String.format("%s, nodes: %s, edges: %s", propertiesString, this.nodes(), this.edges());
   }
}
