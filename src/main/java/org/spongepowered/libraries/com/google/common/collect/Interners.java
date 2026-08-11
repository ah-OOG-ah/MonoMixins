package org.spongepowered.libraries.com.google.common.collect;

import java.util.concurrent.ConcurrentMap;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Equivalence;
import org.spongepowered.libraries.com.google.common.base.Function;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
@GwtIncompatible
public final class Interners {
   private Interners() {
   }

   public static Interners.InternerBuilder newBuilder() {
      return new Interners.InternerBuilder();
   }

   public static <E> Interner<E> newStrongInterner() {
      return newBuilder().strong().build();
   }

   @GwtIncompatible("java.lang.ref.WeakReference")
   public static <E> Interner<E> newWeakInterner() {
      return newBuilder().weak().build();
   }

   public static <E> Function<E, E> asFunction(Interner<E> interner) {
      return new Interners.InternerFunction<>(Preconditions.checkNotNull(interner));
   }

   public static class InternerBuilder {
      private final MapMaker mapMaker = new MapMaker();
      private boolean strong = true;

      private InternerBuilder() {
      }

      public Interners.InternerBuilder strong() {
         this.strong = true;
         return this;
      }

      @GwtIncompatible("java.lang.ref.WeakReference")
      public Interners.InternerBuilder weak() {
         this.strong = false;
         return this;
      }

      public Interners.InternerBuilder concurrencyLevel(int concurrencyLevel) {
         this.mapMaker.concurrencyLevel(concurrencyLevel);
         return this;
      }

      public <E> Interner<E> build() {
         return (Interner<E>)(this.strong ? new Interners.StrongInterner<>(this.mapMaker) : new Interners.WeakInterner<>(this.mapMaker));
      }
   }

   private static class InternerFunction<E> implements Function<E, E> {
      private final Interner<E> interner;

      public InternerFunction(Interner<E> interner) {
         this.interner = interner;
      }

      @Override
      public E apply(E input) {
         return this.interner.intern(input);
      }

      @Override
      public int hashCode() {
         return this.interner.hashCode();
      }

      @Override
      public boolean equals(Object other) {
         if (other instanceof Interners.InternerFunction) {
            Interners.InternerFunction<?> that = (Interners.InternerFunction<?>)other;
            return this.interner.equals(that.interner);
         } else {
            return false;
         }
      }
   }

   @VisibleForTesting
   static final class StrongInterner<E> implements Interner<E> {
      @VisibleForTesting
      final ConcurrentMap<E, E> map;

      private StrongInterner(MapMaker mapMaker) {
         this.map = mapMaker.makeMap();
      }

      @Override
      public E intern(E sample) {
         E canonical = this.map.putIfAbsent(Preconditions.checkNotNull(sample), sample);
         return canonical == null ? sample : canonical;
      }
   }

   @VisibleForTesting
   static final class WeakInterner<E> implements Interner<E> {
      @VisibleForTesting
      final MapMakerInternalMap<E, Interners.WeakInterner.Dummy, ?, ?> map;

      private WeakInterner(MapMaker mapMaker) {
         this.map = mapMaker.weakKeys().keyEquivalence(Equivalence.equals()).makeCustomMap();
      }

      @Override
      public E intern(E sample) {
         Interners.WeakInterner.Dummy sneaky;
         do {
            MapMakerInternalMap.InternalEntry<E, Interners.WeakInterner.Dummy, ?> entry = this.map.getEntry(sample);
            if (entry != null) {
               sneaky = entry.getKey();
               if (sneaky != null) {
                  return (E)sneaky;
               }
            }

            sneaky = this.map.putIfAbsent(sample, Interners.WeakInterner.Dummy.VALUE);
         } while (sneaky != null);

         return sample;
      }

      private static enum Dummy {
         VALUE;
      }
   }
}
