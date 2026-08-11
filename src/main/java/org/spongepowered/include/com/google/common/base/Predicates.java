package org.spongepowered.include.com.google.common.base;

import java.io.Serializable;
import java.util.Collection;
import javax.annotation.Nullable;

public final class Predicates {
   private static final Joiner COMMA_JOINER = Joiner.on(',');

   public static <T> Predicate<T> isNull() {
      return Predicates.ObjectPredicate.IS_NULL.withNarrowedType();
   }

   public static <T> Predicate<T> equalTo(@Nullable T target) {
      return (Predicate<T>)(target == null ? isNull() : new Predicates.IsEqualToPredicate<>(target));
   }

   public static <T> Predicate<T> in(Collection<? extends T> target) {
      return new Predicates.InPredicate<>(target);
   }

   private static class InPredicate<T> implements Serializable, Predicate<T> {
      private final Collection<?> target;

      private InPredicate(Collection<?> target) {
         this.target = Preconditions.checkNotNull(target);
      }

      @Override
      public boolean apply(@Nullable T t) {
         try {
            return this.target.contains(t);
         } catch (NullPointerException var3) {
            return false;
         } catch (ClassCastException var4) {
            return false;
         }
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj instanceof Predicates.InPredicate) {
            Predicates.InPredicate<?> that = (Predicates.InPredicate<?>)obj;
            return this.target.equals(that.target);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.target.hashCode();
      }

      @Override
      public String toString() {
         return "Predicates.in(" + this.target + ")";
      }
   }

   private static class IsEqualToPredicate<T> implements Serializable, Predicate<T> {
      private final T target;

      private IsEqualToPredicate(T target) {
         this.target = target;
      }

      @Override
      public boolean apply(T t) {
         return this.target.equals(t);
      }

      @Override
      public int hashCode() {
         return this.target.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj instanceof Predicates.IsEqualToPredicate) {
            Predicates.IsEqualToPredicate<?> that = (Predicates.IsEqualToPredicate<?>)obj;
            return this.target.equals(that.target);
         } else {
            return false;
         }
      }

      @Override
      public String toString() {
         return "Predicates.equalTo(" + this.target + ")";
      }
   }

   static enum ObjectPredicate implements Predicate<Object> {
      ALWAYS_TRUE {
         @Override
         public boolean apply(@Nullable Object o) {
            return true;
         }

         @Override
         public String toString() {
            return "Predicates.alwaysTrue()";
         }
      },
      ALWAYS_FALSE {
         @Override
         public boolean apply(@Nullable Object o) {
            return false;
         }

         @Override
         public String toString() {
            return "Predicates.alwaysFalse()";
         }
      },
      IS_NULL {
         @Override
         public boolean apply(@Nullable Object o) {
            return o == null;
         }

         @Override
         public String toString() {
            return "Predicates.isNull()";
         }
      },
      NOT_NULL {
         @Override
         public boolean apply(@Nullable Object o) {
            return o != null;
         }

         @Override
         public String toString() {
            return "Predicates.notNull()";
         }
      };

      private ObjectPredicate() {
      }

      <T> Predicate<T> withNarrowedType() {
         return this;
      }
   }
}
