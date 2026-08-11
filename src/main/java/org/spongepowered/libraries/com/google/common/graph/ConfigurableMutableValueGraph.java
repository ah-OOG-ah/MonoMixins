package org.spongepowered.libraries.com.google.common.graph;

import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

final class ConfigurableMutableValueGraph<N, V> extends ConfigurableValueGraph<N, V> implements MutableValueGraph<N, V> {
   ConfigurableMutableValueGraph(AbstractGraphBuilder<? super N> builder) {
      super(builder);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean addNode(N node) {
      Preconditions.checkNotNull(node, "node");
      if (this.containsNode(node)) {
         return false;
      } else {
         this.addNodeInternal(node);
         return true;
      }
   }

   @CanIgnoreReturnValue
   private GraphConnections<N, V> addNodeInternal(N node) {
      GraphConnections<N, V> connections = this.newConnections();
      Preconditions.checkState(this.nodeConnections.put(node, connections) == null);
      return connections;
   }

   @CanIgnoreReturnValue
   @Override
   public V putEdgeValue(N nodeU, N nodeV, V value) {
      Preconditions.checkNotNull(nodeU, "nodeU");
      Preconditions.checkNotNull(nodeV, "nodeV");
      Preconditions.checkNotNull(value, "value");
      if (!this.allowsSelfLoops()) {
         Preconditions.checkArgument(
            !nodeU.equals(nodeV),
            "Cannot add self-loop edge on node %s, as self-loops are not allowed. To construct a graph that allows self-loops, call allowsSelfLoops(true) on the Builder.",
            nodeU
         );
      }

      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      if (connectionsU == null) {
         connectionsU = this.addNodeInternal(nodeU);
      }

      V previousValue = connectionsU.addSuccessor(nodeV, value);
      GraphConnections<N, V> connectionsV = this.nodeConnections.get(nodeV);
      if (connectionsV == null) {
         connectionsV = this.addNodeInternal(nodeV);
      }

      connectionsV.addPredecessor(nodeU, value);
      if (previousValue == null) {
         Graphs.checkPositive(++this.edgeCount);
      }

      return previousValue;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean removeNode(Object node) {
      Preconditions.checkNotNull(node, "node");
      GraphConnections<N, V> connections = this.nodeConnections.get(node);
      if (connections == null) {
         return false;
      } else {
         if (this.allowsSelfLoops() && connections.removeSuccessor(node) != null) {
            connections.removePredecessor(node);
            this.edgeCount--;
         }

         for (N successor : connections.successors()) {
            this.nodeConnections.getWithoutCaching(successor).removePredecessor(node);
            this.edgeCount--;
         }

         if (this.isDirected()) {
            for (N predecessor : connections.predecessors()) {
               Preconditions.checkState(this.nodeConnections.getWithoutCaching(predecessor).removeSuccessor(node) != null);
               this.edgeCount--;
            }
         }

         this.nodeConnections.remove(node);
         Graphs.checkNonNegative(this.edgeCount);
         return true;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public V removeEdge(Object nodeU, Object nodeV) {
      Preconditions.checkNotNull(nodeU, "nodeU");
      Preconditions.checkNotNull(nodeV, "nodeV");
      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      GraphConnections<N, V> connectionsV = this.nodeConnections.get(nodeV);
      if (connectionsU != null && connectionsV != null) {
         V previousValue = connectionsU.removeSuccessor(nodeV);
         if (previousValue != null) {
            connectionsV.removePredecessor(nodeU);
            Graphs.checkNonNegative(--this.edgeCount);
         }

         return previousValue;
      } else {
         return null;
      }
   }

   private GraphConnections<N, V> newConnections() {
      return (GraphConnections<N, V>)(this.isDirected() ? DirectedGraphConnections.of() : UndirectedGraphConnections.of());
   }
}
