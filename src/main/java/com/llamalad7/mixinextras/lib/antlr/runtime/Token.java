package com.llamalad7.mixinextras.lib.antlr.runtime;

public interface Token {
   String getText();

   int getType();

   int getLine();

   int getCharPositionInLine();

   int getChannel();

   int getTokenIndex();

   int getStartIndex();

   int getStopIndex();

   TokenSource getTokenSource();
}
