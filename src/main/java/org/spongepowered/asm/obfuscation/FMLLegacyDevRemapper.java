package org.spongepowered.asm.obfuscation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.include.com.google.common.base.Charsets;
import org.spongepowered.include.com.google.common.base.Strings;
import org.spongepowered.include.com.google.common.io.Files;
import org.spongepowered.include.com.google.common.io.LineProcessor;

public class FMLLegacyDevRemapper implements IRemapper {
   private static final Logger logger = LogManager.getLogger("mixin");
   private static final Map<String, Map<String, String>> srgs = new HashMap<>();
   private final Map<String, String> mappings;
   private final Map<String, String> descCache = new HashMap<>();

   public FMLLegacyDevRemapper(MixinEnvironment env) {
      String resource = getResource(env);
      this.mappings = loadSrgs(resource);
      logger.info("Initialised Legacy FML Dev Remapper");
   }

   @Override
   public String mapMethodName(String owner, String name, String desc) {
      return this.mappings.getOrDefault(name, name);
   }

   @Override
   public String mapFieldName(String owner, String name, String desc) {
      return this.mappings.getOrDefault(name, name);
   }

   @Override
   public String map(String typeName) {
      return typeName;
   }

   @Override
   public String unmap(String typeName) {
      return typeName;
   }

   @Override
   public String mapDesc(String desc) {
      String remapped = this.descCache.get(desc);
      if (remapped == null) {
         remapped = desc;

         for (Entry<String, String> entry : this.mappings.entrySet()) {
            remapped = remapped.replace(entry.getKey(), entry.getValue());
         }

         this.descCache.put(desc, remapped);
      }

      return remapped;
   }

   @Override
   public String unmapDesc(String desc) {
      throw new UnsupportedOperationException();
   }

   private static Map<String, String> loadSrgs(String fileName) {
      if (srgs.containsKey(fileName)) {
         return srgs.get(fileName);
      } else {
         final Map<String, String> map = new HashMap<>();
         srgs.put(fileName, map);
         File file = new File(fileName);
         if (!file.isFile()) {
            return map;
         } else {
            try {
               Files.readLines(
                  file,
                  Charsets.UTF_8,
                  new LineProcessor<Object>() {
                     @Override
                     public Object getResult() {
                        return null;
                     }

                     @Override
                     public boolean processLine(String line) throws IOException {
                        if (!Strings.isNullOrEmpty(line) && !line.startsWith("#")) {
                           int fromPos = 0;
                           int toPos = 0;
                           if ((toPos = line.startsWith("MD: ") ? 2 : (line.startsWith("FD: ") ? 1 : 0)) > 0) {
                              String[] entries = line.substring(4).split(" ", 4);
                              map.put(
                                 entries[fromPos].substring(entries[fromPos].lastIndexOf(47) + 1), entries[toPos].substring(entries[toPos].lastIndexOf(47) + 1)
                              );
                           }

                           return true;
                        } else {
                           return true;
                        }
                     }
                  }
               );
            } catch (IOException var4) {
               logger.warn("Could not read input SRG file: {}", new Object[]{fileName});
               logger.catching(var4);
            }

            return map;
         }
      }
   }

   private static String getResource(MixinEnvironment env) {
      String resource = env.getOptionValue(MixinEnvironment.Option.REFMAP_REMAP_RESOURCE);
      return Strings.isNullOrEmpty(resource) ? System.getProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp") : resource;
   }
}
