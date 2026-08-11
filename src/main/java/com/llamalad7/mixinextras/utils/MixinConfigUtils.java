package com.llamalad7.mixinextras.utils;

import com.llamalad7.mixinextras.config.MixinExtrasConfig;
import com.llamalad7.mixinextras.lib.gson.Strictness;
import com.llamalad7.mixinextras.lib.gson.stream.JsonReader;
import com.llamalad7.mixinextras.service.MixinExtrasVersion;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.service.MixinService;

public class MixinConfigUtils {
   private static final String KEY_TOP_LEVEL_MIN_VERSION = "minMixinExtrasVersion";
   private static final String KEY_SUBCONFIG = "mixinextras";
   private static final String KEY_MIN_VERSION = "minVersion";
   private static final Map<IMixinConfig, MixinExtrasConfig> CONFIG_CACHE = new WeakHashMap<>();

   public static void requireMinVersion(IMixinConfig config, MixinExtrasVersion desiredVersion, String featureName) {
      MixinExtrasVersion min = extraConfigFor(config).minVersion;
      if (min == null || min.getNumber() < desiredVersion.getNumber()) {
         throw new UnsupportedOperationException(
            String.format(
               "In order to use %s, Mixin Config '%s' needs to declare a reliance on MixinExtras >=%s! E.g. `\"%s\": {\"%s\": \"%s\"}`",
               featureName,
               config,
               desiredVersion,
               "mixinextras",
               "minVersion",
               MixinExtrasVersion.LATEST
            )
         );
      }
   }

   private static MixinExtrasConfig extraConfigFor(IMixinConfig config) {
      return CONFIG_CACHE.computeIfAbsent(config, k -> new MixinExtrasConfig(config, readMinString(config)));
   }

   private static String readMinString(IMixinConfig config) {
      return readConfig(config, reader -> {
         reader.beginObject();

         while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
               case "mixinextras":
                  reader.beginObject();

                  while (reader.hasNext()) {
                     String innerKey = reader.nextName();
                     if (innerKey.equals("minVersion")) {
                        return reader.nextString();
                     }

                     reader.skipValue();
                  }

                  reader.endObject();
                  break;
               case "minMixinExtrasVersion":
                  return reader.nextString();
               default:
                  reader.skipValue();
            }
         }

         return null;
      });
   }

   private static <T> T readConfig(IMixinConfig config, MixinConfigUtils.JsonProcessor<T> compute) {
      try {
         JsonReader reader = new JsonReader(new InputStreamReader(MixinService.getService().getResourceAsStream(config.getName())));

         Object var3;
         try {
            reader.setStrictness(Strictness.LENIENT);
            var3 = compute.process(reader);
         } catch (Throwable var6) {
            try {
               reader.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         reader.close();
         return (T)var3;
      } catch (Exception var7) {
         throw new RuntimeException("Failed to read mixin config " + config.getName(), var7);
      }
   }

   @FunctionalInterface
   private interface JsonProcessor<T> {
      T process(JsonReader var1) throws IOException;
   }
}
