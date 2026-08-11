package org.spongepowered.libraries.com.google.common.graph;

import org.spongepowered.libraries.com.google.common.base.Optional;

abstract class AbstractGraphBuilder<N> {
   final boolean directed;
   boolean allowsSelfLoops = false;
   ElementOrder<N> nodeOrder = ElementOrder.insertion();
   Optional<Integer> expectedNodeCount = Optional.absent();

   AbstractGraphBuilder(boolean directed) {
      this.directed = directed;
   }
}
