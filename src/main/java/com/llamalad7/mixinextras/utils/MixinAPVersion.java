package com.llamalad7.mixinextras.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.util.VersionNumber;

public class MixinAPVersion {
   private static final String MIN_VERSION = "0.8.3";
   private static final VersionNumber MIN_VERSION_NUMBER = VersionNumber.parse("0.8.3");
   private static final String BOOTSTRAP_ClASS = "org/spongepowered/asm/launch/MixinBootstrap.class";
   private static boolean checked = false;

   public static void check(ProcessingEnvironment env) {
      if (!checked) {
         checked = true;

         try {
            InputStream is = findBootstrapClass();

            label73: {
               label79: {
                  try {
                     if (is == null) {
                        printFailed(env);
                        break label79;
                     }

                     ClassNode node = new ClassNode();
                     new ClassReader(is).accept(node, 1);
                     VersionNumber version = getBootstrapVersion(node);
                     if (version == null) {
                        printFailed(env);
                        break label73;
                     }

                     if (version.compareTo(MIN_VERSION_NUMBER) < 0) {
                        throw new IllegalStateException("MixinExtras requires the Mixin AP to be at least 0.8.3");
                     }
                  } catch (Throwable var5) {
                     if (is != null) {
                        try {
                           is.close();
                        } catch (Throwable var4) {
                           var5.addSuppressed(var4);
                        }
                     }

                     throw var5;
                  }

                  if (is != null) {
                     is.close();
                  }

                  return;
               }

               if (is != null) {
                  is.close();
               }

               return;
            }

            if (is != null) {
               is.close();
            }
         } catch (IOException var6) {
            printFailed(env);
         }
      }
   }

   private static VersionNumber getBootstrapVersion(ClassNode bootstrapClass) {
      for (FieldNode field : bootstrapClass.fields) {
         if (field.name.equals("VERSION")) {
            if (!(field.value instanceof String)) {
               return null;
            }

            return VersionNumber.parse((String)field.value);
         }
      }

      return null;
   }

   private static void printFailed(ProcessingEnvironment env) {
      env.getMessager().printMessage(Kind.WARNING, "[MixinExtras] Failed to determine Mixin version. Assuming >=0.8.3");
   }

   private static InputStream findBootstrapClass() throws IOException {
      Enumeration<URL> urls = MixinAPVersion.class.getClassLoader().getResources("org/spongepowered/asm/launch/MixinBootstrap.class");
      return !urls.hasMoreElements() ? null : urls.nextElement().openStream();
   }
}
