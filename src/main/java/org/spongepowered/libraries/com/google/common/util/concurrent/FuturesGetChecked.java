package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.Ordering;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtIncompatible
final class FuturesGetChecked {
   private static final Ordering<Constructor<?>> WITH_STRING_PARAM_FIRST = Ordering.<Boolean>natural().onResultOf(new Function<Constructor<?>, Boolean>() {
      public Boolean apply(Constructor<?> input) {
         return Arrays.asList(input.getParameterTypes()).contains(String.class);
      }
   }).reverse();

   @CanIgnoreReturnValue
   static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass) throws X {
      return getChecked(bestGetCheckedTypeValidator(), future, exceptionClass);
   }

   @CanIgnoreReturnValue
   @VisibleForTesting
   static <V, X extends Exception> V getChecked(FuturesGetChecked.GetCheckedTypeValidator validator, Future<V> future, Class<X> exceptionClass) throws X {
      validator.validateClass(exceptionClass);

      try {
         return future.get();
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
         throw newWithCause(exceptionClass, var4);
      } catch (ExecutionException var5) {
         wrapAndThrowExceptionOrError(var5.getCause(), exceptionClass);
         throw new AssertionError();
      }
   }

   @CanIgnoreReturnValue
   static <V, X extends Exception> V getChecked(Future<V> future, Class<X> exceptionClass, long timeout, TimeUnit unit) throws X {
      bestGetCheckedTypeValidator().validateClass(exceptionClass);

      try {
         return future.get(timeout, unit);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         throw newWithCause(exceptionClass, var6);
      } catch (TimeoutException var7) {
         throw newWithCause(exceptionClass, var7);
      } catch (ExecutionException var8) {
         wrapAndThrowExceptionOrError(var8.getCause(), exceptionClass);
         throw new AssertionError();
      }
   }

   private static FuturesGetChecked.GetCheckedTypeValidator bestGetCheckedTypeValidator() {
      return FuturesGetChecked.GetCheckedTypeValidatorHolder.BEST_VALIDATOR;
   }

   @VisibleForTesting
   static FuturesGetChecked.GetCheckedTypeValidator weakSetValidator() {
      return FuturesGetChecked.GetCheckedTypeValidatorHolder.WeakSetValidator.INSTANCE;
   }

   @VisibleForTesting
   static FuturesGetChecked.GetCheckedTypeValidator classValueValidator() {
      return FuturesGetChecked.GetCheckedTypeValidatorHolder.ClassValueValidator.INSTANCE;
   }

   private static <X extends Exception> void wrapAndThrowExceptionOrError(Throwable cause, Class<X> exceptionClass) throws X {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else if (cause instanceof RuntimeException) {
         throw new UncheckedExecutionException(cause);
      } else {
         throw newWithCause(exceptionClass, cause);
      }
   }

   private static boolean hasConstructorUsableByGetChecked(Class<? extends Exception> exceptionClass) {
      try {
         Exception unused = newWithCause(exceptionClass, new Exception());
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   private static <X extends Exception> X newWithCause(Class<X> exceptionClass, Throwable cause) {
      List<Constructor<X>> constructors = Arrays.asList((Constructor<X>[])exceptionClass.getConstructors());

      for (Constructor<X> constructor : preferringStrings(constructors)) {
         X instance = (X)newFromConstructor(constructor, cause);
         if (instance != null) {
            if (instance.getCause() == null) {
               instance.initCause(cause);
            }

            return instance;
         }
      }

      throw new IllegalArgumentException("No appropriate constructor for exception of type " + exceptionClass + " in response to chained exception", cause);
   }

   private static <X extends Exception> List<Constructor<X>> preferringStrings(List<Constructor<X>> constructors) {
      return WITH_STRING_PARAM_FIRST.sortedCopy(constructors);
   }

   @Nullable
   private static <X> X newFromConstructor(Constructor<X> constructor, Throwable cause) {
      Class<?>[] paramTypes = constructor.getParameterTypes();
      Object[] params = new Object[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
         Class<?> paramType = paramTypes[i];
         if (paramType.equals(String.class)) {
            params[i] = cause.toString();
         } else {
            if (!paramType.equals(Throwable.class)) {
               return null;
            }

            params[i] = cause;
         }
      }

      try {
         return constructor.newInstance(params);
      } catch (IllegalArgumentException var6) {
         return null;
      } catch (InstantiationException var7) {
         return null;
      } catch (IllegalAccessException var8) {
         return null;
      } catch (InvocationTargetException var9) {
         return null;
      }
   }

   @VisibleForTesting
   static boolean isCheckedException(Class<? extends Exception> type) {
      return !RuntimeException.class.isAssignableFrom(type);
   }

   @VisibleForTesting
   static void checkExceptionClassValidity(Class<? extends Exception> exceptionClass) {
      Preconditions.checkArgument(isCheckedException(exceptionClass), "Futures.getChecked exception type (%s) must not be a RuntimeException", exceptionClass);
      Preconditions.checkArgument(
         hasConstructorUsableByGetChecked(exceptionClass),
         "Futures.getChecked exception type (%s) must be an accessible class with an accessible constructor whose parameters (if any) must be of type String and/or Throwable",
         exceptionClass
      );
   }

   private FuturesGetChecked() {
   }

   @VisibleForTesting
   interface GetCheckedTypeValidator {
      void validateClass(Class<? extends Exception> var1);
   }

   @VisibleForTesting
   static class GetCheckedTypeValidatorHolder {
      static final String CLASS_VALUE_VALIDATOR_NAME = FuturesGetChecked.GetCheckedTypeValidatorHolder.class.getName() + "$ClassValueValidator";
      static final FuturesGetChecked.GetCheckedTypeValidator BEST_VALIDATOR = getBestValidator();

      static FuturesGetChecked.GetCheckedTypeValidator getBestValidator() {
         try {
            Class<?> theClass = Class.forName(CLASS_VALUE_VALIDATOR_NAME);
            return (FuturesGetChecked.GetCheckedTypeValidator)theClass.getEnumConstants()[0];
         } catch (Throwable var1) {
            return FuturesGetChecked.weakSetValidator();
         }
      }

      @IgnoreJRERequirement
      static enum ClassValueValidator implements FuturesGetChecked.GetCheckedTypeValidator {
         INSTANCE;

         private static final ClassValue<Boolean> isValidClass = new ClassValue<Boolean>() {
            protected Boolean computeValue(Class<?> type) {
               FuturesGetChecked.checkExceptionClassValidity(type.asSubclass(Exception.class));
               return true;
            }
         };

         @Override
         public void validateClass(Class<? extends Exception> exceptionClass) {
            isValidClass.get(exceptionClass);
         }
      }

      static enum WeakSetValidator implements FuturesGetChecked.GetCheckedTypeValidator {
         INSTANCE;

         private static final Set<WeakReference<Class<? extends Exception>>> validClasses = new CopyOnWriteArraySet<>();

         @Override
         public void validateClass(Class<? extends Exception> exceptionClass) {
            for (WeakReference<Class<? extends Exception>> knownGood : validClasses) {
               if (exceptionClass.equals(knownGood.get())) {
                  return;
               }
            }

            FuturesGetChecked.checkExceptionClassValidity(exceptionClass);
            if (validClasses.size() > 1000) {
               validClasses.clear();
            }

            validClasses.add(new WeakReference<>(exceptionClass));
         }
      }
   }
}
