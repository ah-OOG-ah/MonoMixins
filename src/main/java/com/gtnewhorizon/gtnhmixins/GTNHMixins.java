package com.gtnewhorizon.gtnhmixins;

import com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore;
import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "gtnhmixins", version = "2.2.0", name = "GTNHMixins", acceptableRemoteVersions = "*")
@net.minecraftforge.fml.common.Mod(modid = "gtnhmixins", version = "2.2.0", name = "GTNHMixins", acceptableRemoteVersions = "*")
public class GTNHMixins {
   public static final String NAME = "GTNHMixins";
   public static final String MODID = "gtnhmixins";
   public static final String VERSION = "2.2.0";
   public static final Logger LOGGER = LogManager.getLogger("GTNHMixins");

   public static void log(String message) {
      if (GTNHMixinsCore.isObf()) {
         LOGGER.debug(message);
      } else {
         LOGGER.info(message);
      }
   }

   public static void log(String message, Object... params) {
      if (GTNHMixinsCore.isObf()) {
         LOGGER.debug(message, params);
      } else {
         LOGGER.info(message, params);
      }
   }
}
