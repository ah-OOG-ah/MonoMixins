package ru.timeconqueror.spongemixins.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
@SortingIndex(-2147483643)
@Name("SpongeMixin Core Plugin")
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex(-2147483643)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name("SpongeMixin Core Plugin")
public class SpongeMixinsCore implements IFMLLoadingPlugin {
   public static final String PLUGIN_NAME = "SpongeMixin Core Plugin";
   public static final Logger LOGGER = LogManager.getLogger("SpongeMixin Core Plugin");

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

   static {
      LOGGER.info("Initializing SpongeMixinsCore");
   }
}
