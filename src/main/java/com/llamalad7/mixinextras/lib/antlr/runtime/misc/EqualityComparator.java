package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

public interface EqualityComparator<T> {
   int hashCode(T var1);

   boolean equals(T var1, T var2);
}
