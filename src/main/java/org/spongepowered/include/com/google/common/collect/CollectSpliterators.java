package org.spongepowered.include.com.google.common.collect;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

final class CollectSpliterators {
   static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function) {
      return indexed(size, extraCharacteristics, function, null);
   }

   static <T> Spliterator<T> indexed(int size, final int extraCharacteristics, IntFunction<T> function, final Comparator<? super T> comparator) {
      if (comparator != null) {
         Preconditions.checkArgument((extraCharacteristics & 4) != 0);
      }

      class WithCharacteristics implements Spliterator<T> {
         private final Spliterator<T> delegate;

         WithCharacteristics(Spliterator<T> delegate) {
            this.delegate = delegate;
         }

         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
            return this.delegate.tryAdvance(action);
         }

         @Override
         public void forEachRemaining(Consumer<? super T> action) {
            this.delegate.forEachRemaining(action);
         }

         @Nullable
         @Override
         public Spliterator<T> trySplit() {
            Spliterator<T> split = this.delegate.trySplit();
            return split == null ? null : new WithCharacteristics(split);
         }

         @Override
         public long estimateSize() {
            return this.delegate.estimateSize();
         }

         @Override
         public int characteristics() {
            return this.delegate.characteristics() | extraCharacteristics;
         }

         @Override
         public Comparator<? super T> getComparator() {
            if (this.hasCharacteristics(4)) {
               return comparator;
            } else {
               throw new IllegalStateException();
            }
         }
      }

      return new WithCharacteristics(IntStream.range(0, size).mapToObj(function).spliterator());
   }

   static <F, T> Spliterator<T> map(final Spliterator<F> fromSpliterator, final Function<? super F, ? extends T> function) {
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);
      return new Spliterator<T>() {
         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
            return fromSpliterator.tryAdvance(fromElement -> action.accept((T)function.apply(fromElement)));
         }

         @Override
         public void forEachRemaining(Consumer<? super T> action) {
            fromSpliterator.forEachRemaining(fromElement -> action.accept((T)function.apply(fromElement)));
         }

         @Override
         public Spliterator<T> trySplit() {
            Spliterator<F> fromSplit = fromSpliterator.trySplit();
            return fromSplit != null ? CollectSpliterators.map(fromSplit, function) : null;
         }

         @Override
         public long estimateSize() {
            return fromSpliterator.estimateSize();
         }

         @Override
         public int characteristics() {
            return fromSpliterator.characteristics() & -262;
         }
      };
   }

   static <F, T> Spliterator<T> flatMap(
      Spliterator<F> fromSpliterator, final Function<? super F, Spliterator<T>> function, int topCharacteristics, long topSize
   ) {
      Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
      Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
      Preconditions.checkNotNull(fromSpliterator);
      Preconditions.checkNotNull(function);

      class FlatMapSpliterator implements Spliterator<T> {
         @Nullable
         Spliterator<T> prefix;
         final Spliterator<F> from;
         final int characteristics;
         long estimatedSize;

         FlatMapSpliterator(Spliterator<T> prefix, Spliterator<F> from, int characteristics, long estimatedSize) {
            this.prefix = prefix;
            this.from = from;
            this.characteristics = characteristics;
            this.estimatedSize = estimatedSize;
         }

         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
            while (this.prefix == null || !this.prefix.tryAdvance(action)) {
               this.prefix = null;
               if (!this.from.tryAdvance(fromElement -> this.prefix = function.apply(fromElement))) {
                  return false;
               }
            }

            if (this.estimatedSize != Long.MAX_VALUE) {
               this.estimatedSize--;
            }

            return true;
         }

         @Override
         public void forEachRemaining(Consumer<? super T> action) {
            if (this.prefix != null) {
               this.prefix.forEachRemaining(action);
               this.prefix = null;
            }

            this.from.forEachRemaining(fromElement -> function.apply(fromElement).forEachRemaining(action));
            this.estimatedSize = 0L;
         }

         @Override
         public Spliterator<T> trySplit() {
            Spliterator<F> fromSplit = this.from.trySplit();
            if (fromSplit != null) {
               int splitCharacteristics = this.characteristics & -65;
               long estSplitSize = this.estimateSize();
               if (estSplitSize < Long.MAX_VALUE) {
                  estSplitSize /= 2L;
                  this.estimatedSize -= estSplitSize;
               }

               Spliterator<T> result = new FlatMapSpliterator(this.prefix, fromSplit, splitCharacteristics, estSplitSize);
               this.prefix = null;
               return result;
            } else if (this.prefix != null) {
               Spliterator<T> result = this.prefix;
               this.prefix = null;
               return result;
            } else {
               return null;
            }
         }

         @Override
         public long estimateSize() {
            if (this.prefix != null) {
               this.estimatedSize = Math.max(this.estimatedSize, this.prefix.estimateSize());
            }

            return Math.max(this.estimatedSize, 0L);
         }

         @Override
         public int characteristics() {
            return this.characteristics;
         }
      }

      return new FlatMapSpliterator(null, fromSpliterator, topCharacteristics, topSize);
   }
}
