package com.llamalad7.mixinextras.lib.antlr.runtime;

public interface IntStream {
   void consume();

   int LA(int var1);

   int mark();

   void release(int var1);

   int index();

   void seek(int var1);

   int size();
}
