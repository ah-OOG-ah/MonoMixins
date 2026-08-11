package io.github.legacymoddingmc.unimixins.compat.asm;

import io.github.legacymoddingmc.unimixins.compat.CompatCore;
import java.util.Arrays;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class HackClasspathModDiscoveryTransformer implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      if (basicClass == null) {
         return null;
      } else {
         if (transformedName.equals("cpw.mods.fml.common.discovery.ModDiscoverer")) {
            basicClass = this.doTransformModDiscoverer(basicClass);
         }

         return basicClass;
      }
   }

   private byte[] doTransformModDiscoverer(byte[] bytes) {
      CompatCore.LOGGER.info("HackClasspathModDiscoveryTransformer: Transforming ModDiscoverer#findClasspathMods to ignore reparseable coremods.");

      try {
         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 0);

         label38:
         for (MethodNode m : classNode.methods) {
            if (m.name.equals("findClasspathMods")) {
               for (AbstractInsnNode i = m.instructions.getFirst(); i != null; i = i.getNext()) {
                  if (i.getOpcode() == 184) {
                     MethodInsnNode mi = (MethodInsnNode)i;
                     if (mi.owner.equals("cpw/mods/fml/relauncher/CoreModManager")
                        && mi.name.equals("getReparseableCoremods")
                        && mi.desc.equals("()Ljava/util/List;")) {
                        m.instructions
                           .insertBefore(
                              mi,
                              new MethodInsnNode(
                                 184,
                                 "io/github/legacymoddingmc/unimixins/compat/asm/HackClasspathModDiscoveryTransformer$Hooks",
                                 "redirectGetReparseableCoremods",
                                 mi.desc,
                                 false
                              )
                           );
                        m.instructions.remove(mi);
                        break label38;
                     }
                  }
               }
               break;
            }
         }

         ClassWriter writer = new ClassWriter(0);
         classNode.accept(writer);
         return writer.toByteArray();
      } catch (Exception var8) {
         var8.printStackTrace();
         return bytes;
      }
   }

   public static class Hooks {
      public static List<String> redirectGetReparseableCoremods() {
         return Arrays.asList();
      }
   }
}
