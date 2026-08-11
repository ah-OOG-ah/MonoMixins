package org.spongepowered.libraries.com.google.common.graph;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.AbstractIterator;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.UnmodifiableIterator;

final class DirectedGraphConnections<N, V> implements GraphConnections<N, V> {
   private static final Object PRED = new Object();
   private final Map<N, Object> adjacentNodeValues;
   private int predecessorCount;
   private int successorCount;

   private DirectedGraphConnections(Map<N, Object> adjacentNodeValues, int predecessorCount, int successorCount) {
      this.adjacentNodeValues = Preconditions.checkNotNull(adjacentNodeValues);
      this.predecessorCount = Graphs.checkNonNegative(predecessorCount);
      this.successorCount = Graphs.checkNonNegative(successorCount);
      Preconditions.checkState(predecessorCount <= adjacentNodeValues.size() && successorCount <= adjacentNodeValues.size());
   }

   static <N, V> DirectedGraphConnections<N, V> of() {
      int initialCapacity = 4;
      return new DirectedGraphConnections<>(new HashMap<>(initialCapacity, 1.0F), 0, 0);
   }

   static <N, V> DirectedGraphConnections<N, V> ofImmutable(Set<N> predecessors, Map<N, V> successorValues) {
      Map<N, Object> adjacentNodeValues = new HashMap<>();
      adjacentNodeValues.putAll(successorValues);

      for (N predecessor : predecessors) {
         Object value = adjacentNodeValues.put(predecessor, PRED);
         if (value != null) {
            adjacentNodeValues.put(predecessor, new DirectedGraphConnections.PredAndSucc(value));
         }
      }

      return new DirectedGraphConnections<>(ImmutableMap.copyOf(adjacentNodeValues), predecessors.size(), successorValues.size());
   }

   @Override
   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(this.adjacentNodeValues.keySet());
   }

   @Override
   public Set<N> predecessors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
            return new AbstractIterator<N>() {
               @Override
               protected N computeNext() {
                  while (entries.hasNext()) {
                     Entry<N, Object> entry = entries.next();
                     if (DirectedGraphConnections.isPredecessor(entry.getValue())) {
                        return entry.getKey();
                     }
                  }

                  return (N)this.endOfData();
               }
            };
         }

         @Override
         public int size() {
            return DirectedGraphConnections.this.predecessorCount;
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isPredecessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   @Override
   public Set<N> successors() {
      return new AbstractSet<N>() {
         public UnmodifiableIterator<N> iterator() {
            final Iterator<Entry<N, Object>> entries = DirectedGraphConnections.this.adjacentNodeValues.entrySet().iterator();
            return new AbstractIterator<N>() {
               @Override
               protected N computeNext() {
                  while (entries.hasNext()) {
                     Entry<N, Object> entry = entries.next();
                     if (DirectedGraphConnections.isSuccessor(entry.getValue())) {
                        return entry.getKey();
                     }
                  }

                  return (N)this.endOfData();
               }
            };
         }

         @Override
         public int size() {
            return DirectedGraphConnections.this.successorCount;
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return DirectedGraphConnections.isSuccessor(DirectedGraphConnections.this.adjacentNodeValues.get(obj));
         }
      };
   }

   @Override
   public V value(Object node) {
      Object value = this.adjacentNodeValues.get(node);
      if (value == PRED) {
         return null;
      } else {
         return (V)(value instanceof DirectedGraphConnections.PredAndSucc ? ((DirectedGraphConnections.PredAndSucc)value).successorValue : value);
      }
   }

   @Override
   public void removePredecessor(Object node) {
      Object previousValue = this.adjacentNodeValues.get(node);
      if (previousValue == PRED) {
         this.adjacentNodeValues.remove(node);
         Graphs.checkNonNegative(--this.predecessorCount);
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put((N)node, ((DirectedGraphConnections.PredAndSucc)previousValue).successorValue);
         Graphs.checkNonNegative(--this.predecessorCount);
      }
   }

   @Override
   public V removeSuccessor(Object node) {
      Object previousValue = this.adjacentNodeValues.get(node);
      if (previousValue == null || previousValue == PRED) {
         return null;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put((N)node, PRED);
         Graphs.checkNonNegative(--this.successorCount);
         return (V)((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
      } else {
         this.adjacentNodeValues.remove(node);
         Graphs.checkNonNegative(--this.successorCount);
         return (V)previousValue;
      }
   }

   @Override
   public void addPredecessor(N node, V unused) {
      Object previousValue = this.adjacentNodeValues.put(node, PRED);
      if (previousValue == null) {
         Graphs.checkPositive(++this.predecessorCount);
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, previousValue);
      } else if (previousValue != PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(previousValue));
         Graphs.checkPositive(++this.predecessorCount);
      }
   }

   @Override
   public V addSuccessor(N node, V value) {
      Object previousValue = this.adjacentNodeValues.put(node, value);
      if (previousValue == null) {
         Graphs.checkPositive(++this.successorCount);
         return null;
      } else if (previousValue instanceof DirectedGraphConnections.PredAndSucc) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         return (V)((DirectedGraphConnections.PredAndSucc)previousValue).successorValue;
      } else if (previousValue == PRED) {
         this.adjacentNodeValues.put(node, new DirectedGraphConnections.PredAndSucc(value));
         Graphs.checkPositive(++this.successorCount);
         return null;
      } else {
         return (V)previousValue;
      }
   }

   private static boolean isPredecessor(@Nullable Object value) {
      return value == PRED || value instanceof DirectedGraphConnections.PredAndSucc;
   }

   private static boolean isSuccessor(@Nullable Object value) {
      return value != PRED && value != null;
   }

   private static final class PredAndSucc {
      private final Object successorValue;

      PredAndSucc(Object successorValue) {
         this.successorValue = successorValue;
      }
   }
}
