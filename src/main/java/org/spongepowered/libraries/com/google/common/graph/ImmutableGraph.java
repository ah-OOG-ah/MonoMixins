package org.spongepowered.libraries.com.google.common.graph;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Functions;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.Maps;

@Beta
public abstract class ImmutableGraph<N> extends ForwardingGraph<N> {
   ImmutableGraph() {
   }

   public static <N> ImmutableGraph<N> copyOf(Graph<N> graph) {
      return (ImmutableGraph<N>)(graph instanceof ImmutableGraph
         ? (ImmutableGraph)graph
         : new ImmutableGraph.ValueBackedImpl<>(GraphBuilder.from(graph), getNodeConnections(graph), graph.edges().size()));
   }

   @Deprecated
   public static <N> ImmutableGraph<N> copyOf(ImmutableGraph<N> graph) {
      return Preconditions.checkNotNull(graph);
   }

   private static <N> ImmutableMap<N, GraphConnections<N, GraphConstants.Presence>> getNodeConnections(Graph<N> graph) {
      ImmutableMap.Builder<N, GraphConnections<N, GraphConstants.Presence>> nodeConnections = ImmutableMap.builder();

      for (N node : graph.nodes()) {
         nodeConnections.put(node, connectionsOf(graph, node));
      }

      return nodeConnections.build();
   }

   private static <N> GraphConnections<N, GraphConstants.Presence> connectionsOf(Graph<N> graph, N node) {
      Function<Object, GraphConstants.Presence> edgeValueFn = Functions.constant(GraphConstants.Presence.EDGE_EXISTS);
      return (GraphConnections<N, GraphConstants.Presence>)(graph.isDirected()
         ? DirectedGraphConnections.ofImmutable(graph.predecessors(node), Maps.asMap(graph.successors(node), edgeValueFn))
         : UndirectedGraphConnections.ofImmutable(Maps.asMap(graph.adjacentNodes(node), edgeValueFn)));
   }

   static class ValueBackedImpl<N, V> extends ImmutableGraph<N> {
      protected final ValueGraph<N, V> backingValueGraph;

      ValueBackedImpl(AbstractGraphBuilder<? super N> builder, ImmutableMap<N, GraphConnections<N, V>> nodeConnections, long edgeCount) {
         this.backingValueGraph = new ConfigurableValueGraph<>(builder, nodeConnections, edgeCount);
      }

      @Override
      protected Graph<N> delegate() {
         return this.backingValueGraph;
      }
   }
}
