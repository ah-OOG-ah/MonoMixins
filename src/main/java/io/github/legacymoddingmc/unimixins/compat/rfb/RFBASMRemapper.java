package io.github.legacymoddingmc.unimixins.compat.rfb;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

import com.gtnewhorizons.retrofuturabootstrap.api.BytePatternMatcher;
import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

/// Mixin 0.7 shaded ASM in org.spongepowered.asm.lib. Unimixins continued to support this at runtime... *and* compile
/// time, unfortunately. This means we have to *re*remap mods using this at runtime.
@SuppressWarnings("unused")
public class RFBASMRemapper implements RfbClassTransformer {

    private static final String ASM_PACKAGE_UNSHADED = "org/objectweb/asm/";
    private static final String ASM_PACKAGE_LEGACY = "org/spongepowered/asm/lib/";
    private static final String ASM_PACKAGE_MBL = "org/spongepowered/libraries/org/objectweb/asm/";

    private static final String[] ASM_PACKAGE_PREFIXES = new String[]{
            ASM_PACKAGE_LEGACY,
            ASM_PACKAGE_MBL
    };

    private static final BytePatternMatcher wrongAsmMatcher =
            new BytePatternMatcher(ASM_PACKAGE_PREFIXES, BytePatternMatcher.Mode.Contains);

    @Override
    public @NotNull String id() { return "asmreremapper"; }

    @Override
    public boolean shouldTransformClass(
            @NotNull ExtensibleClassLoader classLoader,
            @NotNull Context context,
            @Nullable Manifest manifest,
            @NotNull String className,
            @NotNull ClassNodeHandle classNode) {
        if (!classNode.isPresent()) return false;
        if (!isPackageTransformable(className)) return false;

        // always nonnull if this is a valid class
        assert classNode.getOriginalMetadata() != null;
        return classNode
                .getOriginalMetadata()
                .matchesBytes(classNode.getOriginalBytes(), wrongAsmMatcher);
    }

    @Override
    public boolean transformClassIfNeeded(
            @NotNull ExtensibleClassLoader classLoader,
            @NotNull Context context,
            @Nullable Manifest manifest,
            @NotNull String className,
            @NotNull ClassNodeHandle cnHandle) {
        LOGGER.info("Transforming class {} to fit current mixin environment.", className);
        final ClassNode classNode = cnHandle.getNode();
        if (classNode == null) return false;

        ClassNode transformed = new ClassNode();
        classNode.accept(new ASMClassRemapper(transformed));
        cnHandle.setNode(transformed);

        return true;
    }

    private boolean isPackageTransformable(String className) {
        return !className.startsWith("io.github.legacymoddingmc.unimixins.compat.asm.")
                && !className.startsWith("com.google.")
                && !className.startsWith("org.apache.")
                && !className.startsWith("org.objectweb.asm.");
    }

    private static class ASMClassRemapper extends ClassRemapper {

        public ASMClassRemapper(ClassVisitor classVisitor) {
            super(classVisitor, ASMRemapper.INSTANCE);
        }
    }

    private static class ASMRemapper extends Remapper {
        private static final ASMRemapper INSTANCE = new ASMRemapper();

        public ASMRemapper() { super(Opcodes.ASM9); }

        public String map(String typeName) {
            for (String s : ASM_PACKAGE_PREFIXES)
                if (typeName.startsWith(s)) return ASM_PACKAGE_UNSHADED + typeName.substring(s.length());
            return typeName;
        }
    }
}
