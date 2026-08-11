package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.ast.identifiers.TypeIdentifier;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.postprocessing.LMFInfo;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import org.spongepowered.asm.lib.Type;

public class ConstructorReferenceExpression extends SimpleExpression {
   public final TypeIdentifier type;

   public ConstructorReferenceExpression(ExpressionSource src, TypeIdentifier type) {
      super(src);
      this.type = type;
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      LMFInfo info = node.getDecoration("lmfInfo");
      return info != null && info.type == LMFInfo.Type.INSTANTIATION ? this.type.matches(ctx.pool, Type.getObjectType(info.impl.getOwner())) : false;
   }
}
