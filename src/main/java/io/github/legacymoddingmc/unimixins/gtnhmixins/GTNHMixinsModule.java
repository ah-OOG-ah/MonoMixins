package io.github.legacymoddingmc.unimixins.gtnhmixins;

import io.github.legacymoddingmc.unimixins.gtnhmixins.repackage.common.sanitycheck.SanityCheckHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GTNHMixinsModule {
   public static final Logger LOGGER = LogManager.getLogger("unimixins-gtnhmixins");

   public static void init() {
      if (SanityCheckHelper.isEnabled()) {
         SanityCheckHelper.warnIfJarPrefixesExist(Arrays.asList("gasstation-", "mixinbooterlegacy-", "spongemixins-"));
         checkComponentIntegrity();
      }
   }

   private static void checkComponentIntegrity() {
      List<String> missingComponents = new ArrayList<>();
      if (!classExists("org.spongepowered.asm.launch.MixinBootstrap")) {
         missingComponents.add("Mixin");
      }

      if (!classExists("ru.timeconqueror.spongemixins.SpongeMixins")) {
         missingComponents.add("SpongeMixins");
      }

      if (!missingComponents.isEmpty()) {
         LOGGER.error("The following missing components were detected: {}", new Object[]{missingComponents});
         LOGGER.error("Please obtain mods which provide them.");
         throw new RuntimeException("Missing components detected");
      }
   }

   public static boolean classExists(String string) {
      return GTNHMixinsModule.class.getResource("/" + string.replaceAll("\\.", "/") + ".class") != null;
   }
}
