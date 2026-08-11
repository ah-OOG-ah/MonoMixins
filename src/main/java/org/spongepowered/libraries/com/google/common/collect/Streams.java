package org.spongepowered.libraries.com.google.common.collect;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.math.LongMath;

@Beta
@GwtCompatible
public final class Streams {
   public static <T> Stream<T> stream(Iterable<T> iterable) {
      return iterable instanceof Collection ? ((Collection)iterable).stream() : StreamSupport.stream(iterable.spliterator(), false);
   }

   @Deprecated
   public static <T> Stream<T> stream(Collection<T> collection) {
      return collection.stream();
   }

   public static <T> Stream<T> stream(Iterator<T> iterator) {
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
   }

   public static <T> Stream<T> stream(Optional<T> optional) {
      return optional.isPresent() ? Stream.of(optional.get()) : Stream.of((T[])(new Object[0]));
   }

   public static <T> Stream<T> stream(java.util.Optional<T> optional) {
      return optional.isPresent() ? Stream.of(optional.get()) : Stream.of((T[])(new Object[0]));
   }

   @SafeVarargs
   public static <T> Stream<T> concat(Stream<? extends T>... streams) {
      boolean isParallel = false;
      int characteristics = 336;
      long estimatedSize = 0L;
      ImmutableList.Builder<Spliterator<? extends T>> splitrsBuilder = new ImmutableList.Builder<>(streams.length);

      for (Stream<? extends T> stream : streams) {
         isParallel |= stream.isParallel();
         Spliterator<? extends T> splitr = stream.spliterator();
         splitrsBuilder.add(splitr);
         characteristics &= splitr.characteristics();
         estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
      }

      return StreamSupport.stream(
         CollectSpliterators.flatMap(splitrsBuilder.build().spliterator(), splitrx -> splitrx, characteristics, estimatedSize), isParallel
      );
   }

   public static IntStream concat(IntStream... streams) {
      return Stream.of(streams).flatMapToInt(stream -> (IntStream)stream);
   }

   public static LongStream concat(LongStream... streams) {
      return Stream.of(streams).flatMapToLong(stream -> (LongStream)stream);
   }

   public static DoubleStream concat(DoubleStream... streams) {
      return Stream.of(streams).flatMapToDouble(stream -> (DoubleStream)stream);
   }

   public static IntStream stream(OptionalInt optional) {
      return optional.isPresent() ? IntStream.of(optional.getAsInt()) : IntStream.empty();
   }

   public static LongStream stream(OptionalLong optional) {
      return optional.isPresent() ? LongStream.of(optional.getAsLong()) : LongStream.empty();
   }

   public static DoubleStream stream(OptionalDouble optional) {
      return optional.isPresent() ? DoubleStream.of(optional.getAsDouble()) : DoubleStream.empty();
   }

   public static <T> java.util.Optional<T> findLast(Stream<T> stream) {
      class OptionalState<T> {
         boolean set = false;
         T value = (T)null;

         void set(@Nullable T value) {
            this.set = true;
            this.value = value;
         }

         T get() {
            Preconditions.checkState(this.set);
            return this.value;
         }
      }

      OptionalState<T> state = new OptionalState<>();
      Deque<Spliterator<T>> splits = new ArrayDeque<>();
      splits.addLast(stream.spliterator());

      while (!splits.isEmpty()) {
         Spliterator<T> spliterator = splits.removeLast();
         if (spliterator.getExactSizeIfKnown() != 0L) {
            if (spliterator.hasCharacteristics(16384)) {
               while (true) {
                  Spliterator<T> prefix = spliterator.trySplit();
                  if (prefix == null || prefix.getExactSizeIfKnown() == 0L) {
                     break;
                  }

                  if (spliterator.getExactSizeIfKnown() == 0L) {
                     spliterator = prefix;
                     break;
                  }
               }

               spliterator.forEachRemaining(state::set);
               return java.util.Optional.of(state.get());
            }

            Spliterator<T> prefixx = spliterator.trySplit();
            if (prefixx != null && prefixx.getExactSizeIfKnown() != 0L) {
               splits.addLast(prefixx);
               splits.addLast(spliterator);
            } else {
               spliterator.forEachRemaining(state::set);
               if (state.set) {
                  return java.util.Optional.of(state.get());
               }
            }
         }
      }

      return java.util.Optional.empty();
   }

   public static OptionalInt findLast(IntStream stream) {
      java.util.Optional<Integer> boxedLast = findLast(stream.boxed());
      return boxedLast.isPresent() ? OptionalInt.of(boxedLast.get()) : OptionalInt.empty();
   }

   public static OptionalLong findLast(LongStream stream) {
      java.util.Optional<Long> boxedLast = findLast(stream.boxed());
      return boxedLast.isPresent() ? OptionalLong.of(boxedLast.get()) : OptionalLong.empty();
   }

   public static OptionalDouble findLast(DoubleStream stream) {
      java.util.Optional<Double> boxedLast = findLast(stream.boxed());
      return boxedLast.isPresent() ? OptionalDouble.of(boxedLast.get()) : OptionalDouble.empty();
   }

   public static <A, B, R> Stream<R> zip(Stream<A> streamA, Stream<B> streamB, final BiFunction<? super A, ? super B, R> function) {
      Preconditions.checkNotNull(streamA);
      Preconditions.checkNotNull(streamB);
      Preconditions.checkNotNull(function);
      boolean isParallel = streamA.isParallel() || streamB.isParallel();
      Spliterator<A> splitrA = streamA.spliterator();
      Spliterator<B> splitrB = streamB.spliterator();
      int characteristics = splitrA.characteristics() & splitrB.characteristics() & 80;
      final Iterator<A> itrA = Spliterators.iterator(splitrA);
      final Iterator<B> itrB = Spliterators.iterator(splitrB);
      return StreamSupport.stream(new AbstractSpliterator<R>(Math.min(splitrA.estimateSize(), splitrB.estimateSize()), characteristics) {
         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (itrA.hasNext() && itrB.hasNext()) {
               action.accept(function.apply(itrA.next(), itrB.next()));
               return true;
            } else {
               return false;
            }
         }
      }, isParallel);
   }

   public static <T, R> Stream<R> mapWithIndex(Stream<T> stream, final Streams.FunctionWithIndex<? super T, ? extends R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      Spliterator<T> fromSpliterator = stream.spliterator();
      if (!fromSpliterator.hasCharacteristics(16384)) {
         final Iterator<T> fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept((R)function.apply(fromIterator.next(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel);
      } else {
         class Splitr extends Streams.MapWithIndexSpliterator<Spliterator<T>, R, Splitr> implements Consumer<T> {
            T holder;

            Splitr(Spliterator<T> splitr, long index) {
               super(splitr, index);
            }

            @Override
            public void accept(@Nullable T t) {
               this.holder = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (this.fromSpliterator.tryAdvance(this)) {
                  boolean var2;
                  try {
                     action.accept((R)function.apply(this.holder, this.index++));
                     var2 = true;
                  } finally {
                     this.holder = null;
                  }

                  return var2;
               } else {
                  return false;
               }
            }

            Splitr createSplit(Spliterator<T> from, long i) {
               return new Splitr(from, i);
            }
         }

         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel);
      }
   }

   public static <R> Stream<R> mapWithIndex(IntStream stream, final Streams.IntFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfInt fromSpliterator = stream.spliterator();
      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfInt fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextInt(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel);
      } else {
         class Splitr extends Streams.MapWithIndexSpliterator<OfInt, R, Splitr> implements IntConsumer, Spliterator<R> {
            int holder;

            Splitr(OfInt splitr, long index) {
               super(splitr, index);
            }

            @Override
            public void accept(int t) {
               this.holder = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (this.fromSpliterator.tryAdvance((IntConsumer)this)) {
                  action.accept(function.apply(this.holder, this.index++));
                  return true;
               } else {
                  return false;
               }
            }

            Splitr createSplit(OfInt from, long i) {
               return new Splitr(from, i);
            }
         }

         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel);
      }
   }

   public static <R> Stream<R> mapWithIndex(LongStream stream, final Streams.LongFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfLong fromSpliterator = stream.spliterator();
      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfLong fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextLong(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel);
      } else {
         class Splitr extends Streams.MapWithIndexSpliterator<OfLong, R, Splitr> implements LongConsumer, Spliterator<R> {
            long holder;

            Splitr(OfLong splitr, long index) {
               super(splitr, index);
            }

            @Override
            public void accept(long t) {
               this.holder = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (this.fromSpliterator.tryAdvance((LongConsumer)this)) {
                  action.accept(function.apply(this.holder, this.index++));
                  return true;
               } else {
                  return false;
               }
            }

            Splitr createSplit(OfLong from, long i) {
               return new Splitr(from, i);
            }
         }

         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel);
      }
   }

   public static <R> Stream<R> mapWithIndex(DoubleStream stream, final Streams.DoubleFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfDouble fromSpliterator = stream.spliterator();
      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfDouble fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextDouble(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel);
      } else {
         class Splitr extends Streams.MapWithIndexSpliterator<OfDouble, R, Splitr> implements DoubleConsumer, Spliterator<R> {
            double holder;

            Splitr(OfDouble splitr, long index) {
               super(splitr, index);
            }

            @Override
            public void accept(double t) {
               this.holder = t;
            }

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (this.fromSpliterator.tryAdvance((DoubleConsumer)this)) {
                  action.accept(function.apply(this.holder, this.index++));
                  return true;
               } else {
                  return false;
               }
            }

            Splitr createSplit(OfDouble from, long i) {
               return new Splitr(from, i);
            }
         }

         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel);
      }
   }

   private Streams() {
   }

   @Beta
   public interface DoubleFunctionWithIndex<R> {
      R apply(double var1, long var3);
   }

   @Beta
   public interface FunctionWithIndex<T, R> {
      R apply(T var1, long var2);
   }

   @Beta
   public interface IntFunctionWithIndex<R> {
      R apply(int var1, long var2);
   }

   @Beta
   public interface LongFunctionWithIndex<R> {
      R apply(long var1, long var3);
   }

   private abstract static class MapWithIndexSpliterator<F extends Spliterator<?>, R, S extends Streams.MapWithIndexSpliterator<F, R, S>>
      implements Spliterator<R> {
      final F fromSpliterator;
      long index;

      MapWithIndexSpliterator(F fromSpliterator, long index) {
         this.fromSpliterator = fromSpliterator;
         this.index = index;
      }

      abstract S createSplit(F var1, long var2);

      public S trySplit() {
         F split = (F)this.fromSpliterator.trySplit();
         if (split == null) {
            return null;
         } else {
            S result = this.createSplit(split, this.index);
            this.index = this.index + split.getExactSizeIfKnown();
            return result;
         }
      }

      @Override
      public long estimateSize() {
         return this.fromSpliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return this.fromSpliterator.characteristics() & 16464;
      }
   }
}
