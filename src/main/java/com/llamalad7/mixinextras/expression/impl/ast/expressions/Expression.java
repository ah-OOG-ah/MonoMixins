package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public abstract class Expression {
   protected final ExpressionSource src;

   public Expression(ExpressionSource src) {
      this.src = src;
   }

   public ExpressionSource getSrc() {
      return this.src;
   }

   public final boolean matches(FlowValue node, ExpressionContext ctx) {
      boolean result = this.matchesImpl(node, ctx);
      ctx.reportMatchStatus(node, this, result);
      return result;
   }

   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      return false;
   }

   protected void capture(FlowValue node, ExpressionContext ctx) {
      ctx.capture(node, this);
   }

   protected boolean inputsMatch(FlowValue node, ExpressionContext ctx, Expression... values) {
      return this.inputsMatch(node, ctx, false, values);
   }

   protected boolean inputsMatch(FlowValue node, ExpressionContext ctx, boolean allowIncomplete, Expression... values) {
      return this.inputsMatch(0, node, ctx, allowIncomplete, values);
   }

   protected boolean inputsMatch(int start, FlowValue node, ExpressionContext ctx, Expression... values) {
      return this.inputsMatch(start, node, ctx, false, values);
   }

   protected boolean inputsMatch(int start, FlowValue node, ExpressionContext ctx, boolean allowIncomplete, Expression... values) {
      ctx.reportPartialMatch(node, this);
      int required = node.inputCount() - start;
      if ((!allowIncomplete || values.length >= required) && values.length != required) {
         return false;
      } else {
         for (int i = 0; i < values.length; i++) {
            Expression value = values[i];
            if (!value.matches(node.getInput(i + start), ctx)) {
               return false;
            }
         }

         return true;
      }
   }

   public interface OutputSink {
      void capture(FlowValue var1, Expression var2, ExpressionContext var3);

      void decorate(AbstractInsnNode var1, String var2, Object var3);

      void decorateInjectorSpecific(AbstractInsnNode var1, String var2, Object var3);

      default void reportMatchStatus(FlowValue node, Expression expr, boolean matched) {
      }

      default void reportPartialMatch(FlowValue node, Expression expr) {
      }
   }
}
