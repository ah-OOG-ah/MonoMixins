package org.spongepowered.libraries.com.google.common.cache;

import java.util.concurrent.atomic.AtomicLong;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Supplier;

@GwtCompatible(emulated = true)
final class LongAddables {
   private static final Supplier<LongAddable> SUPPLIER;

   public static LongAddable create() {
      return SUPPLIER.get();
   }

   static {
      Supplier<LongAddable> supplier;
      try {
         new LongAdder();
         supplier = new Supplier<LongAddable>() {
            public LongAddable get() {
               return new LongAdder();
            }
         };
      } catch (Throwable var2) {
         supplier = new Supplier<LongAddable>() {
            public LongAddable get() {
               return new LongAddables.PureJavaLongAddable();
            }
         };
      }

      SUPPLIER = supplier;
   }

   private static final class PureJavaLongAddable extends AtomicLong implements LongAddable {
      private PureJavaLongAddable() {
      }

      @Override
      public void increment() {
         this.getAndIncrement();
      }

      @Override
      public void add(long x) {
         this.getAndAdd(x);
      }

      @Override
      public long sum() {
         return this.get();
      }
   }
}
