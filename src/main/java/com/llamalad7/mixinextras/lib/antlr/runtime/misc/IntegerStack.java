package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

public class IntegerStack extends IntegerList {
   public final void push(int value) {
      this.add(value);
   }

   public final int pop() {
      return this.removeAt(this.size() - 1);
   }

   public final int peek() {
      return this.get(this.size() - 1);
   }
}
