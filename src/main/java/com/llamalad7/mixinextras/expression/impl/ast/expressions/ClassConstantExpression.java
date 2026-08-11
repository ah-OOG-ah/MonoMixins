package com.llamalad7.mixinextras.expression.impl.ast.expressions;

import com.llamalad7.mixinextras.expression.impl.ExpressionSource;
import com.llamalad7.mixinextras.expression.impl.ast.identifiers.TypeIdentifier;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;

public class ClassConstantExpression extends SimpleExpression {
   public final TypeIdentifier type;

   public ClassConstantExpression(ExpressionSource src, TypeIdentifier type) {
      super(src);
      this.type = type;
   }

   @Override
   protected boolean matchesImpl(FlowValue node, ExpressionContext ctx) {
      Type cstType = this.getConstantType(node.getInsn());
      return cstType != null && this.type.matches(ctx.pool, cstType);
   }

   private Type getConstantType(AbstractInsnNode insn) {
      if (insn instanceof LdcInsnNode) {
         Object cst = ((LdcInsnNode)insn).cst;
         return cst instanceof Type ? (Type)cst : null;
      } else if (insn.getOpcode() != 178) {
         return null;
      } else {
         FieldInsnNode get = (FieldInsnNode)insn;
         if (get.name.equals("TYPE") && get.desc.equals(Type.getDescriptor(Class.class))) {
            String var3 = get.owner;
            switch (var3) {
               case "java/lang/Boolean":
                  return Type.BOOLEAN_TYPE;
               case "java/lang/Character":
                  return Type.CHAR_TYPE;
               case "java/lang/Byte":
                  return Type.BYTE_TYPE;
               case "java/lang/Short":
                  return Type.SHORT_TYPE;
               case "java/lang/Integer":
                  return Type.INT_TYPE;
               case "java/lang/Float":
                  return Type.FLOAT_TYPE;
               case "java/lang/Long":
                  return Type.LONG_TYPE;
               case "java/lang/Double":
                  return Type.DOUBLE_TYPE;
               default:
                  return null;
            }
         } else {
            return null;
         }
      }
   }
}
