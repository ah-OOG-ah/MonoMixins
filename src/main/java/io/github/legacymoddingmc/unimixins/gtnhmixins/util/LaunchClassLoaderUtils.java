package io.github.legacymoddingmc.unimixins.gtnhmixins.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class LaunchClassLoaderUtils {
   private static Map<String, byte[]> resourceCache;
   private static Set<String> negativeResourceCache;

   public static void putInResourceCache(String key, byte[] value) {
      resourceCache.put(key, value);
      negativeResourceCache.remove(key);
   }

   static {
      try {
         Field resourceCacheField = LaunchClassLoader.class.getDeclaredField("resourceCache");
         resourceCacheField.setAccessible(true);
         resourceCache = (Map<String, byte[]>)resourceCacheField.get(Launch.classLoader);
         Field negativeResourceCacheField = LaunchClassLoader.class.getDeclaredField("negativeResourceCache");
         negativeResourceCacheField.setAccessible(true);
         negativeResourceCache = (Set<String>)negativeResourceCacheField.get(Launch.classLoader);
      } catch (Exception var2) {
         var2.printStackTrace();
         throw new RuntimeException(var2);
      }
   }
}
