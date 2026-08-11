package com.llamalad7.mixinextras.lib.antlr.runtime.tree;

import com.llamalad7.mixinextras.lib.antlr.runtime.ParserRuleContext;

public interface ParseTreeListener {
   void visitTerminal(TerminalNode var1);

   void visitErrorNode(ErrorNode var1);

   void enterEveryRule(ParserRuleContext var1);

   void exitEveryRule(ParserRuleContext var1);
}
