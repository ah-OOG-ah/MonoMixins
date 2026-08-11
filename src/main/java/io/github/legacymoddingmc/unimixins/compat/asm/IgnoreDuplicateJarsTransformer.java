package io.github.legacymoddingmc.unimixins.compat.asm;

import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;
import io.github.legacymoddingmc.unimixins.compat.CompatCore;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class IgnoreDuplicateJarsTransformer implements IClassTransformer {
   private static Set<File> uniMixinsJavaAgentJars;

   public static boolean wantsToRun() {
      return !getUniMixinsJavaAgentJars().isEmpty();
   }

   private static Set<File> getUniMixinsJavaAgentJars() {
      if (uniMixinsJavaAgentJars == null) {
         uniMixinsJavaAgentJars = new HashSet<>();

         try {
            for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
               if (arg.startsWith("-javaagent:")) {
                  String agent = arg.substring("-javaagent:".length());
                  File agentFile = new File(agent);
                  String name = agentFile.getName();
                  if (name.toLowerCase().contains("unimixins")) {
                     uniMixinsJavaAgentJars.add(agentFile);
                  }
               }
            }
         } catch (Exception var5) {
            CompatCore.LOGGER.error("Failed to enumerate command line java agents");
            var5.printStackTrace();
         }
      }

      return uniMixinsJavaAgentJars;
   }

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
      CompatCore.LOGGER
         .info("IgnoreDuplicateJarsTransformer: Transforming ModDiscoverer#identifyMods to ignore duplicate UniMixins jars coming from Java agents.");

      try {
         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 0);

         for (MethodNode m : classNode.methods) {
            if (m.name.equals("identifyMods")) {
               InsnList patch = new InsnList();
               patch.add(new VarInsnNode(25, 0));
               patch.add(
                  new MethodInsnNode(
                     184,
                     "io/github/legacymoddingmc/unimixins/compat/asm/IgnoreDuplicateJarsTransformer$Hooks",
                     "preIdentifyMods",
                     "(Lcpw/mods/fml/common/discovery/ModDiscoverer;)V",
                     false
                  )
               );
               m.instructions.insert(patch);
               break;
            }
         }

         ClassWriter writer = new ClassWriter(1);
         classNode.accept(writer);
         return writer.toByteArray();
      } catch (Exception var7) {
         var7.printStackTrace();
         return bytes;
      }
   }

   public static class Hooks {
      public static void preIdentifyMods(ModDiscoverer dis) {
         try {
            List<ModCandidate> candidates = getModCandidates(dis);
            Map<File, List<ModCandidate>> modCandidatesByFile = new HashMap<>();

            for (ModCandidate mc : candidates) {
               modCandidatesByFile.computeIfAbsent(getModContainer(mc), x -> new ArrayList<>()).add(mc);
            }

            for (Entry<File, List<ModCandidate>> e : modCandidatesByFile.entrySet()) {
               File file = e.getKey();
               List<ModCandidate> mcs = e.getValue();
               if (IgnoreDuplicateJarsTransformer.getUniMixinsJavaAgentJars().contains(file)) {
                  Iterator<ModCandidate> it = mcs.iterator();

                  while (mcs.size() > 1 && it.hasNext()) {
                     ModCandidate mc = it.next();
                     if (mc.isClasspath()) {
                        CompatCore.LOGGER.info("Removing duplicate Java agent mod candidate: " + file.getName());
                        candidates.remove(mc);
                        mcs.remove(mc);
                     }
                  }
               }
            }
         } catch (Exception var9) {
            CompatCore.LOGGER
               .error(
                  "Failed to remove duplicate mod candidates, the Mixin Java agent will cause a duplicate mod error. Add the UniMix jar as the java agent instead."
               );
            var9.printStackTrace();
         }
      }

      private static List<ModCandidate> getModCandidates(ModDiscoverer dis) throws Exception {
         Field f = dis.getClass().getDeclaredField("candidates");
         f.setAccessible(true);
         return (List<ModCandidate>)f.get(dis);
      }

      private static File getModContainer(ModCandidate mc) throws Exception {
         Field f = ModCandidate.class.getDeclaredField("modContainer");
         f.setAccessible(true);
         return (File)f.get(mc);
      }
   }
}
