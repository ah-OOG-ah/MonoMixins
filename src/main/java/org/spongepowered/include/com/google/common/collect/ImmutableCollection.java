package org.spongepowered.include.com.google.common.collect;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {
   ImmutableCollection() {
   }

   public abstract UnmodifiableIterator<E> iterator();

   @Override
   public Spliterator<E> spliterator() {
      return Spliterators.spliterator(this, 1296);
   }

   @Override
   public final Object[] toArray() {
      int size = this.size();
      if (size == 0) {
         return ObjectArrays.EMPTY_ARRAY;
      } else {
         Object[] result = new Object[size];
         this.copyIntoArray(result, 0);
         return result;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public final <T> T[] toArray(T[] other) {
      Preconditions.checkNotNull((T)other);
      int size = this.size();
      if (other.length < size) {
         other = (T[])ObjectArrays.newArray(other, size);
      } else if (other.length > size) {
         other[size] = null;
      }

      this.copyIntoArray(other, 0);
      return other;
   }

   @Override
   public abstract boolean contains(@Nullable Object var1);

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean add(E e) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean remove(Object object) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean addAll(Collection<? extends E> newElements) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean removeAll(Collection<?> oldElements) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean removeIf(Predicate<? super E> filter) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final boolean retainAll(Collection<?> elementsToKeep) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   public ImmutableList<E> asList() {
      switch (this.size()) {
         case 0:
            return ImmutableList.of();
         case 1:
            return ImmutableList.of(this.iterator().next());
         default:
            return new RegularImmutableAsList<>(this, this.toArray());
      }
   }

   @CanIgnoreReturnValue
   int copyIntoArray(Object[] dst, int offset) {
      for (E e : this) {
         dst[offset++] = e;
      }

      return offset;
   }

   abstract static class ArrayBasedBuilder<E> extends ImmutableCollection.Builder<E> {
      Object[] contents;
      int size;

      ArrayBasedBuilder(int initialCapacity) {
         CollectPreconditions.checkNonnegative(initialCapacity, "initialCapacity");
         this.contents = new Object[initialCapacity];
         this.size = 0;
      }

      private void ensureCapacity(int minCapacity) {
         if (this.contents.length < minCapacity) {
            this.contents = Arrays.copyOf(this.contents, expandedCapacity(this.contents.length, minCapacity));
         }
      }

      @CanIgnoreReturnValue
      public ImmutableCollection.ArrayBasedBuilder<E> add(E element) {
         Preconditions.checkNotNull(element);
         this.ensureCapacity(this.size + 1);
         this.contents[this.size++] = element;
         return this;
      }

      @CanIgnoreReturnValue
      @Override
      public ImmutableCollection.Builder<E> add(E... elements) {
         ObjectArrays.checkElementsNotNull(elements);
         this.ensureCapacity(this.size + elements.length);
         System.arraycopy(elements, 0, this.contents, this.size, elements.length);
         this.size += elements.length;
         return this;
      }

      @CanIgnoreReturnValue
      @Override
      public ImmutableCollection.Builder<E> addAll(Iterable<? extends E> elements) {
         if (elements instanceof Collection) {
            Collection<?> collection = (Collection<?>)elements;
            this.ensureCapacity(this.size + collection.size());
         }

         super.addAll(elements);
         return this;
      }
   }

   public abstract static class Builder<E> {
      static int expandedCapacity(int oldCapacity, int minCapacity) {
         if (minCapacity < 0) {
            throw new AssertionError("cannot store more than MAX_VALUE elements");
         } else {
            int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
            if (newCapacity < minCapacity) {
               newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
            }

            if (newCapacity < 0) {
               newCapacity = Integer.MAX_VALUE;
            }

            return newCapacity;
         }
      }

      Builder() {
      }

      @CanIgnoreReturnValue
      public abstract ImmutableCollection.Builder<E> add(E var1);

      @CanIgnoreReturnValue
      public ImmutableCollection.Builder<E> add(E... elements) {
         for (E element : elements) {
            this.add(element);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableCollection.Builder<E> addAll(Iterable<? extends E> elements) {
         for (E element : elements) {
            this.add(element);
         }

         return this;
      }
   }
}
