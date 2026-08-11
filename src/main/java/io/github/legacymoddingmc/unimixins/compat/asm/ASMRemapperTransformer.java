package io.github.legacymoddingmc.unimixins.compat.asm;

import io.github.legacymoddingmc.unimixins.compat.CompatCore;
import java.util.Arrays;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMRemapperTransformer implements IClassTransformer {
   private static final String ASM_PACKAGE_UNSHADED = "org/objectweb/asm/";
   private static final String ASM_PACKAGE_LEGACY = "org/spongepowered/asm/lib/";
   private static final String ASM_PACKAGE_MBL = "org/spongepowered/libraries/org/objectweb/asm/";
   private static final String REMAP_ANNOTATION = "Lio/github/legacymoddingmc/unimixins/compat/api/RemapASMForMixin;";
   private static final List<String> ASM_PACKAGE_PREFIXES = Arrays.asList(
      "org/objectweb/asm/", "org/spongepowered/asm/lib/", "org/spongepowered/libraries/org/objectweb/asm/"
   );
   private static String realASMPackagePrefix;
   private final BytePatternMatcher wrongAsmMatcher;
   private final BytePatternMatcher shadedAsmMatcher;
   private final BytePatternMatcher remapAnnotationMatcher;

   public ASMRemapperTransformer() {
      String[] wrongAsmPackagePrefixes = ASM_PACKAGE_PREFIXES.stream().filter(x -> !x.equals(getRealASMPackagePrefix())).toArray(String[]::new);
      String[] shadedAsmPackagePrefixes = new String[]{"org/spongepowered/asm/lib/", "org/spongepowered/libraries/org/objectweb/asm/"};
      this.wrongAsmMatcher = new BytePatternMatcher(wrongAsmPackagePrefixes, BytePatternMatcher.Mode.Contains);
      this.shadedAsmMatcher = new BytePatternMatcher(shadedAsmPackagePrefixes, BytePatternMatcher.Mode.Contains);
      this.remapAnnotationMatcher = new BytePatternMatcher("Lio/github/legacymoddingmc/unimixins/compat/api/RemapASMForMixin;", BytePatternMatcher.Mode.Equals);
   }

   public byte[] transform(String name, String transformedName, byte[] basicClass) {
      if (basicClass == null) {
         return null;
      } else if (!transformedName.startsWith("io.github.legacymoddingmc.unimixins.compat.asm.")
         && !transformedName.startsWith("com.google.")
         && !transformedName.startsWith("org.apache.")
         && !transformedName.startsWith("org.objectweb.asm.")) {
         ClassReader classReader = new ClassReader(basicClass);
         boolean foundWrongAsm = matchUtf8Constant(classReader, basicClass, this.wrongAsmMatcher);
         if (!foundWrongAsm) {
            return basicClass;
         } else {
            boolean doRemap = matchUtf8Constant(classReader, basicClass, this.shadedAsmMatcher);
            if (!doRemap) {
               for (String itf : classReader.getInterfaces()) {
                  if (itf.equals("org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin")) {
                     doRemap = true;
                     break;
                  }
               }
            }

            if (!doRemap) {
               doRemap = matchUtf8Constant(classReader, basicClass, this.remapAnnotationMatcher);
            }

            if (!doRemap) {
               return basicClass;
            } else {
               CompatCore.LOGGER.info("Transforming class {} to fit current mixin environment.", new Object[]{transformedName});
               ClassWriter classWriter = new ClassWriter(1);
               RemappingClassAdapter remapAdapter = new ASMRemapperTransformer.SpongepoweredASMRemappingAdapter(classWriter);
               classReader.accept(remapAdapter, 8);
               return classWriter.toByteArray();
            }
         }
      } else {
         return basicClass;
      }
   }

   private static boolean matchUtf8Constant(ClassReader classReader, byte[] basicClass, BytePatternMatcher matcher) {
      int itemCount = classReader.getItemCount();

      for (int i = 1; i < itemCount; i++) {
         int itemOffset = classReader.getItem(i);
         if (itemOffset != 0 && basicClass[itemOffset - 1] == 1) {
            int utfLen = classReader.readUnsignedShort(itemOffset);
            int utfStart = itemOffset + 2;
            if (matcher.matches(basicClass, utfStart, utfLen)) {
               return true;
            }
         }
      }

      return false;
   }

   private static String getRealASMPackagePrefix() {
      if (realASMPackagePrefix == null) {
         try {
            ClassReader cr = new ClassReader(Launch.classLoader.getClassBytes("org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin"));
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            for (MethodNode m : cn.methods) {
               if (m.name.equals("preApply")) {
                  int classNodeDescStart = StringUtils.ordinalIndexOf(m.desc, "L", 2);
                  int classNodeDescEnd = StringUtils.ordinalIndexOf(m.desc, "L", 3);
                  String classNodeName = m.desc.substring(classNodeDescStart + 1, classNodeDescEnd - 1);
                  realASMPackagePrefix = classNodeName.substring(0, classNodeName.indexOf("tree/ClassNode"));
                  break;
               }
            }
         } catch (Exception var10) {
            var10.printStackTrace();
            throw new RuntimeException("Failed to determine real package name Mixin's shaded ASM.");
         } finally {
            if (realASMPackagePrefix == null) {
               realASMPackagePrefix = "UNKNOWN";
            }

            CompatCore.LOGGER.debug("Resolved real package prefix to: " + realASMPackagePrefix);
         }
      }

      return realASMPackagePrefix;
   }

   private static class SpongepoweredASMRemapper extends Remapper {
      public static final Remapper INSTANCE = new ASMRemapperTransformer.SpongepoweredASMRemapper();

      public String map(String typeName) {
         for (String s : ASMRemapperTransformer.ASM_PACKAGE_PREFIXES) {
            if (typeName.startsWith(s)) {
               return ASMRemapperTransformer.getRealASMPackagePrefix() + typeName.substring(s.length());
            }
         }

         return super.map(typeName);
      }
   }

   private static class SpongepoweredASMRemappingAdapter extends RemappingClassAdapter {
      public SpongepoweredASMRemappingAdapter(ClassWriter classWriter) {
         super(classWriter, ASMRemapperTransformer.SpongepoweredASMRemapper.INSTANCE);
      }
   }
}
