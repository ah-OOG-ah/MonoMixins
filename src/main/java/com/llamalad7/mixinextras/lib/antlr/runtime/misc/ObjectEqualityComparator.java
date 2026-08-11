package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

public final class ObjectEqualityComparator extends AbstractEqualityComparator<Object> {
   public static final ObjectEqualityComparator INSTANCE = new ObjectEqualityComparator();

   @Override
   public int hashCode(Object obj) {
      return obj == null ? 0 : obj.hashCode();
   }

   @Override
   public boolean equals(Object a, Object b) {
      return a == null ? b == null : a.equals(b);
   }
}
