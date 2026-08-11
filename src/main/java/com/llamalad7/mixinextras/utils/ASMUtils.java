package com.llamalad7.mixinextras.utils;

import com.llamalad7.mixinextras.lib.apache.commons.StringUtils;
import com.llamalad7.mixinextras.service.MixinExtrasService;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.util.Bytecode;

public class ASMUtils {
   public static final Type OBJECT_TYPE = Type.getType(Object.class);
   public static final Handle LMF_HANDLE = new Handle(
      6,
      "java/lang/invoke/LambdaMetafactory",
      "metafactory",
      Bytecode.generateDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class),
      false
   );

   public static String annotationToString(AnnotationNode annotation) {
      StringBuilder builder = new StringBuilder("@").append(typeToString(Type.getType(annotation.desc)));
      List<Object> values = annotation.values;
      if (values != null && !values.isEmpty()) {
         builder.append('(');

         for (int i = 0; i < values.size(); i += 2) {
            if (i != 0) {
               builder.append(", ");
            }

            String name = (String)values.get(i);
            Object value = values.get(i + 1);
            builder.append(name).append(" = ").append(valueToString(value));
         }

         builder.append(')');
         return builder.toString();
      } else {
         return builder.toString();
      }
   }

   public static String typeToString(Type type) {
      String name = type.getClassName();
      return name.substring(name.lastIndexOf(46) + 1).replace('$', '.');
   }

   private static String valueToString(Object value) {
      if (value instanceof String) {
         return '"' + value.toString() + '"';
      } else if (value instanceof Type) {
         Type type = (Type)value;
         return typeToString(type) + ".class";
      } else if (value instanceof String[]) {
         String[] enumInfo = (String[])value;
         return typeToString(Type.getType(enumInfo[0])) + '.' + enumInfo[1];
      } else if (value instanceof AnnotationNode) {
         return annotationToString((AnnotationNode)value);
      } else if (value instanceof List) {
         List<?> list = (List<?>)value;
         return list.size() == 1 ? valueToString(list.get(0)) : '{' + list.stream().map(ASMUtils::valueToString).collect(Collectors.joining(", ")) + '}';
      } else {
         return value.toString();
      }
   }

   public static boolean isPrimitive(Type type) {
      return type.getDescriptor().length() == 1;
   }

   public static MethodInsnNode getInvokeInstruction(ClassNode owner, MethodNode method) {
      boolean isInterface = (owner.access & 512) != 0;
      int opcode = (method.access & 8) != 0 ? 184 : ((method.access & 2) != 0 ? 183 : (isInterface ? 185 : 182));
      return new MethodInsnNode(opcode, owner.name, method.name, method.desc, isInterface);
   }

   public static int getDummyOpcodeForType(Type type) {
      switch (type.getSort()) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            return 3;
         case 6:
            return 11;
         case 7:
            return 9;
         case 8:
            return 14;
         case 9:
         case 10:
            return 1;
         default:
            throw new UnsupportedOperationException();
      }
   }

   public static MethodInsnNode findInitNodeFor(Target target, TypeInsnNode newNode) {
      int start = target.indexOf(newNode);
      int depth = 0;

      for (AbstractInsnNode insn : target.insns) {
         if (insn instanceof TypeInsnNode && insn.getOpcode() == 187) {
            TypeInsnNode typeNode = (TypeInsnNode)insn;
            if (typeNode.desc.equals(newNode.desc)) {
               depth++;
            }
         } else if (insn instanceof MethodInsnNode && insn.getOpcode() == 183) {
            MethodInsnNode methodNode = (MethodInsnNode)insn;
            if ("<init>".equals(methodNode.name) && methodNode.owner.equals(newNode.desc)) {
               if (--depth == 0) {
                  return methodNode;
               }
            }
         }
      }

      return null;
   }

   public static void ifElse(InsnList insns, int jumpToSecond, Runnable first, Runnable second) {
      ifElse(insns, label -> insns.add(new JumpInsnNode(jumpToSecond, label)), first, second);
   }

   public static void ifElse(InsnList insns, Consumer<LabelNode> addJump, Runnable first, Runnable second) {
      LabelNode secondLabel = new LabelNode();
      LabelNode end = new LabelNode();
      addJump.accept(secondLabel);
      first.run();
      insns.add(new JumpInsnNode(167, end));
      insns.add(secondLabel);
      second.run();
      insns.add(end);
   }

   public static AnnotationNode getRepeatedMEAnnotation(MethodNode method, Class<? extends Annotation> single) {
      Class<? extends Annotation> container = single.getAnnotation(Repeatable.class).value();
      AnnotationNode repeated = getInvisibleMEAnnotation(method, container);
      if (repeated != null) {
         return repeated;
      } else {
         AnnotationNode individual = getInvisibleMEAnnotation(method, single);
         if (individual == null) {
            return null;
         } else {
            AnnotationNode result = new AnnotationNode(Type.getDescriptor(container));
            result.visit("value", individual);
            return result;
         }
      }
   }

   public static AnnotationNode getInvisibleMEAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
      return getMEAnnotation(method.invisibleAnnotations, Type.getInternalName(annotation));
   }

   private static AnnotationNode getMEAnnotation(List<AnnotationNode> annotations, String internalAnnotationName) {
      String annotationName = "." + StringUtils.substringAfterLast(internalAnnotationName, "/");
      if (annotations == null) {
         return null;
      } else {
         for (AnnotationNode annotation : annotations) {
            String binaryName = Type.getType(annotation.desc).getClassName();
            if (MixinExtrasService.getInstance().isClassOwned(binaryName) && binaryName.endsWith(annotationName)) {
               return annotation;
            }
         }

         return null;
      }
   }

   public static Type getConstantType(AbstractInsnNode insn) {
      return insn instanceof TypeInsnNode ? null : Bytecode.getConstantType(insn);
   }
}
