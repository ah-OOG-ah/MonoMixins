package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.Type;

public class IntLiteralExpression extends SimpleExpression {
   public final long value;

   public IntLiteralExpression(ExpressionSource src, long value) {
      super(src);
      this.value = value;
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      if (!node.typeMatches(Type.INT_TYPE) && !node.typeMatches(Type.LONG_TYPE)) {
         return false;
      } else {
         Object cst = ExpressionASMUtils.getConstant(node.getInsn());
         return cst == null ? false : (cst instanceof Integer || cst instanceof Long) && ((Number)cst).longValue() == this.value;
      }
   }
}
