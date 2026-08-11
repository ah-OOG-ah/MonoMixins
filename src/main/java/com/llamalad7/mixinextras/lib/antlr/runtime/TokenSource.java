package com.llamalad7.mixinextras.lib.antlr.runtime;

public interface TokenSource {
   Token nextToken();

   int getLine();

   int getCharPositionInLine();

   CharStream getInputStream();

   TokenFactory<?> getTokenFactory();
}
