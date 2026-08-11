package makamys.mixingasm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigHelper {
   private final String MODID;
   private final Logger LOGGER;

   public ConfigHelper(String modid) {
      this.MODID = modid;
      this.LOGGER = LogManager.getLogger(this.MODID);
   }

   public Path getDefaultConfigFilePath(Path relPath) throws IOException {
      String resourceRelPath = Paths.get("assets/" + this.MODID + "/default_config/").resolve(relPath).toString().replace('\\', '/');
      URL resourceURL = (new Object() {}).getClass().getEnclosingClass().getClassLoader().getResource(resourceRelPath);
      String var4 = resourceURL.getProtocol();
      switch (var4) {
         case "jar":
            String urlString = resourceURL.getPath();
            int lastExclamation = urlString.lastIndexOf(33);
            String newURLString = urlString.substring(0, lastExclamation);
            return FileSystems.newFileSystem(new File(URI.create(newURLString)).toPath(), null).getPath(resourceRelPath);
         case "file":
            return new File(URI.create(resourceURL.toString())).toPath();
         default:
            return null;
      }
   }

   private void copyDefaultConfigFile(Path src, Path dest) throws IOException {
      Files.createDirectories(this.getParentSafe(dest));
      this.LOGGER.debug("Copying " + src + " -> " + dest);
      Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
   }

   public boolean createDefaultConfigFileIfMissing(File configFile, boolean overwrite) {
      Path configFolderPath = Paths.get(new File(Launch.minecraftHome, "config").getPath());
      Path configFilePath = Paths.get(configFile.getPath());
      Path relPath = configFolderPath.relativize(configFilePath);
      if (!configFilePath.startsWith(configFolderPath)) {
         this.LOGGER.debug("Invalid argument for creating default config file: " + relPath.toString() + " (file is not in the config directory)");
         return false;
      } else {
         try {
            Path defaultConfigPath = this.getDefaultConfigFilePath(relPath);
            if (Files.isRegularFile(defaultConfigPath)) {
               if (!configFile.exists() || overwrite) {
                  this.copyDefaultConfigFile(defaultConfigPath, configFile.toPath());
               }
            } else if (Files.isDirectory(defaultConfigPath)) {
               Files.createDirectories(Paths.get(configFile.getPath()));

               for (Object po : Files.walk(defaultConfigPath).toArray()) {
                  Path destPath = configFile.toPath().resolve(defaultConfigPath.toAbsolutePath().relativize(((Path)po).toAbsolutePath()).toString());
                  if (Files.isRegularFile((Path)po) && (!Files.exists(destPath) || overwrite)) {
                     this.copyDefaultConfigFile((Path)po, destPath);
                  }
               }
            }

            return true;
         } catch (IOException var12) {
            this.LOGGER.error("Failed to create default config file for " + relPath.toString() + ": " + var12.getMessage());
            return false;
         }
      }
   }

   public Path getParentSafe(Path p) {
      return p != null && p.getParent() != null ? p.getParent() : Paths.get("");
   }
}
