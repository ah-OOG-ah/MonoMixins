package io.github.tox1cozz.mixinbooterlegacy;

import com.google.common.util.concurrent.Runnables;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import io.github.legacymoddingmc.unimixins.mixinbooterlegacy.MixinBooterLegacyModule;
import io.github.tox1cozz.mixinextras.MixinExtrasBootstrap;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

@Name("MixinBooterLegacy")
@MCVersion("1.7.10")
@SortingIndex(-2147483647)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name("MixinBooterLegacy")
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex(-2147483647)
public final class MixinBooterLegacyPlugin implements IFMLLoadingPlugin {
   public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

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
      Object coremodList = data.get("coremodList");
      if (coremodList instanceof List) {
         for (Object coremod : (List)coremodList) {
            try {
               Field field = coremod.getClass().getField("coreModInstance");
               field.setAccessible(true);
               Object theMod = field.get(coremod);
               if (theMod instanceof IEarlyMixinLoader) {
                  IEarlyMixinLoader loader = (IEarlyMixinLoader)theMod;

                  for (String mixinConfig : loader.getMixinConfigs()) {
                     if (loader.shouldMixinConfigQueue(mixinConfig)) {
                        LOGGER.info("Adding {} mixin configuration.", new Object[]{mixinConfig});
                        Mixins.addConfiguration(mixinConfig);
                        loader.onMixinConfigQueued(mixinConfig);
                     }
                  }
               }
            } catch (Exception var10) {
               LOGGER.error("Unexpected error", var10);
            }
         }
      }

      Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing()).run();
   }

   public String getAccessTransformerClass() {
      return null;
   }

   static {
      LOGGER.info("MixinBootstrap Initializing...");
      MixinBooterLegacyModule.init();
      MixinExtrasBootstrap.init();
      Mixins.addConfiguration("mixin.mixinbooterlegacy.json");
   }

   @Mod(modid = "mixinbooterlegacy", name = "MixinBooterLegacy", version = "1.2.1", acceptableRemoteVersions = "*")
   @net.minecraftforge.fml.common.Mod(modid = "mixinbooterlegacy", name = "MixinBooterLegacy", version = "1.2.1", acceptableRemoteVersions = "*")
   public static class Container {
   }
}
