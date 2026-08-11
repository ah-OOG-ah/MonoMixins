package com.llamalad7.mixinextras.lib.antlr.runtime.tree;

import com.llamalad7.mixinextras.lib.antlr.runtime.RuleContext;

public interface ParseTree {
   void setParent(RuleContext var1);

   String getText();
}
