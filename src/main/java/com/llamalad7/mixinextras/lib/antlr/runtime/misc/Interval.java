package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

public class Interval {
   public static final Interval INVALID = new Interval(-1, -2);
   static final Interval[] cache = new Interval[1001];
   public int a;
   public int b;

   public Interval(int a, int b) {
      this.a = a;
      this.b = b;
   }

   public static Interval of(int a, int b) {
      if (a == b && a >= 0 && a <= 1000) {
         if (cache[a] == null) {
            cache[a] = new Interval(a, a);
         }

         return cache[a];
      } else {
         return new Interval(a, b);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && o instanceof Interval) {
         Interval other = (Interval)o;
         return this.a == other.a && this.b == other.b;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int hash = 23;
      hash = hash * 31 + this.a;
      return hash * 31 + this.b;
   }

   public boolean startsBeforeDisjoint(Interval other) {
      return this.a < other.a && this.b < other.a;
   }

   public boolean startsAfterDisjoint(Interval other) {
      return this.a > other.b;
   }

   public boolean disjoint(Interval other) {
      return this.startsBeforeDisjoint(other) || this.startsAfterDisjoint(other);
   }

   public boolean adjacent(Interval other) {
      return this.a == other.b + 1 || this.b == other.a - 1;
   }

   public Interval union(Interval other) {
      return of(Math.min(this.a, other.a), Math.max(this.b, other.b));
   }

   @Override
   public String toString() {
      return this.a + ".." + this.b;
   }
}
