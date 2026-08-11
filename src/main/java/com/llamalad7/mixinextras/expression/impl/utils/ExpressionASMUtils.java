package com.llamalad7.mixinextras.expression.impl.utils;

import com.llamalad7.mixinextras.expression.impl.ExpressionService;
import com.llamalad7.mixinextras.expression.impl.flow.Boxing;
import com.llamalad7.mixinextras.expression.impl.flow.FlowContext;
import com.llamalad7.mixinextras.lib.apache.commons.StringUtils;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.IntInsnNode;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MultiANewArrayInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.util.Bytecode;

public class ExpressionASMUtils {
   public static final Type OBJECT_TYPE = Type.getType(Object.class);
   public static final Type BOTTOM_TYPE = Type.getObjectType("null");
   public static final Type INTLIKE_TYPE = Type.getObjectType("int-like");
   public static final Handle LMF_HANDLE = new Handle(
      6,
      "java/lang/invoke/LambdaMetafactory",
      "metafactory",
      Bytecode.generateDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class),
      false
   );
   public static final Handle ALT_LMF_HANDLE = new Handle(
      6,
      "java/lang/invoke/LambdaMetafactory",
      "altMetafactory",
      Bytecode.generateDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class, Object[].class),
      false
   );

   public static Type getNewType(AbstractInsnNode insn) {
      switch (insn.getOpcode()) {
         case 1:
            return BOTTOM_TYPE;
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 16:
         case 17:
            return INTLIKE_TYPE;
         case 9:
         case 10:
            return Type.LONG_TYPE;
         case 11:
         case 12:
         case 13:
            return Type.FLOAT_TYPE;
         case 14:
         case 15:
            return Type.DOUBLE_TYPE;
         case 18:
            Object cst = ((LdcInsnNode)insn).cst;
            if (cst instanceof Integer) {
               return INTLIKE_TYPE;
            } else if (cst instanceof Float) {
               return Type.FLOAT_TYPE;
            } else if (cst instanceof Long) {
               return Type.LONG_TYPE;
            } else if (cst instanceof Double) {
               return Type.DOUBLE_TYPE;
            } else if (cst instanceof String) {
               return Type.getType(String.class);
            } else {
               if (cst instanceof Type) {
                  int sort = ((Type)cst).getSort();
                  if (sort == 10 || sort == 9) {
                     return Type.getType(Class.class);
                  }

                  if (sort == 11) {
                     return Type.getType(MethodType.class);
                  }
               }

               if (cst instanceof Handle) {
                  return Type.getType(MethodHandle.class);
               }

               throw new IllegalArgumentException("Illegal LDC constant " + cst);
            }
         case 178:
            return Type.getType(((FieldInsnNode)insn).desc);
         case 187:
            return Type.getObjectType(((TypeInsnNode)insn).desc);
         default:
            throw errorFor(insn);
      }
   }

   public static Type getUnaryType(AbstractInsnNode insn) {
      switch (insn.getOpcode()) {
         case 116:
         case 132:
         case 136:
         case 139:
         case 142:
         case 190:
            return Type.INT_TYPE;
         case 117:
         case 133:
         case 140:
         case 143:
            return Type.LONG_TYPE;
         case 118:
         case 134:
         case 137:
         case 144:
            return Type.FLOAT_TYPE;
         case 119:
         case 135:
         case 138:
         case 141:
            return Type.DOUBLE_TYPE;
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         case 177:
         case 178:
         case 181:
         case 182:
         case 183:
         case 184:
         case 185:
         case 186:
         case 187:
         case 196:
         case 197:
         default:
            throw errorFor(insn);
         case 145:
            return Type.BYTE_TYPE;
         case 146:
            return Type.CHAR_TYPE;
         case 147:
            return Type.SHORT_TYPE;
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 179:
         case 191:
         case 194:
         case 195:
         case 198:
         case 199:
            return Type.VOID_TYPE;
         case 180:
            return Type.getType(((FieldInsnNode)insn).desc);
         case 188:
            switch (((IntInsnNode)insn).operand) {
               case 4:
                  return Type.getType("[Z");
               case 5:
                  return Type.getType("[C");
               case 6:
                  return Type.getType("[F");
               case 7:
                  return Type.getType("[D");
               case 8:
                  return Type.getType("[B");
               case 9:
                  return Type.getType("[S");
               case 10:
                  return Type.getType("[I");
               case 11:
                  return Type.getType("[J");
               default:
                  throw new Error("Invalid array type " + ((IntInsnNode)insn).operand);
            }
         case 189: {
            String desc = ((TypeInsnNode)insn).desc;
            return Type.getType("[" + Type.getObjectType(desc));
         }
         case 192: {
            String desc = ((TypeInsnNode)insn).desc;
            return Type.getObjectType(desc);
         }
         case 193:
            return Type.BOOLEAN_TYPE;
      }
   }

   public static Type getBinaryType(AbstractInsnNode insn, Type left) {
      switch (insn.getOpcode()) {
         case 46:
         case 96:
         case 100:
         case 104:
         case 108:
         case 112:
         case 120:
         case 122:
         case 124:
         case 126:
         case 128:
         case 130:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
            return Type.INT_TYPE;
         case 47:
         case 97:
         case 101:
         case 105:
         case 109:
         case 113:
         case 121:
         case 123:
         case 125:
         case 127:
         case 129:
         case 131:
            return Type.LONG_TYPE;
         case 48:
         case 98:
         case 102:
         case 106:
         case 110:
         case 114:
            return Type.FLOAT_TYPE;
         case 49:
         case 99:
         case 103:
         case 107:
         case 111:
         case 115:
            return Type.DOUBLE_TYPE;
         case 50:
         case 51:
            return getInnerType(left);
         case 52:
            return Type.CHAR_TYPE;
         case 53:
            return Type.SHORT_TYPE;
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 116:
         case 117:
         case 118:
         case 119:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 177:
         case 178:
         case 179:
         case 180:
         default:
            throw errorFor(insn);
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 181:
            return Type.VOID_TYPE;
      }
   }

   public static Type getNaryType(AbstractInsnNode insn) {
      switch (insn.getOpcode()) {
         case 186:
            return Type.getReturnType(((InvokeDynamicInsnNode)insn).desc);
         case 197:
            return Type.getType(((MultiANewArrayInsnNode)insn).desc);
         default:
            return Type.getReturnType(((MethodInsnNode)insn).desc);
      }
   }

   private static Error errorFor(AbstractInsnNode insn) {
      return new AssertionError(String.format("Could not compute type of %s! Please inform LlamaLad7!", Bytecode.describeNode(insn)));
   }

   public static Type getCommonSupertype(FlowContext ctx, Type type1, Type type2) {
      if (type1.equals(type2) || type2.equals(BOTTOM_TYPE)) {
         return type1;
      } else if (type1.equals(BOTTOM_TYPE)) {
         return type2;
      } else {
         boolean isIntLike1 = isIntLike(type1);
         boolean isIntLike2 = isIntLike(type2);
         if (isIntLike1 && isIntLike2) {
            return INTLIKE_TYPE;
         } else if (!isIntLike1 && !isIntLike2) {
            if (type1.getSort() == 9 && type2.getSort() == 9) {
               int dim1 = type1.getDimensions();
               Type elem1 = type1.getElementType();
               int dim2 = type2.getDimensions();
               Type elem2 = type2.getElementType();
               if (dim1 != dim2) {
                  int shared;
                  Type smaller;
                  if (dim1 < dim2) {
                     smaller = elem1;
                     shared = dim1 - 1;
                  } else {
                     smaller = elem2;
                     shared = dim2 - 1;
                  }

                  if (smaller.getSort() == 10) {
                     shared++;
                  }

                  return arrayType(OBJECT_TYPE, shared);
               } else {
                  Type commonSupertype;
                  if (elem1.equals(elem2)) {
                     commonSupertype = elem1;
                  } else {
                     if (elem1.getSort() != 10 || elem2.getSort() != 10) {
                        return arrayType(OBJECT_TYPE, dim1 - 1);
                     }

                     commonSupertype = getCommonSupertype(ctx, elem1, elem2);
                  }

                  return arrayType(commonSupertype, dim1);
               }
            } else if ((type1.getSort() != 9 || type2.getSort() != 10) && (type2.getSort() != 9 || type1.getSort() != 10)) {
               return type1.getSort() != type2.getSort() ? BOTTOM_TYPE : ExpressionService.getInstance().getCommonSuperClass(ctx, type1, type2);
            } else {
               return OBJECT_TYPE;
            }
         } else {
            return BOTTOM_TYPE;
         }
      }
   }

   public static Type getCommonIntType(FlowContext ctx, Type type1, Type type2) {
      Type unboxed1 = Boxing.getUnboxedType(type1);
      Type unboxed2 = Boxing.getUnboxedType(type2);
      return getCommonSupertype(ctx, unboxed1 != null ? unboxed1 : type1, unboxed2 != null ? unboxed2 : type2);
   }

   public static boolean isIntLike(Type type) {
      switch (type.getSort()) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            return true;
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return false;
         case 10:
            return type.equals(INTLIKE_TYPE);
      }
   }

   private static Type arrayType(Type element, int dimensions) {
      return Type.getType(StringUtils.repeat('[', dimensions) + element.getDescriptor());
   }

   public static Type getInnerType(Type arrayType) {
      return arrayType.equals(BOTTOM_TYPE) ? BOTTOM_TYPE : Type.getType(arrayType.getDescriptor().substring(1));
   }

   public static Type getNewArrayType(IntInsnNode newArray) {
      switch (newArray.operand) {
         case 4:
            return Type.BOOLEAN_TYPE;
         case 5:
            return Type.CHAR_TYPE;
         case 6:
            return Type.FLOAT_TYPE;
         case 7:
            return Type.DOUBLE_TYPE;
         case 8:
            return Type.BYTE_TYPE;
         case 9:
            return Type.SHORT_TYPE;
         case 10:
            return Type.INT_TYPE;
         case 11:
            return Type.LONG_TYPE;
         default:
            return null;
      }
   }

   public static Object getConstant(AbstractInsnNode insn) {
      if (insn.getOpcode() == 188) {
         return null;
      } else {
         return insn instanceof TypeInsnNode ? null : Bytecode.getConstant(insn);
      }
   }

   public static AbstractInsnNode pushInt(int integer) {
      switch (integer) {
         case -1:
            return new InsnNode(2);
         case 0:
            return new InsnNode(3);
         case 1:
            return new InsnNode(4);
         case 2:
            return new InsnNode(5);
         case 3:
            return new InsnNode(6);
         case 4:
            return new InsnNode(7);
         case 5:
            return new InsnNode(8);
         default:
            if (-128 <= integer && integer <= 127) {
               return new IntInsnNode(16, integer);
            } else {
               return (AbstractInsnNode)(-32768 <= integer && integer <= 32767 ? new IntInsnNode(17, integer) : new LdcInsnNode(integer));
            }
      }
   }

   public static Type getCastType(AbstractInsnNode insn) {
      switch (insn.getOpcode()) {
         case 133:
         case 140:
         case 143:
            return Type.LONG_TYPE;
         case 134:
         case 137:
         case 144:
            return Type.FLOAT_TYPE;
         case 135:
         case 138:
         case 141:
            return Type.DOUBLE_TYPE;
         case 136:
         case 139:
         case 142:
            return Type.INT_TYPE;
         case 145:
            return Type.BYTE_TYPE;
         case 146:
            return Type.CHAR_TYPE;
         case 147:
            return Type.SHORT_TYPE;
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 177:
         case 178:
         case 179:
         case 180:
         case 181:
         case 182:
         case 183:
         case 184:
         case 185:
         case 186:
         case 187:
         case 188:
         case 189:
         case 190:
         case 191:
         default:
            return null;
         case 192:
            return Type.getObjectType(((TypeInsnNode)insn).desc);
      }
   }
}
