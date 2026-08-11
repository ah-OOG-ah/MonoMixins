package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.Type;

public class StringLiteralExpression extends SimpleExpression {
   public final String value;
   private final Integer charValue;

   public StringLiteralExpression(ExpressionSource src, String value) {
      super(src);
      this.value = value;
      if (value.length() == 1) {
         this.charValue = Integer.valueOf(value.charAt(0));
      } else {
         this.charValue = null;
      }
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      Object cst = ExpressionASMUtils.getConstant(node.getInsn());
      return cst == null ? false : cst.equals(this.value) || node.typeMatches(Type.CHAR_TYPE) && cst.equals(this.charValue);
   }
}
