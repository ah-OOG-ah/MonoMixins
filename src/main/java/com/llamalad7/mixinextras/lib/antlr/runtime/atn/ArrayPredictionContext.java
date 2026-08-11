package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import java.util.Arrays;

public class ArrayPredictionContext extends PredictionContext {
   public final PredictionContext[] parents;
   public final int[] returnStates;

   public ArrayPredictionContext(SingletonPredictionContext a) {
      this(new PredictionContext[]{a.parent}, new int[]{a.returnState});
   }

   public ArrayPredictionContext(PredictionContext[] parents, int[] returnStates) {
      super(calculateHashCode(parents, returnStates));

      assert parents != null && parents.length > 0;

      assert returnStates != null && returnStates.length > 0;

      this.parents = parents;
      this.returnStates = returnStates;
   }

   @Override
   public boolean isEmpty() {
      return this.returnStates[0] == Integer.MAX_VALUE;
   }

   @Override
   public int size() {
      return this.returnStates.length;
   }

   @Override
   public PredictionContext getParent(int index) {
      return this.parents[index];
   }

   @Override
   public int getReturnState(int index) {
      return this.returnStates[index];
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ArrayPredictionContext)) {
         return false;
      } else if (this.hashCode() != o.hashCode()) {
         return false;
      } else {
         ArrayPredictionContext a = (ArrayPredictionContext)o;
         return Arrays.equals(this.returnStates, a.returnStates) && Arrays.equals((Object[])this.parents, (Object[])a.parents);
      }
   }

   @Override
   public String toString() {
      if (this.isEmpty()) {
         return "[]";
      } else {
         StringBuilder buf = new StringBuilder();
         buf.append("[");

         for (int i = 0; i < this.returnStates.length; i++) {
            if (i > 0) {
               buf.append(", ");
            }

            if (this.returnStates[i] == Integer.MAX_VALUE) {
               buf.append("$");
            } else {
               buf.append(this.returnStates[i]);
               if (this.parents[i] != null) {
                  buf.append(' ');
                  buf.append(this.parents[i].toString());
               } else {
                  buf.append("null");
               }
            }
         }

         buf.append("]");
         return buf.toString();
      }
   }
}
