package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(serializable = true)
final class UsingToStringOrdering extends Ordering<Object> implements Serializable {
   static final UsingToStringOrdering INSTANCE = new UsingToStringOrdering();
   private static final long serialVersionUID = 0L;

   @Override
   public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
   }

   private Object readResolve() {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "Ordering.usingToString()";
   }

   private UsingToStringOrdering() {
   }
}
