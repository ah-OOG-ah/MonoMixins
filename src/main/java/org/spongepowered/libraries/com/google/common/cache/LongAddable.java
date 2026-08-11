package org.spongepowered.libraries.com.google.common.cache;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
interface LongAddable {
   void increment();

   void add(long var1);

   long sum();
}
