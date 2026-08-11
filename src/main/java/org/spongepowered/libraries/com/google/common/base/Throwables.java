package org.spongepowered.libraries.com.google.common.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(emulated = true)
public final class Throwables {
   @GwtIncompatible
   private static final String JAVA_LANG_ACCESS_CLASSNAME = "sun.misc.JavaLangAccess";
   @GwtIncompatible
   @VisibleForTesting
   static final String SHARED_SECRETS_CLASSNAME = "sun.misc.SharedSecrets";
   @Nullable
   @GwtIncompatible
   private static final Object jla = getJLA();
   @Nullable
   @GwtIncompatible
   private static final Method getStackTraceElementMethod = jla == null ? null : getGetMethod();
   @Nullable
   @GwtIncompatible
   private static final Method getStackTraceDepthMethod = jla == null ? null : getSizeMethod();

   private Throwables() {
   }

   @GwtIncompatible
   public static <X extends Throwable> void throwIfInstanceOf(Throwable throwable, Class<X> declaredType) throws X {
      Preconditions.checkNotNull(throwable);
      if (declaredType.isInstance(throwable)) {
         throw declaredType.cast(throwable);
      }
   }

   @Deprecated
   @GwtIncompatible
   public static <X extends Throwable> void propagateIfInstanceOf(@Nullable Throwable throwable, Class<X> declaredType) throws X {
      if (throwable != null) {
         throwIfInstanceOf(throwable, declaredType);
      }
   }

   public static void throwIfUnchecked(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      } else if (throwable instanceof Error) {
         throw (Error)throwable;
      }
   }

   @Deprecated
   @GwtIncompatible
   public static void propagateIfPossible(@Nullable Throwable throwable) {
      if (throwable != null) {
         throwIfUnchecked(throwable);
      }
   }

   @GwtIncompatible
   public static <X extends Throwable> void propagateIfPossible(@Nullable Throwable throwable, Class<X> declaredType) throws X {
      propagateIfInstanceOf(throwable, declaredType);
      propagateIfPossible(throwable);
   }

   @GwtIncompatible
   public static <X1 extends Throwable, X2 extends Throwable> void propagateIfPossible(
      @Nullable Throwable throwable, Class<X1> declaredType1, Class<X2> declaredType2
   ) throws X1, X2 {
      Preconditions.checkNotNull(declaredType2);
      propagateIfInstanceOf(throwable, declaredType1);
      propagateIfPossible(throwable, declaredType2);
   }

   @Deprecated
   @CanIgnoreReturnValue
   @GwtIncompatible
   public static RuntimeException propagate(Throwable throwable) {
      throwIfUnchecked(throwable);
      throw new RuntimeException(throwable);
   }

   public static Throwable getRootCause(Throwable throwable) {
      Throwable cause;
      while ((cause = throwable.getCause()) != null) {
         throwable = cause;
      }

      return throwable;
   }

   @Beta
   public static List<Throwable> getCausalChain(Throwable throwable) {
      Preconditions.checkNotNull(throwable);

      List<Throwable> causes;
      for (causes = new ArrayList<>(4); throwable != null; throwable = throwable.getCause()) {
         causes.add(throwable);
      }

      return Collections.unmodifiableList(causes);
   }

   @GwtIncompatible
   public static String getStackTraceAsString(Throwable throwable) {
      StringWriter stringWriter = new StringWriter();
      throwable.printStackTrace(new PrintWriter(stringWriter));
      return stringWriter.toString();
   }

   @Beta
   @GwtIncompatible
   public static List<StackTraceElement> lazyStackTrace(Throwable throwable) {
      return lazyStackTraceIsLazy() ? jlaStackTrace(throwable) : Collections.unmodifiableList(Arrays.asList(throwable.getStackTrace()));
   }

   @Beta
   @GwtIncompatible
   public static boolean lazyStackTraceIsLazy() {
      return getStackTraceElementMethod != null & getStackTraceDepthMethod != null;
   }

   @GwtIncompatible
   private static List<StackTraceElement> jlaStackTrace(final Throwable t) {
      Preconditions.checkNotNull(t);
      return new AbstractList<StackTraceElement>() {
         public StackTraceElement get(int n) {
            return (StackTraceElement)Throwables.invokeAccessibleNonThrowingMethod(Throwables.getStackTraceElementMethod, Throwables.jla, t, n);
         }

         @Override
         public int size() {
            return (Integer)Throwables.invokeAccessibleNonThrowingMethod(Throwables.getStackTraceDepthMethod, Throwables.jla, t);
         }
      };
   }

   @GwtIncompatible
   private static Object invokeAccessibleNonThrowingMethod(Method method, Object receiver, Object... params) {
      try {
         return method.invoke(receiver, params);
      } catch (IllegalAccessException var4) {
         throw new RuntimeException(var4);
      } catch (InvocationTargetException var5) {
         throw propagate(var5.getCause());
      }
   }

   @Nullable
   @GwtIncompatible
   private static Object getJLA() {
      try {
         Class<?> sharedSecrets = Class.forName("sun.misc.SharedSecrets", false, null);
         Method langAccess = sharedSecrets.getMethod("getJavaLangAccess");
         return langAccess.invoke(null);
      } catch (ThreadDeath var2) {
         throw var2;
      } catch (Throwable var3) {
         return null;
      }
   }

   @Nullable
   @GwtIncompatible
   private static Method getGetMethod() {
      return getJlaMethod("getStackTraceElement", Throwable.class, int.class);
   }

   @Nullable
   @GwtIncompatible
   private static Method getSizeMethod() {
      return getJlaMethod("getStackTraceDepth", Throwable.class);
   }

   @Nullable
   @GwtIncompatible
   private static Method getJlaMethod(String name, Class<?>... parameterTypes) throws ThreadDeath {
      try {
         return Class.forName("sun.misc.JavaLangAccess", false, null).getMethod(name, parameterTypes);
      } catch (ThreadDeath var3) {
         throw var3;
      } catch (Throwable var4) {
         return null;
      }
   }
}
