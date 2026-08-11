package com.gtnewhorizon.gtnhmixins.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Runnables;
import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.Reflection;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import io.github.legacymoddingmc.unimixins.gtnhmixins.GTNHMixinsModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.service.MixinService;

@MCVersion("1.7.10")
@SortingIndex(-2147483643)
@Name("GTNHMixins Core Plugin")
@TransformerExclusions("com.gtnewhorizon.gtnhmixins.core")
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex(-2147483643)
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name("GTNHMixins Core Plugin")
@net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions("com.gtnewhorizon.gtnhmixins.core")
public class GTNHMixinsCore implements IFMLLoadingPlugin {
   public static final String PLUGIN_NAME = "GTNHMixins Core Plugin";
   public static final Logger LOGGER = LogManager.getLogger("GTNHMixins Core Plugin");
   private static boolean isObf;
   public static final Map<String, String> MANUALLY_IDENTIFIED_COREMODS = ImmutableMap.builder()
      .put("optifine.OptiFineForgeTweaker", "optifine.OptiFineForgeTweaker")
      .put("codechicken.lib.asm.ModularASMTransformer", "codechicken.lib")
      .put("org.bukkit.World", "Bukkit")
      .build();

   public String[] getASMTransformerClass() {
      return new String[0];
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   private Set<String> getLoadedCoremods(List<?> coremodList) {
      Set<String> loadedCoremods = new HashSet<>();
      MANUALLY_IDENTIFIED_COREMODS.forEach((clsName, coreModIdentifier) -> {
         try {
            MixinService.getService().getBytecodeProvider().getClassNode(clsName, false);
            loadedCoremods.add(coreModIdentifier);
         } catch (Exception var4x) {
         }
      });

      for (Object tweak : (ArrayList)Launch.blackboard.get("Tweaks")) {
         try {
            Object obj = Reflection.pluginWrapperClass.isInstance(tweak) ? Reflection.coreModInstanceField.get(tweak) : tweak;
            loadedCoremods.add(obj.toString().split("@")[0]);
         } catch (Exception var8) {
         }
      }

      for (Object coremod : coremodList) {
         try {
            Object theMod = Reflection.coreModInstanceField.get(coremod);
            loadedCoremods.add(theMod.toString().split("@")[0]);
         } catch (Exception var7) {
         }
      }

      return loadedCoremods;
   }

   public void injectData(Map<String, Object> data) {
      isObf = (Boolean)data.get("runtimeDeobfuscationEnabled");
      GTNHMixins.log("Searching coremodList for IEarlyMixinLoaders");
      Object coremodList = data.get("coremodList");
      if (coremodList instanceof List) {
         Set<String> loadedCoremods = this.getLoadedCoremods((List<?>)coremodList);
         GTNHMixins.log("LoadedCoreMods {}", loadedCoremods.toString());

         for (Object coremod : (List)coremodList) {
            try {
               Object theMod = Reflection.coreModInstanceField.get(coremod);
               if (theMod instanceof IEarlyMixinLoader) {
                  GTNHMixins.log("Loading mixins from IEarlyMixinLoader [{}]", theMod.getClass().getName());
                  IEarlyMixinLoader loader = (IEarlyMixinLoader)theMod;
                  String mixinConfig = loader.getMixinConfig();
                  Config config = Config.create(mixinConfig, null);
                  List<String> mixins = loader.getMixins(loadedCoremods);

                  for (String mixin : mixins) {
                     GTNHMixins.log("Loading [{}] {}", mixinConfig, mixin);
                  }

                  Reflection.mixinClassesField.set(Reflection.configField.get(config), mixins);
                  Reflection.registerConfigurationMethod.invoke(null, config);
               }
            } catch (ReflectiveOperationException var13) {
               GTNHMixins.LOGGER.error("Unexpected error", var13);
            }
         }
      }

      Launch.blackboard.getOrDefault("unimixins.mixinModidDecorator.refresh", Runnables.doNothing()).run();
   }

   public String getAccessTransformerClass() {
      return null;
   }

   public static boolean isObf() {
      return isObf;
   }

   static {
      GTNHMixins.LOGGER.debug("Initializing GTNHMixins Core");
      GTNHMixinsModule.init();
   }
}
