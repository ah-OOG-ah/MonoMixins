package io.github.legacymoddingmc.unimixins.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

public final class MixinModidDecorator {
   private static final Logger logger = LogManager.getLogger();
   public static final String KEY_MOD_ID = "fabric-modId";
   private static Map<String, String> jarNameToModid;

   private MixinModidDecorator() {
   }

   public static void apply() {
      if (jarNameToModid == null) {
         jarNameToModid = createJarNameToModidMap();
         Launch.blackboard.put("unimixins.mixinModidDecorator.refresh", MixinModidDecorator::apply);
      }

      for (Config config : Mixins.getConfigs()) {
         if (!config.getConfig().hasDecoration("fabric-modId")) {
            URL resource = Launch.classLoader.getResource(config.getName());
            if (resource != null) {
               String jarName = getJarNameFromResourceUrl(resource);
               if (jarName != null) {
                  String modid = jarNameToModid.get(jarName);
                  if (modid != null) {
                     config.getConfig().decorate("fabric-modId", modid);
                  }
               }
            }
         }
      }
   }

   private static Map<String, String> createJarNameToModidMap() {
      Map<String, String> map = new HashMap<>();

      try {
         for (URL url : Collections.list(Launch.classLoader.getResources("mcmod.info"))) {
            String fileName = getJarNameFromResourceUrl(url);
            if (fileName != null) {
               List<MixinModidDecorator.ModMetadata> metas = parseMcmodInfo(url);
               if (!metas.isEmpty()) {
                  String modid = metas.get(0).modid;
                  if (modid != null) {
                     map.put(fileName, createUniqueModid(modid, map.values()));
                  }
               }
            }
         }
      } catch (Exception var6) {
         logger.warn("Failed to construct jar name -> modid map, mod names will not be shown in errors.");
      }

      return map;
   }

   private static String createUniqueModid(String modid, Collection<String> registeredModids) {
      String sanitizedModid = "";

      for (int i = 0; i < modid.length(); i++) {
         char c = modid.charAt(i);
         boolean valid = i == 0 ? isValidLetter(c) : isValidLetter(c) || isValidDigit(c);
         sanitizedModid = sanitizedModid + (valid ? c : '_');
      }

      modid = sanitizedModid;
      int discriminator = 1;

      while (registeredModids.contains(modid)) {
         modid = sanitizedModid + "_" + ++discriminator;
      }

      return modid;
   }

   private static boolean isValidLetter(char c) {
      return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
   }

   private static boolean isValidDigit(char c) {
      return c >= '0' && c <= '9' || c == '_';
   }

   private static List<MixinModidDecorator.ModMetadata> parseMcmodInfo(URL url) {
      try {
         JsonElement root = (JsonElement)new Gson().fromJson(new InputStreamReader(url.openStream()), JsonElement.class);
         return root.isJsonArray()
            ? Arrays.asList((Object[])new Gson().fromJson(new InputStreamReader(url.openStream()), MixinModidDecorator.ModMetadata[].class))
            : ((MixinModidDecorator.ModMetadataCollection)new Gson()
                  .fromJson(new InputStreamReader(url.openStream()), MixinModidDecorator.ModMetadataCollection.class))
               .modList;
      } catch (Exception var2) {
         logger.warn("Failed to parse mcmod.info at " + url + ": " + var2);
         return Arrays.asList();
      }
   }

   private static String getJarNameFromResourceUrl(URL url) {
      if (url.getPath().contains("!/")) {
         String filePath = url.getPath().split("!/")[0];
         String[] parts = filePath.split("/");
         if (parts.length != 0) {
            return parts[parts.length - 1];
         }
      }

      return null;
   }

   private static class ModMetadata {
      String modid;
   }

   private static class ModMetadataCollection {
      List<MixinModidDecorator.ModMetadata> modList;
   }
}
