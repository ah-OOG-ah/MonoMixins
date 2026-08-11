package com.llamalad7.mixinextras.lib.antlr.runtime;

import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ErrorNode;
import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ParseTree;
import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ParseTreeListener;
import com.llamalad7.mixinextras.lib.antlr.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParserRuleContext extends RuleContext {
   public static final ParserRuleContext EMPTY = new ParserRuleContext();
   public List<ParseTree> children;
   public Token start;
   public Token stop;
   public RecognitionException exception;

   public ParserRuleContext() {
   }

   public void copyFrom(ParserRuleContext ctx) {
      this.parent = ctx.parent;
      this.invokingState = ctx.invokingState;
      this.start = ctx.start;
      this.stop = ctx.stop;
      if (ctx.children != null) {
         this.children = new ArrayList<>();

         for (ParseTree child : ctx.children) {
            if (child instanceof ErrorNode) {
               this.addChild((ErrorNode)child);
            }
         }
      }
   }

   public ParserRuleContext(ParserRuleContext parent, int invokingStateNumber) {
      super(parent, invokingStateNumber);
   }

   public void enterRule(ParseTreeListener listener) {
   }

   public void exitRule(ParseTreeListener listener) {
   }

   public <T extends ParseTree> T addAnyChild(T t) {
      if (this.children == null) {
         this.children = new ArrayList<>();
      }

      this.children.add(t);
      return t;
   }

   public RuleContext addChild(RuleContext ruleInvocation) {
      return this.addAnyChild(ruleInvocation);
   }

   public TerminalNode addChild(TerminalNode t) {
      t.setParent(this);
      return this.addAnyChild(t);
   }

   public ErrorNode addErrorNode(ErrorNode errorNode) {
      errorNode.setParent(this);
      return this.addAnyChild(errorNode);
   }

   public void removeLastChild() {
      if (this.children != null) {
         this.children.remove(this.children.size() - 1);
      }
   }

   @Override
   public ParseTree getChild(int i) {
      return this.children != null && i >= 0 && i < this.children.size() ? this.children.get(i) : null;
   }

   public <T extends ParseTree> T getChild(Class<? extends T> ctxType, int i) {
      if (this.children != null && i >= 0 && i < this.children.size()) {
         int j = -1;

         for (ParseTree o : this.children) {
            if (ctxType.isInstance(o)) {
               if (++j == i) {
                  return (T)ctxType.cast(o);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public <T extends ParserRuleContext> T getRuleContext(Class<? extends T> ctxType, int i) {
      return this.getChild(ctxType, i);
   }

   public <T extends ParserRuleContext> List<T> getRuleContexts(Class<? extends T> ctxType) {
      if (this.children == null) {
         return Collections.emptyList();
      } else {
         List<T> contexts = null;

         for (ParseTree o : this.children) {
            if (ctxType.isInstance(o)) {
               if (contexts == null) {
                  contexts = new ArrayList<>();
               }

               contexts.add((T)ctxType.cast(o));
            }
         }

         return contexts == null ? Collections.emptyList() : contexts;
      }
   }

   @Override
   public int getChildCount() {
      return this.children != null ? this.children.size() : 0;
   }
}
