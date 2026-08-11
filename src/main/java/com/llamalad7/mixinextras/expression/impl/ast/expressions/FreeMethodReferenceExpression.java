package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.ast.identifiers.MemberIdentifier;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.postprocessing.LMFInfo;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;

public class FreeMethodReferenceExpression extends SimpleExpression {
   public final MemberIdentifier name;

   public FreeMethodReferenceExpression(ExpressionSource src, MemberIdentifier name) {
      super(src);
      this.name = name;
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      LMFInfo info = node.getDecoration("lmfInfo");
      return info != null && info.type == LMFInfo.Type.FREE_METHOD ? this.name.matches(ctx.pool, node) : false;
   }
}
