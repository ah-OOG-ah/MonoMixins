package com.gtnewhorizon.gtnhmixins.builders;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;

public interface ITargetMod {
   @Nonnull
   TargetModBuilder getBuilder();

   public static class Predicates {
      public static Predicate<String> equals(String str) {
         return s -> s.equals(str);
      }

      public static Predicate<String> contains(String str) {
         return s -> s.contains(str);
      }

      public static Predicate<String> endsWith(String str) {
         return s -> s.endsWith(str);
      }

      public static Predicate<String> startsWith(String str) {
         return s -> s.startsWith(str);
      }

      public static Predicate<String> matches(String str) {
         return s -> s.matches(str);
      }

      public static Predicate<String> versionGreater(String version) {
         return s -> new ComparableVersion(version).compareTo(new ComparableVersion(s)) > 0;
      }

      public static Predicate<String> versionLower(String version) {
         return s -> new ComparableVersion(version).compareTo(new ComparableVersion(s)) < 0;
      }

      public static Predicate<ClassNode> testModAnnotation(Predicate<String> modIdTest, Predicate<String> modNameTest, Predicate<String> modVersionTest) {
         if (modIdTest == null && modNameTest == null && modVersionTest == null) {
            throw new IllegalArgumentException("At least one of the Mod Annotation test predicates must be non null");
         } else {
            return cn -> {
               if (cn.visibleAnnotations == null) {
                  return false;
               } else {
                  for (AnnotationNode annotation : cn.visibleAnnotations) {
                     if ("Lcpw/mods/fml/common/Mod;".equals(annotation.desc) && annotation.values != null) {
                        String modId = "";
                        String modName = "";
                        String modVersion = "";
                        List<Object> values = annotation.values;
                        int size = values.size();

                        for (int i = 0; i < size - 1; i += 2) {
                           Object name = values.get(i);
                           Object value = values.get(i + 1);
                           if (name instanceof String && value instanceof String) {
                              if ("modid".equals(name)) {
                                 modId = (String)value;
                              } else if ("name".equals(name)) {
                                 modName = (String)value;
                              } else if ("version".equals(name)) {
                                 modVersion = (String)value;
                              }
                           }
                        }

                        boolean test = true;
                        if (modIdTest != null) {
                           test = modIdTest.test(modId);
                        }

                        if (modNameTest != null) {
                           test = test && modNameTest.test(modName);
                        }

                        if (modVersionTest != null) {
                           test = test && modVersionTest.test(modVersion);
                        }

                        return test;
                     }
                  }

                  return false;
               }
            };
         }
      }
   }
}
