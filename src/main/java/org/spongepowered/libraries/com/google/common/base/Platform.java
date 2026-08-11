package org.spongepowered.libraries.com.google.common.base;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
final class Platform {
   private static final Logger logger = Logger.getLogger(Platform.class.getName());
   private static final PatternCompiler patternCompiler = loadPatternCompiler();

   private Platform() {
   }

   static long systemNanoTime() {
      return System.nanoTime();
   }

   static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
      return matcher.precomputedInternal();
   }

   static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> enumClass, String value) {
      WeakReference<? extends Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
      return ref == null ? Optional.absent() : Optional.of(enumClass.cast(ref.get()));
   }

   static String formatCompact4Digits(double value) {
      return String.format(Locale.ROOT, "%.4g", value);
   }

   static boolean stringIsNullOrEmpty(@Nullable String string) {
      return string == null || string.isEmpty();
   }

   static CommonPattern compilePattern(String pattern) {
      Preconditions.checkNotNull(pattern);
      return patternCompiler.compile(pattern);
   }

   static boolean usingJdkPatternCompiler() {
      return patternCompiler instanceof Platform.JdkPatternCompiler;
   }

   private static PatternCompiler loadPatternCompiler() {
      ServiceLoader<PatternCompiler> loader = ServiceLoader.load(PatternCompiler.class);

      try {
         Iterator<PatternCompiler> it = loader.iterator();

         while (it.hasNext()) {
            try {
               return it.next();
            } catch (ServiceConfigurationError var3) {
               logPatternCompilerError(var3);
            }
         }
      } catch (ServiceConfigurationError var4) {
         logPatternCompilerError(var4);
      }

      return new Platform.JdkPatternCompiler();
   }

   private static void logPatternCompilerError(ServiceConfigurationError e) {
      logger.log(Level.WARNING, "Error loading regex compiler, falling back to next option", (Throwable)e);
   }

   private static final class JdkPatternCompiler implements PatternCompiler {
      private JdkPatternCompiler() {
      }

      @Override
      public CommonPattern compile(String pattern) {
         return new JdkPattern(Pattern.compile(pattern));
      }
   }
}
