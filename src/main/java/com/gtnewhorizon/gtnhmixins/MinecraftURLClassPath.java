package com.gtnewhorizon.gtnhmixins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class MinecraftURLClassPath {
   private static final Path MOD_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();

   public static File getJarInModPath(String jarname) {
      try {
         return Files.walk(MOD_DIRECTORY_PATH).filter(p -> {
            String filename = p.toString();
            String extension = com.google.common.io.Files.getFileExtension(filename);
            return com.google.common.io.Files.getNameWithoutExtension(filename).contains(jarname) && ("jar".equals(extension) || "litemod".equals(extension));
         }).map(Path::toFile).findFirst().orElse(null);
      } catch (IOException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static boolean findJarInClassPath(String jarname) {
      for (URL url : Launch.classLoader.getURLs()) {
         String filename = url.getFile();
         String extension = com.google.common.io.Files.getFileExtension(filename);
         if (com.google.common.io.Files.getNameWithoutExtension(filename).contains(jarname) && ("jar".equals(extension) || "litemod".equals(extension))) {
            return true;
         }
      }

      return false;
   }

   public static void addJar(File pathToJar) throws Exception {
      LaunchClassLoader loader = Launch.classLoader;
      loader.addURL(pathToJar.toURI().toURL());
      loader.getSources().remove(loader.getSources().size() - 1);
   }

   private MinecraftURLClassPath() {
   }
}
