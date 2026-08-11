package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public class SingletonPredictionContext extends PredictionContext {
   public final PredictionContext parent;
   public final int returnState;

   SingletonPredictionContext(PredictionContext parent, int returnState) {
      super(parent != null ? calculateHashCode(parent, returnState) : calculateEmptyHashCode());

      assert returnState != -1;

      this.parent = parent;
      this.returnState = returnState;
   }

   public static SingletonPredictionContext create(PredictionContext parent, int returnState) {
      return (SingletonPredictionContext)(returnState == Integer.MAX_VALUE && parent == null
         ? EmptyPredictionContext.Instance
         : new SingletonPredictionContext(parent, returnState));
   }

   @Override
   public int size() {
      return 1;
   }

   @Override
   public PredictionContext getParent(int index) {
      assert index == 0;

      return this.parent;
   }

   @Override
   public int getReturnState(int index) {
      assert index == 0;

      return this.returnState;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof SingletonPredictionContext)) {
         return false;
      } else if (this.hashCode() != o.hashCode()) {
         return false;
      } else {
         SingletonPredictionContext s = (SingletonPredictionContext)o;
         return this.returnState == s.returnState && this.parent != null && this.parent.equals(s.parent);
      }
   }

   @Override
   public String toString() {
      String up = this.parent != null ? this.parent.toString() : "";
      if (up.length() == 0) {
         return this.returnState == Integer.MAX_VALUE ? "$" : String.valueOf(this.returnState);
      } else {
         return this.returnState + " " + up;
      }
   }
}
