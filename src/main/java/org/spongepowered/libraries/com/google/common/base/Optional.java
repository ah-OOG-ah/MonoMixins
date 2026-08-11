package org.spongepowered.libraries.com.google.common.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(serializable = true)
public abstract class Optional<T> implements Serializable {
   private static final long serialVersionUID = 0L;

   public static <T> Optional<T> absent() {
      return Absent.withType();
   }

   public static <T> Optional<T> of(T reference) {
      return new Present<>(Preconditions.checkNotNull(reference));
   }

   public static <T> Optional<T> fromNullable(@Nullable T nullableReference) {
      return (Optional<T>)(nullableReference == null ? absent() : new Present<>(nullableReference));
   }

   @Nullable
   public static <T> Optional<T> fromJavaUtil(@Nullable java.util.Optional<T> javaUtilOptional) {
      return javaUtilOptional == null ? null : fromNullable(javaUtilOptional.orElse(null));
   }

   @Nullable
   public static <T> java.util.Optional<T> toJavaUtil(@Nullable Optional<T> googleOptional) {
      return googleOptional == null ? null : googleOptional.toJavaUtil();
   }

   Optional() {
   }

   public abstract boolean isPresent();

   public abstract T get();

   public abstract T or(T var1);

   public abstract Optional<T> or(Optional<? extends T> var1);

   @Beta
   public abstract T or(Supplier<? extends T> var1);

   @Nullable
   public abstract T orNull();

   public abstract Set<T> asSet();

   public abstract <V> Optional<V> transform(Function<? super T, V> var1);

   public java.util.Optional<T> toJavaUtil() {
      return java.util.Optional.ofNullable(this.orNull());
   }

   @Override
   public abstract boolean equals(@Nullable Object var1);

   @Override
   public abstract int hashCode();

   @Override
   public abstract String toString();

   @Beta
   public static <T> Iterable<T> presentInstances(final Iterable<? extends Optional<? extends T>> optionals) {
      Preconditions.checkNotNull(optionals);
      return new Iterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
               private final Iterator<? extends Optional<? extends T>> iterator = Preconditions.checkNotNull(optionals.iterator());

               @Override
               protected T computeNext() {
                  while (this.iterator.hasNext()) {
                     Optional<? extends T> optional = (Optional<? extends T>)this.iterator.next();
                     if (optional.isPresent()) {
                        return (T)optional.get();
                     }
                  }

                  return (T)this.endOfData();
               }
            };
         }
      };
   }
}
