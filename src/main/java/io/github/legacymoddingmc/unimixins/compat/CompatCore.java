package io.github.legacymoddingmc.unimixins.compat;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer;
import io.github.legacymoddingmc.unimixins.compat.repackage.common.config.ConfigUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

@MCVersion("1.7.10")
public class CompatCore implements IFMLLoadingPlugin {
   public static final Logger LOGGER = LogManager.getLogger("unimixins");

   public CompatCore() {
      LOGGER.info("Instantiating CompatCore");
      ConfigUtil.load(CompatConfig.class);
      if (CompatConfig.enableRemapper) {
         Launch.classLoader.registerTransformer(relativeClassName("asm.ASMRemapperTransformer"));
      }
   }

   public String[] getASMTransformerClass() {
      List<String> classes = new ArrayList<>();
      if (CompatConfig.enhanceCrashReports) {
         classes.add("io.github.legacymoddingmc.unimixins.compat.asm.EnhanceCrashReportsTransformer");
      }

      if (IgnoreDuplicateJarsTransformer.wantsToRun()) {
         classes.add("io.github.legacymoddingmc.unimixins.compat.asm.IgnoreDuplicateJarsTransformer");
      }

      if (Boolean.parseBoolean(System.getProperty("unimixins.compat.hackClasspathModDiscovery", "false"))) {
         classes.add("io.github.legacymoddingmc.unimixins.compat.asm.HackClasspathModDiscoveryTransformer");
      }

      return classes.toArray(new String[0]);
   }

   private static String relativeClassName(String relName) {
      String name = CompatCore.class.getName();
      name = name.substring(0, name.lastIndexOf(46) + 1);
      return name + relName;
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
      if (CompatConfig.enhanceCrashReports) {
         Mixins.registerErrorHandlerClass("io.github.legacymoddingmc.unimixins.compat.MixinErrorHandler");
      }
   }

   public String getAccessTransformerClass() {
      return "io.github.legacymoddingmc.unimixins.compat.CompatCore$DummyTransformer";
   }

   public static class DummyTransformer implements IClassTransformer {
      public DummyTransformer() {
         if (CompatConfig.improveInitPhaseDetection) {
            setFmlLoggerToVerbose();
         }
      }

      private static void setFmlLoggerToVerbose() {
         Logger fmlLog;
         try {
            fmlLog = FMLRelaunchLog.log.getLogger();
            if (!(fmlLog instanceof org.apache.logging.log4j.core.Logger)) {
               return;
            }
         } catch (NoClassDefFoundError var2) {
            return;
         }

         org.apache.logging.log4j.core.Logger fmlCoreLog = (org.apache.logging.log4j.core.Logger)fmlLog;
         if (fmlCoreLog.getLevel() != Level.ALL) {
            CompatCore.LOGGER.info("Correcting FML log level from " + fmlCoreLog.getLevel() + " to ALL");
         } else {
            CompatCore.LOGGER.debug("FML log level was already ALL, doing nothing");
         }

         fmlCoreLog.setLevel(Level.ALL);
      }

      public byte[] transform(String name, String transformedName, byte[] basicClass) {
         return basicClass;
      }
   }
}
