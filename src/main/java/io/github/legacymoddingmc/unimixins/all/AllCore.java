package io.github.legacymoddingmc.unimixins.all;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import io.github.legacymoddingmc.unimixins.compat.CompatCore;
import io.github.legacymoddingmc.unimixins.mixin.MixinCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MCVersion("1.7.10")
// The embedded core plugins aren't real, so we hoist their exclusions here.
@IFMLLoadingPlugin.TransformerExclusions({
        "io.github.legacymoddingmc.unimixins.compat.MixinCore",
        "io.github.legacymoddingmc.unimixins.compat.CompatCore",
        "io.github.legacymoddingmc.unimixins.compat.asm"
})
public class AllCore implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("unimixins");

    private static String[] embeddedCorePluginClassNames = new String[] {
//            "io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin",
//            "com.gtnewhorizon.gtnhmixins.core.GTNHMixinsCore",
//            "io.github.legacymoddingmc.unimixins.mixinextras.MixinExtrasCore"
    };

    private static final Class<?>[] knownCLaxxes = new Class[] {
        MixinCore.class,
        CompatCore.class
    };
    private static final List<Class<?>> embeddedCorePluginClasses = new ArrayList<>(Arrays.asList(knownCLaxxes));

    private static final List<IFMLLoadingPlugin> embeddedCorePluginInstances = new ArrayList<>();

    static {
        try {
            for (String s : embeddedCorePluginClassNames) {
                Class<?> cls = Class.forName(s);
                embeddedCorePluginClasses.add(cls);
            }
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public AllCore() {
        LOGGER.info("Instantiating AllCore");

        for (Class<?> cls : embeddedCorePluginClasses) {
            try {
                embeddedCorePluginInstances.add((IFMLLoadingPlugin)cls.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        final ArrayList<String> classes = new ArrayList<>();
        for (IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            classes.addAll(Arrays.asList(p.getASMTransformerClass()));
        }
        return classes.toArray(new String[0]);
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        for (IFMLLoadingPlugin p : embeddedCorePluginInstances) {
            p.injectData(data);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return "io.github.legacymoddingmc.unimixins.all.AllCore$CombinedAccessTransformer";
    }

    public static class CombinedAccessTransformer implements IClassTransformer {
        private final List<IClassTransformer> delegates = new ArrayList<>();
        public CombinedAccessTransformer() {
            for (IFMLLoadingPlugin plugin : embeddedCorePluginInstances) {
                String atClass = plugin.getAccessTransformerClass();
                if (atClass != null) {
                    try {
                        delegates.add((IClassTransformer) Class.forName(atClass).newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            for (IClassTransformer delegate : delegates) {
                basicClass = delegate.transform(name, transformedName, basicClass);
            }
            return basicClass;
        }

        @Override
        public String toString() {
            return delegates.toString();
        }
    }

}
