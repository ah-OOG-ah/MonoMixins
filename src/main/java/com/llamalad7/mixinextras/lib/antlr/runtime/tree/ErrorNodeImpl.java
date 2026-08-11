package com.llamalad7.mixinextras.lib.antlr.runtime.tree;

import com.llamalad7.mixinextras.lib.antlr.runtime.Token;

public class ErrorNodeImpl extends TerminalNodeImpl implements ErrorNode {
   public ErrorNodeImpl(Token token) {
      super(token);
   }
}
