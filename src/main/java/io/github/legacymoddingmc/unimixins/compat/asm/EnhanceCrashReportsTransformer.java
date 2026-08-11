package io.github.legacymoddingmc.unimixins.compat.asm;

import io.github.legacymoddingmc.unimixins.compat.CompatCore;
import io.github.legacymoddingmc.unimixins.compat.CrashReportEnhancer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class EnhanceCrashReportsTransformer implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      if (basicClass == null) {
         return null;
      } else {
         return name.equals("cpw.mods.fml.common.FMLCommonHandler") ? transformFMLCommonHandler(basicClass) : basicClass;
      }
   }

   private static byte[] transformFMLCommonHandler(byte[] bytes) {
      CompatCore.LOGGER.info("Transforming FMLCommonHandler to add hook for enhancing crash reports");
      ClassNode classNode = new ClassNode();
      ClassReader classReader = new ClassReader(bytes);
      classReader.accept(classNode, 0);

      for (MethodNode m : classNode.methods) {
         if (m.name.equals("enhanceCrashReport")) {
            AbstractInsnNode injectionTarget = null;

            for (int i = 0; i < m.instructions.size(); i++) {
               AbstractInsnNode ain = m.instructions.get(i);
               if (ain instanceof InsnNode) {
                  InsnNode in = (InsnNode)ain;
                  if (in.getOpcode() == 177) {
                     injectionTarget = in;
                     break;
                  }
               }
            }

            if (injectionTarget != null) {
               InsnList inject = new InsnList();
               inject.add(new VarInsnNode(25, 1));
               inject.add(new VarInsnNode(25, 2));
               inject.add(
                  new MethodInsnNode(
                     184,
                     "io/github/legacymoddingmc/unimixins/compat/asm/EnhanceCrashReportsTransformer$Hooks",
                     "postEnhanceCrashReport",
                     "(Lnet/minecraft/crash/CrashReport;Lnet/minecraft/crash/CrashReportCategory;)V",
                     false
                  )
               );
               m.instructions.insertBefore(injectionTarget, inject);
            }
         }
      }

      ClassWriter writer = new ClassWriter(3);
      classNode.accept(writer);
      return writer.toByteArray();
   }

   public static class Hooks {
      public static void postEnhanceCrashReport(CrashReport report, CrashReportCategory category) {
         CrashReportEnhancer.addMixinsToCrashReport(report, category);
      }
   }
}
