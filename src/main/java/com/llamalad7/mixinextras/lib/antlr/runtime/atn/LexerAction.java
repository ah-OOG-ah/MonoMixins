package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.Lexer;

public interface LexerAction {
   boolean isPositionDependent();

   void execute(Lexer var1);
}
