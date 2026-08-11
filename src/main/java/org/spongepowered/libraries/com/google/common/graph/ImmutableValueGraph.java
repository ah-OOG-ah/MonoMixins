package org.spongepowered.libraries.com.google.common.graph;

import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.Maps;

@Beta
public final class ImmutableValueGraph<N, V> extends ImmutableGraph.ValueBackedImpl<N, V> implements ValueGraph<N, V> {
   private ImmutableValueGraph(ValueGraph<N, V> graph) {
      super(ValueGraphBuilder.from(graph), getNodeConnections(graph), graph.edges().size());
   }

   public static <N, V> ImmutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
      return graph instanceof ImmutableValueGraph ? (ImmutableValueGraph)graph : new ImmutableValueGraph<>(graph);
   }

   @Deprecated
   public static <N, V> ImmutableValueGraph<N, V> copyOf(ImmutableValueGraph<N, V> graph) {
      return Preconditions.checkNotNull(graph);
   }

   private static <N, V> ImmutableMap<N, GraphConnections<N, V>> getNodeConnections(ValueGraph<N, V> graph) {
      ImmutableMap.Builder<N, GraphConnections<N, V>> nodeConnections = ImmutableMap.builder();

      for (N node : graph.nodes()) {
         nodeConnections.put(node, connectionsOf(graph, node));
      }

      return nodeConnections.build();
   }

   private static <N, V> GraphConnections<N, V> connectionsOf(final ValueGraph<N, V> graph, final N node) {
      Function<N, V> successorNodeToValueFn = new Function<N, V>() {
         @Override
         public V apply(N successorNode) {
            return graph.edgeValue(node, successorNode);
         }
      };
      return (GraphConnections<N, V>)(graph.isDirected()
         ? DirectedGraphConnections.ofImmutable(graph.predecessors(node), Maps.asMap(graph.successors(node), successorNodeToValueFn))
         : UndirectedGraphConnections.ofImmutable(Maps.asMap(graph.adjacentNodes(node), successorNodeToValueFn)));
   }

   @Override
   public V edgeValue(Object nodeU, Object nodeV) {
      return this.backingValueGraph.edgeValue(nodeU, nodeV);
   }

   @Override
   public V edgeValueOrDefault(Object nodeU, Object nodeV, @Nullable V defaultValue) {
      return this.backingValueGraph.edgeValueOrDefault(nodeU, nodeV, defaultValue);
   }

   @Override
   public String toString() {
      return this.backingValueGraph.toString();
   }
}
