package io.github.legacymoddingmc.unimixins.mixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.mixin.repackage.common.sanitycheck.SanityCheckHelper;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class MixinCore implements IFMLLoadingPlugin {
   public static final Logger LOGGER = LogManager.getLogger("unimixins");

   public String[] getASMTransformerClass() {
      return new String[0];
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
      MixinModidDecorator.apply();
   }

   public String getAccessTransformerClass() {
      return null;
   }

   static {
      if (SanityCheckHelper.isEnabled()) {
         SanityCheckHelper.showBigWarning(
            MixinSanityCheck.checkMixinHasInitialized(),
            MixinSanityCheck.checkMixinContainer(),
            SanityCheckHelper.checkIfJarPrefixesExist(Arrays.asList("gasstation-", "mixinbooterlegacy-", "spongemixins-"))
         );
      }
   }
}
