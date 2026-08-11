package org.spongepowered.include.com.google.common.collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public abstract class ImmutableList<E> extends ImmutableCollection<E> implements List<E>, RandomAccess {
   public static <E> ImmutableList<E> of() {
      return (ImmutableList<E>)RegularImmutableList.EMPTY;
   }

   public static <E> ImmutableList<E> of(E element) {
      return new SingletonImmutableList<>(element);
   }

   public static <E> ImmutableList<E> of(E e1, E e2) {
      return construct(e1, e2);
   }

   public static <E> ImmutableList<E> of(E e1, E e2, E e3) {
      return construct(e1, e2, e3);
   }

   public static <E> ImmutableList<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
      return construct(e1, e2, e3, e4, e5, e6, e7);
   }

   private static <E> ImmutableList<E> construct(Object... elements) {
      return asImmutableList(ObjectArrays.checkElementsNotNull(elements));
   }

   static <E> ImmutableList<E> asImmutableList(Object[] elements) {
      return asImmutableList(elements, elements.length);
   }

   static <E> ImmutableList<E> asImmutableList(Object[] elements, int length) {
      switch (length) {
         case 0:
            return of();
         case 1:
            ImmutableList<E> list = new SingletonImmutableList<>((E)elements[0]);
            return list;
         default:
            if (length < elements.length) {
               elements = Arrays.copyOf(elements, length);
            }

            return new RegularImmutableList<>(elements);
      }
   }

   ImmutableList() {
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return this.listIterator();
   }

   public UnmodifiableListIterator<E> listIterator() {
      return this.listIterator(0);
   }

   public UnmodifiableListIterator<E> listIterator(int index) {
      return new AbstractIndexedListIterator<E>(this.size(), index) {
         @Override
         protected E get(int index) {
            return (E)ImmutableList.this.get(index);
         }
      };
   }

   @Override
   public void forEach(Consumer<? super E> consumer) {
      Preconditions.checkNotNull(consumer);
      int n = this.size();

      for (int i = 0; i < n; i++) {
         consumer.accept(this.get(i));
      }
   }

   @Override
   public int indexOf(@Nullable Object object) {
      return object == null ? -1 : Lists.indexOfImpl(this, object);
   }

   @Override
   public int lastIndexOf(@Nullable Object object) {
      return object == null ? -1 : Lists.lastIndexOfImpl(this, object);
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.indexOf(object) >= 0;
   }

   public ImmutableList<E> subList(int fromIndex, int toIndex) {
      Preconditions.checkPositionIndexes(fromIndex, toIndex, this.size());
      int length = toIndex - fromIndex;
      if (length == this.size()) {
         return this;
      } else {
         switch (length) {
            case 0:
               return of();
            case 1:
               return of(this.get(fromIndex));
            default:
               return this.subListUnchecked(fromIndex, toIndex);
         }
      }
   }

   ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
      return new ImmutableList.SubList(fromIndex, toIndex - fromIndex);
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean addAll(int index, Collection<? extends E> newElements) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final E set(int index, E element) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void add(int index, E element) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final E remove(int index) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void replaceAll(UnaryOperator<E> operator) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void sort(Comparator<? super E> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public final ImmutableList<E> asList() {
      return this;
   }

   @Override
   public Spliterator<E> spliterator() {
      return CollectSpliterators.indexed(this.size(), 1296, this::get);
   }

   @Override
   int copyIntoArray(Object[] dst, int offset) {
      int size = this.size();

      for (int i = 0; i < size; i++) {
         dst[offset + i] = this.get(i);
      }

      return offset + size;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      return Lists.equalsImpl(this, obj);
   }

   @Override
   public int hashCode() {
      int hashCode = 1;
      int n = this.size();

      for (int i = 0; i < n; i++) {
         hashCode = 31 * hashCode + this.get(i).hashCode();
         hashCode = ~(~hashCode);
      }

      return hashCode;
   }

   public static <E> ImmutableList.Builder<E> builder() {
      return new ImmutableList.Builder<>();
   }

   public static final class Builder<E> extends ImmutableCollection.ArrayBasedBuilder<E> {
      public Builder() {
         this(4);
      }

      Builder(int capacity) {
         super(capacity);
      }

      @CanIgnoreReturnValue
      public ImmutableList.Builder<E> add(E element) {
         super.add(element);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableList.Builder<E> addAll(Iterable<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableList.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      public ImmutableList<E> build() {
         return ImmutableList.asImmutableList(this.contents, this.size);
      }
   }

   class SubList extends ImmutableList<E> {
      final transient int offset;
      final transient int length;

      SubList(int offset, int length) {
         this.offset = offset;
         this.length = length;
      }

      @Override
      public int size() {
         return this.length;
      }

      @Override
      public E get(int index) {
         Preconditions.checkElementIndex(index, this.length);
         return ImmutableList.this.get(index + this.offset);
      }

      @Override
      public ImmutableList<E> subList(int fromIndex, int toIndex) {
         Preconditions.checkPositionIndexes(fromIndex, toIndex, this.length);
         return ImmutableList.this.subList(fromIndex + this.offset, toIndex + this.offset);
      }
   }
}
