package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.Type;

public abstract class SimpleExpression extends Expression {
   public SimpleExpression(ExpressionSource src) {
      super(src);
   }

   @Override
   public void capture(FlowValue node, ExpressionContext ctx) {
      Type type = node.getType();
      if (type.equals(ExpressionASMUtils.BOTTOM_TYPE)) {
         type = ExpressionASMUtils.OBJECT_TYPE;
      }

      if (!type.equals(Type.VOID_TYPE)) {
         ctx.decorate(node.getInsn(), "mixinextras_simpleExpressionType", type);
      }

      super.capture(node, ctx);
   }
}
