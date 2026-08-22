package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;
import static org.objectweb.asm.Opcodes.ASM9;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

public class RemappingTransformer {

    private final String[] targetPackages;
    private final String[] resultPackages;
    private final String[] excludedPackages;
    private final BytePatternMatcher targetMatcher;
    private final String message;


    /// @apiNote excludedPackages should probably be fairly small, since it's checked as an array (not a set)
    public RemappingTransformer(String[] targetPackages, String[] resultPackages, String[] excludedPackages, String message) {
        this.targetPackages = targetPackages;
        this.resultPackages = resultPackages;
        this.excludedPackages = excludedPackages;

        targetMatcher = new BytePatternMatcher(targetPackages, BytePatternMatcher.Mode.Contains);
        this.message = message;
    }

    /// Transform the given classnode in-place, returns true if it did a transform.
    /// This does NOT execute pre-transform checks, it simply does the transformation.
    private ClassNode transform(String className, ClassNode original) {
        LOGGER.info("Transforming class {} {}", className, message);
        ClassNode output = new ClassNode();
        ClassRemapper remapAdapter = new ClassRemapper(output, new Remapper(ASM9) {
            @Override
            public String map(String typeName) {
                for (int i = 0; i < targetPackages.length; ++i) {
                    if (typeName.startsWith(targetPackages[i])) {
                        return resultPackages[i] + typeName.substring(targetPackages[i].length());
                    }
                }
                return super.map(typeName);
            }
        });
        original.accept(remapAdapter);
        return output;
    }

    public boolean isPackageTransformable(String className) {
        for (String exclusion : excludedPackages)
            if (className.startsWith(exclusion)) return false;
        return true;
    }

    public byte[] transform(String className, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!isPackageTransformable(className)) return basicClass;

        ClassReader classReader = new ClassReader(basicClass);

        boolean foundTarget = matchUtf8Constant(classReader, basicClass, targetMatcher);
        if (!foundTarget) return basicClass;

        // Wrap the byte array in a class node for transformation
        ClassNode original = new ClassNode();
        classReader.accept(original, ClassReader.EXPAND_FRAMES);
        ClassNode newNode = transform(className, original);
        ClassWriter output = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        newNode.accept(output);

        return output.toByteArray();
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
}
