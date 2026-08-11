package io.github.legacymoddingmc.unimixins.mixinextras;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
public class MixinExtrasCore implements IFMLLoadingPlugin {
   public static final Logger LOGGER = LogManager.getLogger("unimixins-mixinextras");

   public MixinExtrasCore() {
      LOGGER.info("Instantiating " + this.getClass().getSimpleName());
      MixinExtrasBootstrap.init();
   }

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
   }

   public String getAccessTransformerClass() {
      return null;
   }
}
