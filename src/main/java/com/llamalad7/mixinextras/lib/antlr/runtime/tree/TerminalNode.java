package com.llamalad7.mixinextras.lib.antlr.runtime.tree;

import com.llamalad7.mixinextras.lib.antlr.runtime.Token;

public interface TerminalNode extends ParseTree {
   Token getSymbol();
}
