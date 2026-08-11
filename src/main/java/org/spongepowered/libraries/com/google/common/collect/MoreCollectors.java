package org.spongepowered.libraries.com.google.common.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
@GwtCompatible
public final class MoreCollectors {
   private static final Collector<Object, ?, Optional<Object>> TO_OPTIONAL = Collector.of(
      MoreCollectors.ToOptionalState::new,
      MoreCollectors.ToOptionalState::add,
      MoreCollectors.ToOptionalState::combine,
      MoreCollectors.ToOptionalState::getOptional,
      Characteristics.UNORDERED
   );
   private static final Object NULL_PLACEHOLDER = new Object();
   private static final Collector<Object, ?, Object> ONLY_ELEMENT = Collector.of(
      MoreCollectors.ToOptionalState::new, (state, o) -> state.add(o == null ? NULL_PLACEHOLDER : o), MoreCollectors.ToOptionalState::combine, state -> {
         Object result = state.getElement();
         return result == NULL_PLACEHOLDER ? null : result;
      }, Characteristics.UNORDERED
   );

   public static <T> Collector<T, ?, Optional<T>> toOptional() {
      return (Collector<T, ?, Optional<T>>)TO_OPTIONAL;
   }

   public static <T> Collector<T, ?, T> onlyElement() {
      return (Collector<T, ?, T>)ONLY_ELEMENT;
   }

   private MoreCollectors() {
   }

   private static final class ToOptionalState {
      static final int MAX_EXTRAS = 4;
      @Nullable
      Object element = null;
      @Nullable
      List<Object> extras = null;

      ToOptionalState() {
      }

      IllegalArgumentException multiples(boolean overflow) {
         StringBuilder sb = new StringBuilder().append("expected one element but was: <").append(this.element);

         for (Object o : this.extras) {
            sb.append(", ").append(o);
         }

         if (overflow) {
            sb.append(", ...");
         }

         sb.append('>');
         throw new IllegalArgumentException(sb.toString());
      }

      void add(Object o) {
         Preconditions.checkNotNull(o);
         if (this.element == null) {
            this.element = o;
         } else if (this.extras == null) {
            this.extras = new ArrayList<>(4);
            this.extras.add(o);
         } else {
            if (this.extras.size() >= 4) {
               throw this.multiples(true);
            }

            this.extras.add(o);
         }
      }

      MoreCollectors.ToOptionalState combine(MoreCollectors.ToOptionalState other) {
         if (this.element == null) {
            return other;
         } else if (other.element == null) {
            return this;
         } else {
            if (this.extras == null) {
               this.extras = new ArrayList<>();
            }

            this.extras.add(other.element);
            if (other.extras != null) {
               this.extras.addAll(other.extras);
            }

            if (this.extras.size() > 4) {
               this.extras.subList(4, this.extras.size()).clear();
               throw this.multiples(true);
            } else {
               return this;
            }
         }
      }

      Optional<Object> getOptional() {
         if (this.extras == null) {
            return Optional.ofNullable(this.element);
         } else {
            throw this.multiples(false);
         }
      }

      Object getElement() {
         if (this.element == null) {
            throw new NoSuchElementException();
         } else if (this.extras == null) {
            return this.element;
         } else {
            throw this.multiples(false);
         }
      }
   }
}
