package org.spongepowered.asm.util;

import java.lang.reflect.Field;
import java.util.List;
import org.spongepowered.asm.lib.ConstantDynamic;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.LdcInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.asm.ASM;
import org.spongepowered.asm.util.asm.ClassNodeAdapter;

public final class LanguageFeatures {
   public static final int METHODS_IN_INTERFACES = 1;
   public static final int PRIVATE_SYNTHETIC_METHODS_IN_INTERFACES = 2;
   public static final int PRIVATE_METHODS_IN_INTERFACES = 4;
   public static final int NESTING = 8;
   public static final int DYNAMIC_CONSTANTS = 16;
   public static final int RECORDS = 32;
   public static final int SEALED_CLASSES = 64;

   private LanguageFeatures() {
   }

   public static int scan(ClassNode classNode) {
      int features = scanClassFeatures(classNode);
      boolean isInterface = Bytecode.hasFlag(classNode, 512);

      for (MethodNode methodNode : classNode.methods) {
         if (isInterface) {
            features |= scanInterfaceFeatures(methodNode);
         } else {
            features |= scanMethodFeatures(methodNode);
         }
      }

      return features;
   }

   private static int scanClassFeatures(ClassNode classNode) {
      int features = 0;
      String nestHostClass = ClassNodeAdapter.getNestHostClass(classNode);
      List<String> nestMembers = ClassNodeAdapter.getNestMembers(classNode);
      if (nestHostClass != null || nestMembers != null && nestMembers.size() > 0) {
         features |= 8;
      }

      return features;
   }

   private static int scanInterfaceFeatures(MethodNode methodNode) {
      int features = 0;
      if (!Bytecode.hasFlag(methodNode, 1024)) {
         features |= 1;
      }

      if (Bytecode.getVisibility(methodNode).isLessThan(Bytecode.Visibility.PUBLIC)) {
         features |= Bytecode.hasFlag(methodNode, 4096) ? 2 : 4;
      }

      return features;
   }

   private static int scanMethodFeatures(MethodNode methodNode) {
      if (ASM.isAtLeastVersion(6)) {
         for (AbstractInsnNode insn : methodNode.instructions) {
            if (insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst instanceof ConstantDynamic) {
               return 16;
            }
         }
      }

      return 0;
   }

   public static final String format(int features) {
      StringBuilder sb = new StringBuilder("[");

      try {
         int count = 0;

         for (Field field : LanguageFeatures.class.getDeclaredFields()) {
            if ((features & field.getInt(null)) != 0) {
               if (count++ > 0) {
                  sb.append(',');
               }

               sb.append(field.getName());
            }
         }
      } catch (ReflectiveOperationException var7) {
         sb.append("ERROR");
      }

      return sb.append(']').toString();
   }
}
