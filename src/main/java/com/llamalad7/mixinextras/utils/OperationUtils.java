package com.llamalad7.mixinextras.utils;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperationRuntime;
import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.IntInsnNode;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.asm.ASM;

public class OperationUtils {
   public static void makeOperation(
      Type[] argTypes,
      Type returnType,
      InsnList insns,
      boolean virtual,
      Type[] trailingParams,
      ClassNode classNode,
      Type operationType,
      String name,
      OperationUtils.OperationContents contents
   ) {
      Type[] descriptorArgs = trailingParams;
      if (virtual) {
         descriptorArgs = ArrayUtils.add(trailingParams, 0, Type.getObjectType(classNode.name));
      }

      insns.add(
         new InvokeDynamicInsnNode(
            "call",
            Type.getMethodDescriptor(operationType, descriptorArgs),
            ASMUtils.LMF_HANDLE,
            Type.getMethodType(Type.getType(Object.class), Type.getType(Object[].class)),
            generateSyntheticBridge(argTypes, returnType, virtual, trailingParams, name, classNode, contents),
            Type.getMethodType(
               ASMUtils.isPrimitive(returnType)
                  ? Type.getObjectType(returnType == Type.VOID_TYPE ? "java/lang/Void" : Bytecode.getBoxingType(returnType))
                  : returnType,
               Type.getType(Object[].class)
            )
         )
      );
   }

   private static Handle generateSyntheticBridge(
      final Type[] argTypes,
      final Type returnType,
      final boolean virtual,
      final Type[] boundParams,
      String name,
      ClassNode classNode,
      final OperationUtils.OperationContents contents
   ) {
      MethodNode method = new MethodNode(
         ASM.API_VERSION,
         4098 | (virtual ? 0 : 8),
         UniquenessHelper.getUniqueMethodName(classNode, "mixinextras$bridge$" + name),
         Bytecode.generateDescriptor(
            ASMUtils.isPrimitive(returnType)
               ? Type.getObjectType(returnType == Type.VOID_TYPE ? "java/lang/Void" : Bytecode.getBoxingType(returnType))
               : returnType,
            ArrayUtils.add(boundParams, Type.getType(Object[].class))
         ),
         null,
         null
      );
      method.instructions = new InsnList() {
         {
            int paramArrayIndex = Arrays.stream(boundParams).mapToInt(Type::getSize).sum() + (virtual ? 1 : 0);
            this.add(new VarInsnNode(25, paramArrayIndex));
            this.add(new IntInsnNode(16, argTypes.length));
            this.add(new LdcInsnNode(Arrays.stream(argTypes).map(Type::getClassName).collect(Collectors.joining(", ", "[", "]"))));
            this.add(
               new MethodInsnNode(
                  184,
                  Type.getInternalName(WrapOperationRuntime.class),
                  "checkArgumentCount",
                  Bytecode.generateDescriptor(void.class, Object[].class, int.class, String.class),
                  false
               )
            );
            if (virtual) {
               this.add(new VarInsnNode(25, 0));
            }

            Consumer<InsnList> loadArgs = insns -> {
               insns.add(new VarInsnNode(25, paramArrayIndex));

               for (int i = 0; i < argTypes.length; i++) {
                  Type argType = argTypes[i];
                  insns.add(new InsnNode(89));
                  insns.add(new IntInsnNode(16, i));
                  insns.add(new InsnNode(50));
                  if (ASMUtils.isPrimitive(argType)) {
                     insns.add(new TypeInsnNode(192, Bytecode.getBoxingType(argType)));
                     insns.add(
                        new MethodInsnNode(182, Bytecode.getBoxingType(argType), Bytecode.getUnboxingMethod(argType), Type.getMethodDescriptor(argType), false)
                     );
                  } else {
                     insns.add(new TypeInsnNode(192, argType.getInternalName()));
                  }

                  if (argType.getSize() == 2) {
                     insns.add(new InsnNode(93));
                     insns.add(new InsnNode(88));
                  } else {
                     insns.add(new InsnNode(95));
                  }
               }

               insns.add(new InsnNode(87));
               int boundParamIndex = virtual ? 1 : 0;

               for (Type boundParamType : boundParams) {
                  insns.add(new VarInsnNode(boundParamType.getOpcode(21), boundParamIndex));
                  boundParamIndex += boundParamType.getSize();
               }
            };
            this.add(contents.generate(paramArrayIndex, loadArgs));
            if (returnType == Type.VOID_TYPE) {
               this.add(new InsnNode(1));
               this.add(new TypeInsnNode(192, "java/lang/Void"));
            } else if (ASMUtils.isPrimitive(returnType)) {
               this.add(
                  new MethodInsnNode(
                     184,
                     Bytecode.getBoxingType(returnType),
                     "valueOf",
                     Bytecode.generateDescriptor(Type.getObjectType(Bytecode.getBoxingType(returnType)), returnType),
                     false
                  )
               );
            }

            this.add(new InsnNode(176));
         }
      };
      classNode.methods.add(method);
      return new Handle(virtual ? 7 : 6, classNode.name, method.name, method.desc, (classNode.access & 512) != 0);
   }

   @FunctionalInterface
   public interface OperationContents {
      InsnList generate(int var1, Consumer<InsnList> var2);
   }
}
