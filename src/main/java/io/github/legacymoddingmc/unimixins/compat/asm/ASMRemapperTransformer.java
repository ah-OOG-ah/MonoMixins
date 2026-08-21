package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;
import static org.objectweb.asm.Opcodes.ASM9;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/// The ASM package name used by Mixin differs between mixin loaders. This transformer remaps references to ASM to use
/// the correct package name [org.objectweb.asm], regardless of what mixin loader they were compiled against.
///
/// The following packages are remapped:
///   - `org.spongepowered.libraries.org.objectweb.asm` (MixinBooterLegacy, GTNHMixins)
///   - `org.spongepowered.asm.lib` (Mixin 0.7, UniMixins)
@SuppressWarnings("unused")
public class ASMRemapperTransformer implements IClassTransformer {

    private static final String ASM_PACKAGE_UNSHADED = "org/objectweb/asm/";
    private static final String ASM_PACKAGE_LEGACY = "org/spongepowered/asm/lib/";
    private static final String ASM_PACKAGE_MBL = "org/spongepowered/libraries/org/objectweb/asm/";

    private static final String[] ASM_SHADED_PREFIXES = new String[]{
            ASM_PACKAGE_LEGACY,
            ASM_PACKAGE_MBL
    };

    private static String realASMPackagePrefix;
    private final BytePatternMatcher wrongAsmMatcher =
            new BytePatternMatcher(ASM_SHADED_PREFIXES, BytePatternMatcher.Mode.Contains);

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (transformedName.startsWith("io.github.legacymoddingmc.unimixins.compat.asm.")
            || transformedName.startsWith("com.google.")
            || transformedName.startsWith("org.apache.")
            || transformedName.startsWith("org.objectweb.asm.")
        ) {
            return basicClass;
        }

        ClassReader classReader = new ClassReader(basicClass);

        boolean foundWrongAsm = matchUtf8Constant(classReader, basicClass, wrongAsmMatcher);
        if (!foundWrongAsm) return basicClass;

        LOGGER.info("Transforming class {} to unshade ASM.", transformedName);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ASMRemappingAdapter remapAdapter = new ASMRemappingAdapter(classWriter);
        classReader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static boolean matchUtf8Constant(ClassReader classReader, byte[] basicClass, BytePatternMatcher matcher) {
        final int itemCount = classReader.getItemCount();

        // Constant pool entries start from index 1
        for (int i = 1; i < itemCount; i++) {
            final int itemOffset = classReader.getItem(i);

            // Long and double take two CP slots, the second slot has no item
            if (itemOffset == 0) {
                continue;
            }

            // Only match the UTF8 constant (which tag is 1).
            // getItem(i) points after the tag byte, i.e. at the first length byte.
            // [tag][u2 length][bytes...]
            if (basicClass[itemOffset - 1] != 1) {
                continue;
            }

            // Length takes 2 bytes, so we read them and skip these 2 bytes
            final int utfLen = classReader.readUnsignedShort(itemOffset);
            final int utfStart = itemOffset + 2;

            if (matcher.matches(basicClass, utfStart, utfLen)) {
                return true;
            }
        }

        return false;
    }

    private static class ASMRemappingAdapter extends ClassRemapper {
        public ASMRemappingAdapter(ClassWriter classWriter) {
            super(classWriter, ASMRemapper.INSTANCE);
        }

        private static class ASMRemapper extends Remapper {
            private ASMRemapper() {
                super(ASM9);
            }

            private static final Remapper INSTANCE = new ASMRemapper();

            @Override
            public String map(String typeName) {
                for (String s : ASM_SHADED_PREFIXES) {
                    if (typeName.startsWith(s)) return ASM_PACKAGE_UNSHADED + typeName.substring(s.length());
                }
                return super.map(typeName);
            }
        }
    }
}
