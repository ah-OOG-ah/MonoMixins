package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.Type;

public class BooleanLiteralExpression extends SimpleExpression {
   public final boolean value;

   public BooleanLiteralExpression(ExpressionSource src, boolean value) {
      super(src);
      this.value = value;
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      if (!node.typeMatches(Type.BOOLEAN_TYPE)) {
         return false;
      } else {
         Object cst = ExpressionASMUtils.getConstant(node.getInsn());
         return cst == null ? false : cst.equals(this.value ? 1 : 0);
      }
   }
}
