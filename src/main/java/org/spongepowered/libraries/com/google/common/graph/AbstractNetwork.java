package org.spongepowered.libraries.com.google.common.graph;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.collect.ImmutableSet;
import org.spongepowered.libraries.com.google.common.collect.Iterators;
import org.spongepowered.libraries.com.google.common.collect.Maps;
import org.spongepowered.libraries.com.google.common.collect.Sets;
import org.spongepowered.libraries.com.google.common.math.IntMath;

@Beta
public abstract class AbstractNetwork<N, E> implements Network<N, E> {
   @Override
   public Graph<N> asGraph() {
      return new AbstractGraph<N>() {
         @Override
         public Set<N> nodes() {
            return AbstractNetwork.this.nodes();
         }

         @Override
         public Set<EndpointPair<N>> edges() {
            return (Set<EndpointPair<N>>)(AbstractNetwork.this.allowsParallelEdges()
               ? super.edges()
               : new AbstractSet<EndpointPair<N>>() {
                  @Override
                  public Iterator<EndpointPair<N>> iterator() {
                     return Iterators.transform(AbstractNetwork.this.edges().iterator(), new Function<E, EndpointPair<N>>() {
                        public EndpointPair<N> apply(E edge) {
                           return AbstractNetwork.this.incidentNodes(edge);
                        }
                     });
                  }

                  @Override
                  public int size() {
                     return AbstractNetwork.this.edges().size();
                  }

                  @Override
                  public boolean contains(@Nullable Object obj) {
                     if (!(obj instanceof EndpointPair)) {
                        return false;
                     } else {
                        EndpointPair<?> endpointPair = (EndpointPair<?>)obj;
                        return isDirected() == endpointPair.isOrdered()
                           && nodes().contains(endpointPair.nodeU())
                           && successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
                     }
                  }
               });
         }

         @Override
         public ElementOrder<N> nodeOrder() {
            return AbstractNetwork.this.nodeOrder();
         }

         @Override
         public boolean isDirected() {
            return AbstractNetwork.this.isDirected();
         }

         @Override
         public boolean allowsSelfLoops() {
            return AbstractNetwork.this.allowsSelfLoops();
         }

         @Override
         public Set<N> adjacentNodes(Object node) {
            return AbstractNetwork.this.adjacentNodes(node);
         }

         @Override
         public Set<N> predecessors(Object node) {
            return AbstractNetwork.this.predecessors(node);
         }

         @Override
         public Set<N> successors(Object node) {
            return AbstractNetwork.this.successors(node);
         }
      };
   }

   @Override
   public int degree(Object node) {
      return this.isDirected()
         ? IntMath.saturatedAdd(this.inEdges(node).size(), this.outEdges(node).size())
         : IntMath.saturatedAdd(this.incidentEdges(node).size(), this.edgesConnecting(node, node).size());
   }

   @Override
   public int inDegree(Object node) {
      return this.isDirected() ? this.inEdges(node).size() : this.degree(node);
   }

   @Override
   public int outDegree(Object node) {
      return this.isDirected() ? this.outEdges(node).size() : this.degree(node);
   }

   @Override
   public Set<E> adjacentEdges(Object edge) {
      EndpointPair<?> endpointPair = this.incidentNodes(edge);
      Set<E> endpointPairIncidentEdges = Sets.union(this.incidentEdges(endpointPair.nodeU()), this.incidentEdges(endpointPair.nodeV()));
      return Sets.difference(endpointPairIncidentEdges, ImmutableSet.of(edge));
   }

   @Override
   public String toString() {
      String propertiesString = String.format(
         "isDirected: %s, allowsParallelEdges: %s, allowsSelfLoops: %s", this.isDirected(), this.allowsParallelEdges(), this.allowsSelfLoops()
      );
      return String.format("%s, nodes: %s, edges: %s", propertiesString, this.nodes(), this.edgeIncidentNodesMap());
   }

   private Map<E, EndpointPair<N>> edgeIncidentNodesMap() {
      Function<E, EndpointPair<N>> edgeToIncidentNodesFn = new Function<E, EndpointPair<N>>() {
         public EndpointPair<N> apply(E edge) {
            return AbstractNetwork.this.incidentNodes(edge);
         }
      };
      return Maps.asMap(this.edges(), edgeToIncidentNodesFn);
   }
}
