package org.spongepowered.libraries.com.google.common.graph;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.Objects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.Iterables;
import org.spongepowered.libraries.com.google.common.collect.Maps;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public final class Graphs {
   private Graphs() {
   }

   public static boolean hasCycle(Graph<?> graph) {
      int numEdges = graph.edges().size();
      if (numEdges == 0) {
         return false;
      } else if (!graph.isDirected() && numEdges >= graph.nodes().size()) {
         return true;
      } else {
         Map<Object, Graphs.NodeVisitState> visitedNodes = Maps.newHashMapWithExpectedSize(graph.nodes().size());

         for (Object node : graph.nodes()) {
            if (subgraphHasCycle(graph, visitedNodes, node, null)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean hasCycle(Network<?, ?> network) {
      return !network.isDirected() && network.allowsParallelEdges() && network.edges().size() > network.asGraph().edges().size()
         ? true
         : hasCycle(network.asGraph());
   }

   private static boolean subgraphHasCycle(Graph<?> graph, Map<Object, Graphs.NodeVisitState> visitedNodes, Object node, @Nullable Object previousNode) {
      Graphs.NodeVisitState state = visitedNodes.get(node);
      if (state == Graphs.NodeVisitState.COMPLETE) {
         return false;
      } else if (state == Graphs.NodeVisitState.PENDING) {
         return true;
      } else {
         visitedNodes.put(node, Graphs.NodeVisitState.PENDING);

         for (Object nextNode : graph.successors(node)) {
            if (canTraverseWithoutReusingEdge(graph, nextNode, previousNode) && subgraphHasCycle(graph, visitedNodes, nextNode, node)) {
               return true;
            }
         }

         visitedNodes.put(node, Graphs.NodeVisitState.COMPLETE);
         return false;
      }
   }

   private static boolean canTraverseWithoutReusingEdge(Graph<?> graph, Object nextNode, @Nullable Object previousNode) {
      return graph.isDirected() || !Objects.equal(previousNode, nextNode);
   }

   public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
      MutableGraph<N> transitiveClosure = GraphBuilder.from(graph).allowsSelfLoops(true).build();
      if (graph.isDirected()) {
         for (N node : graph.nodes()) {
            for (N reachableNode : reachableNodes(graph, node)) {
               transitiveClosure.putEdge(node, reachableNode);
            }
         }
      } else {
         Set<N> visitedNodes = new HashSet<>();

         for (N node : graph.nodes()) {
            if (!visitedNodes.contains(node)) {
               Set<N> reachableNodes = reachableNodes(graph, node);
               visitedNodes.addAll(reachableNodes);
               int pairwiseMatch = 1;

               for (N nodeU : reachableNodes) {
                  for (N nodeV : Iterables.limit(reachableNodes, pairwiseMatch++)) {
                     transitiveClosure.putEdge(nodeU, nodeV);
                  }
               }
            }
         }
      }

      return transitiveClosure;
   }

   public static <N> Set<N> reachableNodes(Graph<N> graph, Object node) {
      Preconditions.checkArgument(graph.nodes().contains(node), "Node %s is not an element of this graph.", node);
      Set<N> visitedNodes = new LinkedHashSet<>();
      Queue<N> queuedNodes = new ArrayDeque<>();
      visitedNodes.add((N)node);
      queuedNodes.add((N)node);

      while (!queuedNodes.isEmpty()) {
         N currentNode = queuedNodes.remove();

         for (N successor : graph.successors(currentNode)) {
            if (visitedNodes.add(successor)) {
               queuedNodes.add(successor);
            }
         }
      }

      return Collections.unmodifiableSet(visitedNodes);
   }

   public static boolean equivalent(@Nullable Graph<?> graphA, @Nullable Graph<?> graphB) {
      if (graphA == graphB) {
         return true;
      } else {
         return graphA != null && graphB != null
            ? graphA.isDirected() == graphB.isDirected() && graphA.nodes().equals(graphB.nodes()) && graphA.edges().equals(graphB.edges())
            : false;
      }
   }

   public static boolean equivalent(@Nullable ValueGraph<?, ?> graphA, @Nullable ValueGraph<?, ?> graphB) {
      if (graphA == graphB) {
         return true;
      } else if (graphA != null && graphB != null) {
         if (graphA.isDirected() == graphB.isDirected() && graphA.nodes().equals(graphB.nodes()) && graphA.edges().equals(graphB.edges())) {
            for (EndpointPair<?> edge : graphA.edges()) {
               if (!graphA.edgeValue(edge.nodeU(), edge.nodeV()).equals(graphB.edgeValue(edge.nodeU(), edge.nodeV()))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean equivalent(@Nullable Network<?, ?> networkA, @Nullable Network<?, ?> networkB) {
      if (networkA == networkB) {
         return true;
      } else if (networkA != null && networkB != null) {
         if (networkA.isDirected() == networkB.isDirected() && networkA.nodes().equals(networkB.nodes()) && networkA.edges().equals(networkB.edges())) {
            for (Object edge : networkA.edges()) {
               if (!networkA.incidentNodes(edge).equals(networkB.incidentNodes(edge))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static <N> Graph<N> transpose(Graph<N> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (Graph<N>)(graph instanceof Graphs.TransposedGraph ? ((Graphs.TransposedGraph)graph).graph : new Graphs.TransposedGraph<>(graph));
      }
   }

   public static <N, V> ValueGraph<N, V> transpose(ValueGraph<N, V> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (ValueGraph<N, V>)(graph instanceof Graphs.TransposedValueGraph
            ? ((Graphs.TransposedValueGraph)graph).graph
            : new Graphs.TransposedValueGraph<>(graph));
      }
   }

   public static <N, E> Network<N, E> transpose(Network<N, E> network) {
      if (!network.isDirected()) {
         return network;
      } else {
         return (Network<N, E>)(network instanceof Graphs.TransposedNetwork
            ? ((Graphs.TransposedNetwork)network).network
            : new Graphs.TransposedNetwork<>(network));
      }
   }

   public static <N> MutableGraph<N> inducedSubgraph(Graph<N> graph, Iterable<? extends N> nodes) {
      MutableGraph<N> subgraph = GraphBuilder.from(graph).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (N successorNode : graph.successors(node)) {
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdge(node, successorNode);
            }
         }
      }

      return subgraph;
   }

   public static <N, V> MutableValueGraph<N, V> inducedSubgraph(ValueGraph<N, V> graph, Iterable<? extends N> nodes) {
      MutableValueGraph<N, V> subgraph = ValueGraphBuilder.from(graph).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (N successorNode : graph.successors(node)) {
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdgeValue(node, successorNode, graph.edgeValue(node, successorNode));
            }
         }
      }

      return subgraph;
   }

   public static <N, E> MutableNetwork<N, E> inducedSubgraph(Network<N, E> network, Iterable<? extends N> nodes) {
      MutableNetwork<N, E> subgraph = NetworkBuilder.from(network).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (E edge : network.outEdges(node)) {
            N successorNode = network.incidentNodes(edge).adjacentNode(node);
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.addEdge(node, successorNode, edge);
            }
         }
      }

      return subgraph;
   }

   public static <N> MutableGraph<N> copyOf(Graph<N> graph) {
      MutableGraph<N> copy = GraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();

      for (N node : graph.nodes()) {
         copy.addNode(node);
      }

      for (EndpointPair<N> edge : graph.edges()) {
         copy.putEdge(edge.nodeU(), edge.nodeV());
      }

      return copy;
   }

   public static <N, V> MutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
      MutableValueGraph<N, V> copy = ValueGraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();

      for (N node : graph.nodes()) {
         copy.addNode(node);
      }

      for (EndpointPair<N> edge : graph.edges()) {
         copy.putEdgeValue(edge.nodeU(), edge.nodeV(), graph.edgeValue(edge.nodeU(), edge.nodeV()));
      }

      return copy;
   }

   public static <N, E> MutableNetwork<N, E> copyOf(Network<N, E> network) {
      MutableNetwork<N, E> copy = NetworkBuilder.from(network).expectedNodeCount(network.nodes().size()).expectedEdgeCount(network.edges().size()).build();

      for (N node : network.nodes()) {
         copy.addNode(node);
      }

      for (E edge : network.edges()) {
         EndpointPair<N> endpointPair = network.incidentNodes(edge);
         copy.addEdge(endpointPair.nodeU(), endpointPair.nodeV(), edge);
      }

      return copy;
   }

   @CanIgnoreReturnValue
   static int checkNonNegative(int value) {
      Preconditions.checkArgument(value >= 0, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static int checkPositive(int value) {
      Preconditions.checkArgument(value > 0, "Not true that %s is positive.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkNonNegative(long value) {
      Preconditions.checkArgument(value >= 0L, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkPositive(long value) {
      Preconditions.checkArgument(value > 0L, "Not true that %s is positive.", value);
      return value;
   }

   private static enum NodeVisitState {
      PENDING,
      COMPLETE;
   }

   private static class TransposedGraph<N> extends AbstractGraph<N> {
      private final Graph<N> graph;

      TransposedGraph(Graph<N> graph) {
         this.graph = graph;
      }

      @Override
      public Set<N> nodes() {
         return this.graph.nodes();
      }

      @Override
      protected long edgeCount() {
         return this.graph.edges().size();
      }

      @Override
      public boolean isDirected() {
         return this.graph.isDirected();
      }

      @Override
      public boolean allowsSelfLoops() {
         return this.graph.allowsSelfLoops();
      }

      @Override
      public ElementOrder<N> nodeOrder() {
         return this.graph.nodeOrder();
      }

      @Override
      public Set<N> adjacentNodes(Object node) {
         return this.graph.adjacentNodes(node);
      }

      @Override
      public Set<N> predecessors(Object node) {
         return this.graph.successors(node);
      }

      @Override
      public Set<N> successors(Object node) {
         return this.graph.predecessors(node);
      }
   }

   private static class TransposedNetwork<N, E> extends AbstractNetwork<N, E> {
      private final Network<N, E> network;

      TransposedNetwork(Network<N, E> network) {
         this.network = network;
      }

      @Override
      public Set<N> nodes() {
         return this.network.nodes();
      }

      @Override
      public Set<E> edges() {
         return this.network.edges();
      }

      @Override
      public boolean isDirected() {
         return this.network.isDirected();
      }

      @Override
      public boolean allowsParallelEdges() {
         return this.network.allowsParallelEdges();
      }

      @Override
      public boolean allowsSelfLoops() {
         return this.network.allowsSelfLoops();
      }

      @Override
      public ElementOrder<N> nodeOrder() {
         return this.network.nodeOrder();
      }

      @Override
      public ElementOrder<E> edgeOrder() {
         return this.network.edgeOrder();
      }

      @Override
      public Set<N> adjacentNodes(Object node) {
         return this.network.adjacentNodes(node);
      }

      @Override
      public Set<N> predecessors(Object node) {
         return this.network.successors(node);
      }

      @Override
      public Set<N> successors(Object node) {
         return this.network.predecessors(node);
      }

      @Override
      public Set<E> incidentEdges(Object node) {
         return this.network.incidentEdges(node);
      }

      @Override
      public Set<E> inEdges(Object node) {
         return this.network.outEdges(node);
      }

      @Override
      public Set<E> outEdges(Object node) {
         return this.network.inEdges(node);
      }

      @Override
      public EndpointPair<N> incidentNodes(Object edge) {
         EndpointPair<N> endpointPair = this.network.incidentNodes(edge);
         return EndpointPair.of(this.network, endpointPair.nodeV(), endpointPair.nodeU());
      }

      @Override
      public Set<E> adjacentEdges(Object edge) {
         return this.network.adjacentEdges(edge);
      }

      @Override
      public Set<E> edgesConnecting(Object nodeU, Object nodeV) {
         return this.network.edgesConnecting(nodeV, nodeU);
      }
   }

   private static class TransposedValueGraph<N, V> extends AbstractValueGraph<N, V> {
      private final ValueGraph<N, V> graph;

      TransposedValueGraph(ValueGraph<N, V> graph) {
         this.graph = graph;
      }

      @Override
      public Set<N> nodes() {
         return this.graph.nodes();
      }

      @Override
      protected long edgeCount() {
         return this.graph.edges().size();
      }

      @Override
      public boolean isDirected() {
         return this.graph.isDirected();
      }

      @Override
      public boolean allowsSelfLoops() {
         return this.graph.allowsSelfLoops();
      }

      @Override
      public ElementOrder<N> nodeOrder() {
         return this.graph.nodeOrder();
      }

      @Override
      public Set<N> adjacentNodes(Object node) {
         return this.graph.adjacentNodes(node);
      }

      @Override
      public Set<N> predecessors(Object node) {
         return this.graph.successors(node);
      }

      @Override
      public Set<N> successors(Object node) {
         return this.graph.predecessors(node);
      }

      @Override
      public V edgeValue(Object nodeU, Object nodeV) {
         return this.graph.edgeValue(nodeV, nodeU);
      }

      @Override
      public V edgeValueOrDefault(Object nodeU, Object nodeV, @Nullable V defaultValue) {
         return this.graph.edgeValueOrDefault(nodeV, nodeU, defaultValue);
      }
   }
}
