package org.spongepowered.asm.mixin.transformer.ext.extensions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IDecompiler;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.transformers.MixinClassWriter;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.Files;
import org.spongepowered.asm.util.perf.Profiler;

public class ExtensionClassExporter implements IExtension {
   private static final ILogger logger = MixinService.getService().getLogger("mixin");
   private final File classExportDir = new File(Constants.DEBUG_OUTPUT_DIR, "class");
   private final IDecompiler decompiler;

   public ExtensionClassExporter(MixinEnvironment env) {
      this.decompiler = this.initDecompiler(env, new File(Constants.DEBUG_OUTPUT_DIR, "java"));

      try {
         Files.deleteRecursively(this.classExportDir);
      } catch (IOException var3) {
         logger.debug("Error cleaning class output directory: {}", var3.getMessage());
      }
   }

   public boolean isDecompilerActive() {
      return this.decompiler != null;
   }

   private IDecompiler initDecompiler(MixinEnvironment env, File outputPath) {
      if (!env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE)) {
         return null;
      } else {
         try {
            boolean as = env.getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE_THREADED);
            logger.info("Attempting to load Fernflower decompiler{}", as ? " (Threaded mode)" : "");
            String className = "org.spongepowered.asm.mixin.transformer.debug.RuntimeDecompiler" + (as ? "Async" : "");
            Class<? extends IDecompiler> clazz = (Class<? extends IDecompiler>)Class.forName(className);
            Constructor<? extends IDecompiler> ctor = clazz.getDeclaredConstructor(File.class);
            IDecompiler decompiler = ctor.newInstance(outputPath);
            logger.info(
               "Fernflower decompiler was successfully initialised from {}, exported classes will be decompiled{}",
               decompiler,
               as ? " in a separate thread" : ""
            );
            return decompiler;
         } catch (Throwable var8) {
            logger.info("Fernflower could not be loaded, exported classes will not be decompiled. {}: {}", var8.getClass().getSimpleName(), var8.getMessage());
            return null;
         }
      }
   }

   private String prepareFilter(String filter) {
      filter = "^\\Q" + filter.replace("**", "\u0081").replace("*", "\u0082").replace("?", "\u0083") + "\\E$";
      return filter.replace("\u0081", "\\E.*\\Q").replace("\u0082", "\\E[^\\.]+\\Q").replace("\u0083", "\\E.\\Q").replace("\\Q\\E", "");
   }

   private boolean applyFilter(String filter, String subject) {
      return Pattern.compile(this.prepareFilter(filter), 2).matcher(subject).matches();
   }

   @Override
   public boolean checkActive(MixinEnvironment environment) {
      return true;
   }

   @Override
   public void preApply(ITargetClassContext context) {
   }

   @Override
   public void postApply(ITargetClassContext context) {
   }

   @Override
   public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
      if (force || env.getOption(MixinEnvironment.Option.DEBUG_EXPORT)) {
         String filter = env.getOptionValue(MixinEnvironment.Option.DEBUG_EXPORT_FILTER);
         if (force || filter == null || this.applyFilter(filter, name)) {
            Profiler.Section exportTimer = Profiler.getProfiler("export").begin("debug.export");
            File outputFile = this.dumpClass(name.replace('.', '/'), classNode);
            if (this.decompiler != null) {
               this.decompiler.decompile(outputFile);
            }

            exportTimer.end();
         }
      }
   }

   public File dumpClass(String fileName, ClassNode classNode) {
      File outputFile = new File(this.classExportDir, fileName + ".class");
      outputFile.getParentFile().mkdirs();

      try {
         byte[] bytecode = getClassBytes(classNode, true);
         if (bytecode != null) {
            org.spongepowered.include.com.google.common.io.Files.write(bytecode, outputFile);
         }
      } catch (IOException var5) {
      }

      return outputFile;
   }

   private static byte[] getClassBytes(ClassNode classNode, boolean computeFrames) {
      byte[] bytes = null;

      try {
         MixinClassWriter cw = new MixinClassWriter(computeFrames ? 2 : 0);
         classNode.accept(cw);
         bytes = cw.toByteArray();
      } catch (NegativeArraySizeException var4) {
         if (computeFrames) {
            logger.warn("Exporting class {} with COMPUTE_FRAMES failed! Trying a raw export.", classNode.name);
            return getClassBytes(classNode, false);
         }

         var4.printStackTrace();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return bytes;
   }
}
