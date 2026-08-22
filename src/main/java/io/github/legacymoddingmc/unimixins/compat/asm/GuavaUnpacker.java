package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.asm.ASMRemapperTransformer.GUAVA_DEPRECATED;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;

/// Guava 21.0 is provided via hidden classes; this transformer unpacks them at runtime
public class GuavaUnpacker implements IClassTransformer {
    private static final String TARGET = GUAVA_DEPRECATED.replace("/", ".");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.startsWith(TARGET)) return basicClass;

        byte[] ret;
        try (InputStream klass = GuavaUnpacker.class
                .getClassLoader()
                .getResourceAsStream(transformedName.replace(".", "/") + ".klass")) {
            ret = IOUtils.toByteArray(klass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }
}
