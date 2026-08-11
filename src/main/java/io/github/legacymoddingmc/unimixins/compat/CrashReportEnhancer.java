package io.github.legacymoddingmc.unimixins.compat;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

public class CrashReportEnhancer {
   public static void addMixinsToCrashReport(CrashReport crashReport, CrashReportCategory category) {
      try {
         Collection<String> classesInStackTrace = getClassesInStackTrace(crashReport);
         addMixinErrorsToCrashReport(category, classesInStackTrace);
         addMixinsToCrashReport(category, classesInStackTrace);
      } catch (Throwable var3) {
         CompatCore.LOGGER.error("Encountered error while enhancing crash report");
         var3.printStackTrace();
      }
   }

   private static void addMixinsToCrashReport(CrashReportCategory category, Collection<String> classesInStackTrace) {
      String msg = "";

      for (String s : classesInStackTrace) {
         Collection<IMixinInfo> mixins = getMixinsForClass(s);
         if (!mixins.isEmpty()) {
            msg = msg + "\n\t\t" + s + ":";

            for (IMixinInfo mi : mixins) {
               msg = msg + "\n\t\t\t" + mi;
            }
         }
      }

      if (!msg.isEmpty()) {
         category.addCrashSection("Mixins in Stacktrace", msg);
      }
   }

   private static Collection<IMixinInfo> getMixinsForClass(String s) {
      try {
         ClassInfo ci = ClassInfo.fromCache(s);
         if (ci != null) {
            Field f = ClassInfo.class.getDeclaredField("mixins");
            if (f != null) {
               f.setAccessible(true);
               return (Set)f.get(ci);
            }
         }
      } catch (Exception var3) {
      }

      return Collections.emptySet();
   }

   private static void addMixinErrorsToCrashReport(CrashReportCategory category, Collection<String> classesInStackTrace) {
      Map<String, List<String>> errors = new HashMap<>();

      for (String s : classesInStackTrace) {
         List<String> e = MixinErrorHandler.getErrorsForClass(s);
         if (!e.isEmpty()) {
            errors.put(s, e);
         }
      }

      if (!errors.isEmpty()) {
         String msg = "";

         for (Entry<String, List<String>> e : errors.entrySet()) {
            String cls = e.getKey();
            List<String> clsErrors = e.getValue();
            msg = msg + "\n\t\t" + cls + ":";

            for (String clsError : e.getValue()) {
               msg = msg + "\n\t\t\t" + clsError;
            }
         }

         category.addCrashSection(String.format("Mixin Errors in Stacktrace"), msg);
      }
   }

   private static Collection<String> getClassesInStackTrace(CrashReport crashReport) {
      Set<String> classes = new HashSet<>();

      for (Throwable th = crashReport.getCrashCause(); th != null; th = th.getCause()) {
         if (th instanceof ClassNotFoundException) {
            String msg = th.getMessage();
            if (msg != null && !msg.isEmpty()) {
               classes.add(msg);
            }
         }

         for (StackTraceElement elem : th.getStackTrace()) {
            classes.add(elem.getClassName());
         }
      }

      return classes;
   }
}
