package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collector;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;
import org.spongepowered.libraries.com.google.j2objc.annotations.RetainedWith;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
   static final int SPLITERATOR_CHARACTERISTICS = 1297;
   static final int MAX_TABLE_SIZE = 1073741824;
   private static final double DESIRED_LOAD_FACTOR = 0.7;
   private static final int CUTOFF = 751619276;
   @LazyInit
   @RetainedWith
   private transient ImmutableList<E> asList;

   @Beta
   public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
      return CollectCollectors.toImmutableSet();
   }

   public static <E> ImmutableSet<E> of() {
      return (ImmutableSet<E>)RegularImmutableSet.EMPTY;
   }

   public static <E> ImmutableSet<E> of(E element) {
      return new SingletonImmutableSet<>(element);
   }

   public static <E> ImmutableSet<E> of(E e1, E e2) {
      return construct(2, e1, e2);
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3) {
      return construct(3, e1, e2, e3);
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4) {
      return construct(4, e1, e2, e3, e4);
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5) {
      return construct(5, e1, e2, e3, e4, e5);
   }

   @SafeVarargs
   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
      int paramCount = 6;
      Object[] elements = new Object[6 + others.length];
      elements[0] = e1;
      elements[1] = e2;
      elements[2] = e3;
      elements[3] = e4;
      elements[4] = e5;
      elements[5] = e6;
      System.arraycopy(others, 0, elements, 6, others.length);
      return construct(elements.length, elements);
   }

   private static <E> ImmutableSet<E> construct(int n, Object... elements) {
      switch (n) {
         case 0:
            return of();
         case 1:
            E elem = (E)elements[0];
            return of(elem);
         default:
            int tableSize = chooseTableSize(n);
            Object[] table = new Object[tableSize];
            int mask = tableSize - 1;
            int hashCode = 0;
            int uniques = 0;
            int i = 0;

            for (; i < n; i++) {
               Object element = ObjectArrays.checkElementNotNull(elements[i], i);
               int hash = element.hashCode();
               int j = Hashing.smear(hash);

               while (true) {
                  int index = j & mask;
                  Object value = table[index];
                  if (value == null) {
                     elements[uniques++] = element;
                     table[index] = element;
                     hashCode += hash;
                     break;
                  }

                  if (value.equals(element)) {
                     break;
                  }

                  j++;
               }
            }

            Arrays.fill(elements, uniques, n, null);
            if (uniques == 1) {
               E element = (E)elements[0];
               return new SingletonImmutableSet<>(element, hashCode);
            } else if (tableSize != chooseTableSize(uniques)) {
               return construct(uniques, elements);
            } else {
               Object[] uniqueElements = uniques < elements.length ? Arrays.copyOf(elements, uniques) : elements;
               return new RegularImmutableSet<>(uniqueElements, hashCode, table, mask);
            }
      }
   }

   @VisibleForTesting
   static int chooseTableSize(int setSize) {
      if (setSize >= 751619276) {
         Preconditions.checkArgument(setSize < 1073741824, "collection too large");
         return 1073741824;
      } else {
         int tableSize = Integer.highestOneBit(setSize - 1) << 1;

         while (tableSize * 0.7 < setSize) {
            tableSize <<= 1;
         }

         return tableSize;
      }
   }

   public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
      if (elements instanceof ImmutableSet && !(elements instanceof ImmutableSortedSet)) {
         ImmutableSet<E> set = (ImmutableSet<E>)elements;
         if (!set.isPartialView()) {
            return set;
         }
      } else if (elements instanceof EnumSet) {
         return copyOfEnumSet((EnumSet)elements);
      }

      Object[] array = elements.toArray();
      return construct(array.length, array);
   }

   public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
      return elements instanceof Collection ? copyOf((Collection<? extends E>)elements) : copyOf(elements.iterator());
   }

   public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
      if (!elements.hasNext()) {
         return of();
      } else {
         E first = (E)elements.next();
         return !elements.hasNext() ? of(first) : new ImmutableSet.Builder<E>().add(first).addAll(elements).build();
      }
   }

   public static <E> ImmutableSet<E> copyOf(E[] elements) {
      switch (elements.length) {
         case 0:
            return of();
         case 1:
            return of(elements[0]);
         default:
            return construct(elements.length, (Object[])elements.clone());
      }
   }

   private static ImmutableSet copyOfEnumSet(EnumSet enumSet) {
      return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
   }

   ImmutableSet() {
   }

   boolean isHashCodeFast() {
      return false;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else {
         return object instanceof ImmutableSet && this.isHashCodeFast() && ((ImmutableSet)object).isHashCodeFast() && this.hashCode() != object.hashCode()
            ? false
            : Sets.equalsImpl(this, object);
      }
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this);
   }

   @Override
   public abstract UnmodifiableIterator<E> iterator();

   @Override
   public ImmutableList<E> asList() {
      ImmutableList<E> result = this.asList;
      return result == null ? (this.asList = this.createAsList()) : result;
   }

   ImmutableList<E> createAsList() {
      return new RegularImmutableAsList<>(this, this.toArray());
   }

   @Override
   Object writeReplace() {
      return new ImmutableSet.SerializedForm(this.toArray());
   }

   public static <E> ImmutableSet.Builder<E> builder() {
      return new ImmutableSet.Builder<>();
   }

   public static class Builder<E> extends ImmutableCollection.ArrayBasedBuilder<E> {
      public Builder() {
         this(4);
      }

      Builder(int capacity) {
         super(capacity);
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> add(E element) {
         super.add(element);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> addAll(Iterable<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> addAll(Iterator<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableSet.Builder<E> combine(ImmutableCollection.ArrayBasedBuilder<E> builder) {
         super.combine(builder);
         return this;
      }

      public ImmutableSet<E> build() {
         ImmutableSet<E> result = ImmutableSet.construct(this.size, this.contents);
         this.size = result.size();
         return result;
      }
   }

   abstract static class Indexed<E> extends ImmutableSet<E> {
      abstract E get(int var1);

      @Override
      public UnmodifiableIterator<E> iterator() {
         return this.asList().iterator();
      }

      @Override
      public Spliterator<E> spliterator() {
         return CollectSpliterators.indexed(this.size(), 1297, this::get);
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
      ImmutableList<E> createAsList() {
         return new ImmutableAsList<E>() {
            @Override
            public E get(int index) {
               return (E)Indexed.this.get(index);
            }

            ImmutableSet.Indexed<E> delegateCollection() {
               return Indexed.this;
            }
         };
      }
   }

   private static class SerializedForm implements Serializable {
      final Object[] elements;
      private static final long serialVersionUID = 0L;

      SerializedForm(Object[] elements) {
         this.elements = elements;
      }

      Object readResolve() {
         return ImmutableSet.copyOf(this.elements);
      }
   }
}
