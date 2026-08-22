package io.github.legacymoddingmc.unimixins.compat.asm;

import net.minecraft.launchwrapper.IClassTransformer;

/// The ASM package name used by Mixin differs between mixin loaders. This transformer remaps references to ASM to use
/// the correct package name [org.objectweb.asm], regardless of what mixin loader they were compiled against.
///
/// The following packages are remapped:
///   - `org.spongepowered.libraries.org.objectweb.asm` (MixinBooterLegacy, GTNHMixins)
///   - `org.spongepowered.asm.lib` (Mixin 0.7, UniMixins)
@SuppressWarnings("unused")
public class ASMRemapperTransformer extends RemappingTransformer implements IClassTransformer {

    private static final String ASM_PACKAGE_UNSHADED = "org/objectweb/asm/";
    private static final String ASM_PACKAGE_LEGACY = "org/spongepowered/asm/lib/";
    private static final String ASM_PACKAGE_MBL = "org/spongepowered/libraries/org/objectweb/asm/";

    public static final String GUAVA_DEPRECATED = "io/github/legacymoddingmc/unimixins/deprecated/com/google";
    private static final String GUAVA_LEGACY = "org/spongepowered/libraries/com/google";

    // If RFB is present, it will handle ASM - we can skip that
    private static final String[] ASM_TARGETS = new String[]{ ASM_PACKAGE_LEGACY, ASM_PACKAGE_MBL, GUAVA_LEGACY };
    private static final String[] WRFB_TARGETS = new String[]{ GUAVA_LEGACY };

    private static final String[] ASM_RESULTS = new String[]{ ASM_PACKAGE_UNSHADED, ASM_PACKAGE_UNSHADED, GUAVA_DEPRECATED };
    private static final String[] WRFB_RESULTS = new String[]{ GUAVA_DEPRECATED };

    private static final boolean isRFBLoaded = System.getProperty("java.system.class.loader", "")
            .equals("com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader");

    public ASMRemapperTransformer() {
        super(
                isRFBLoaded ? WRFB_TARGETS : ASM_TARGETS,
                isRFBLoaded ? WRFB_RESULTS : ASM_RESULTS,
                new String[]{
                        "io.github.legacymoddingmc.unimixins.compat.asm.",
                        "com.google.",
                        "org.apache.",
                        "org.objectweb.asm."
                },
                isRFBLoaded ? "to reloacte Guava" : "to relocate Guava and ASM"
        );
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return transform(transformedName, basicClass);
    }
}
