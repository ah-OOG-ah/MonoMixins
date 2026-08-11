package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableMultiset<E> extends ImmutableCollection<E> implements Multiset<E> {
   @LazyInit
   private transient ImmutableList<E> asList;
   @LazyInit
   private transient ImmutableSet<Multiset.Entry<E>> entrySet;

   @Beta
   public static <E> Collector<E, ?, ImmutableMultiset<E>> toImmutableMultiset() {
      return toImmutableMultiset(Function.identity(), e -> 1);
   }

   private static <T, E> Collector<T, ?, ImmutableMultiset<E>> toImmutableMultiset(
      Function<? super T, ? extends E> elementFunction, ToIntFunction<? super T> countFunction
   ) {
      Preconditions.checkNotNull(elementFunction);
      Preconditions.checkNotNull(countFunction);
      return Collector.of(
         LinkedHashMultiset::create, (multiset, t) -> multiset.add((E)elementFunction.apply(t), countFunction.applyAsInt(t)), (multiset1, multiset2) -> {
            multiset1.addAll(multiset2);
            return multiset1;
         }, multiset -> copyFromEntries(multiset.entrySet())
      );
   }

   public static <E> ImmutableMultiset<E> of() {
      return (ImmutableMultiset<E>)RegularImmutableMultiset.EMPTY;
   }

   public static <E> ImmutableMultiset<E> of(E element) {
      return copyFromElements(element);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2) {
      return copyFromElements(e1, e2);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3) {
      return copyFromElements(e1, e2, e3);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4) {
      return copyFromElements(e1, e2, e3, e4);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4, E e5) {
      return copyFromElements(e1, e2, e3, e4, e5);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
      return new ImmutableMultiset.Builder<E>().add(e1).add(e2).add(e3).add(e4).add(e5).add(e6).add(others).build();
   }

   public static <E> ImmutableMultiset<E> copyOf(E[] elements) {
      return copyFromElements(elements);
   }

   public static <E> ImmutableMultiset<E> copyOf(Iterable<? extends E> elements) {
      if (elements instanceof ImmutableMultiset) {
         ImmutableMultiset<E> result = (ImmutableMultiset<E>)elements;
         if (!result.isPartialView()) {
            return result;
         }
      }

      Multiset<? extends E> multiset = (Multiset<? extends E>)(elements instanceof Multiset ? Multisets.cast(elements) : LinkedHashMultiset.create(elements));
      return copyFromEntries(multiset.entrySet());
   }

   private static <E> ImmutableMultiset<E> copyFromElements(E... elements) {
      Multiset<E> multiset = LinkedHashMultiset.create();
      Collections.addAll(multiset, elements);
      return copyFromEntries(multiset.entrySet());
   }

   static <E> ImmutableMultiset<E> copyFromEntries(Collection<? extends Multiset.Entry<? extends E>> entries) {
      return (ImmutableMultiset<E>)(entries.isEmpty() ? of() : new RegularImmutableMultiset<>(entries));
   }

   public static <E> ImmutableMultiset<E> copyOf(Iterator<? extends E> elements) {
      Multiset<E> multiset = LinkedHashMultiset.create();
      Iterators.addAll(multiset, elements);
      return copyFromEntries(multiset.entrySet());
   }

   ImmutableMultiset() {
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      final Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      return new UnmodifiableIterator<E>() {
         int remaining;
         E element;

         @Override
         public boolean hasNext() {
            return this.remaining > 0 || entryIterator.hasNext();
         }

         @Override
         public E next() {
            if (this.remaining <= 0) {
               Multiset.Entry<E> entry = entryIterator.next();
               this.element = entry.getElement();
               this.remaining = entry.getCount();
            }

            this.remaining--;
            return this.element;
         }
      };
   }

   @Override
   public ImmutableList<E> asList() {
      ImmutableList<E> result = this.asList;
      return result == null ? (this.asList = this.createAsList()) : result;
   }

   ImmutableList<E> createAsList() {
      return (ImmutableList<E>)(this.isEmpty() ? ImmutableList.of() : new RegularImmutableAsList<>(this, this.toArray()));
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.count(object) > 0;
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final int add(E element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final int remove(Object element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final int setCount(E element, int count) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final boolean setCount(E element, int oldCount, int newCount) {
      throw new UnsupportedOperationException();
   }

   @GwtIncompatible
   @Override
   int copyIntoArray(Object[] dst, int offset) {
      for (Multiset.Entry<E> entry : this.entrySet()) {
         Arrays.fill(dst, offset, offset + entry.getCount(), entry.getElement());
         offset += entry.getCount();
      }

      return offset;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return Multisets.equalsImpl(this, object);
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this.entrySet());
   }

   @Override
   public String toString() {
      return this.entrySet().toString();
   }

   public abstract ImmutableSet<E> elementSet();

   public ImmutableSet<Multiset.Entry<E>> entrySet() {
      ImmutableSet<Multiset.Entry<E>> es = this.entrySet;
      return es == null ? (this.entrySet = this.createEntrySet()) : es;
   }

   private final ImmutableSet<Multiset.Entry<E>> createEntrySet() {
      return (ImmutableSet<Multiset.Entry<E>>)(this.isEmpty() ? ImmutableSet.of() : new ImmutableMultiset.EntrySet());
   }

   abstract Multiset.Entry<E> getEntry(int var1);

   @Override
   Object writeReplace() {
      return new ImmutableMultiset.SerializedForm(this);
   }

   public static <E> ImmutableMultiset.Builder<E> builder() {
      return new ImmutableMultiset.Builder<>();
   }

   public static class Builder<E> extends ImmutableCollection.Builder<E> {
      final Multiset<E> contents;

      public Builder() {
         this(LinkedHashMultiset.create());
      }

      Builder(Multiset<E> contents) {
         this.contents = contents;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> add(E element) {
         this.contents.add(Preconditions.checkNotNull(element));
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addCopies(E element, int occurrences) {
         this.contents.add(Preconditions.checkNotNull(element), occurrences);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> setCount(E element, int count) {
         this.contents.setCount(Preconditions.checkNotNull(element), count);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addAll(Iterable<? extends E> elements) {
         if (elements instanceof Multiset) {
            Multiset<? extends E> multiset = Multisets.cast(elements);

            for (Multiset.Entry<? extends E> entry : multiset.entrySet()) {
               this.addCopies((E)entry.getElement(), entry.getCount());
            }
         } else {
            super.addAll(elements);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addAll(Iterator<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      public ImmutableMultiset<E> build() {
         return ImmutableMultiset.copyOf(this.contents);
      }
   }

   private final class EntrySet extends ImmutableSet.Indexed<Multiset.Entry<E>> {
      private static final long serialVersionUID = 0L;

      private EntrySet() {
      }

      @Override
      boolean isPartialView() {
         return ImmutableMultiset.this.isPartialView();
      }

      Multiset.Entry<E> get(int index) {
         return ImmutableMultiset.this.getEntry(index);
      }

      @Override
      public int size() {
         return ImmutableMultiset.this.elementSet().size();
      }

      @Override
      public boolean contains(Object o) {
         if (o instanceof Multiset.Entry) {
            Multiset.Entry<?> entry = (Multiset.Entry<?>)o;
            if (entry.getCount() <= 0) {
               return false;
            } else {
               int count = ImmutableMultiset.this.count(entry.getElement());
               return count == entry.getCount();
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return ImmutableMultiset.this.hashCode();
      }

      @Override
      Object writeReplace() {
         return new ImmutableMultiset.EntrySetSerializedForm<>(ImmutableMultiset.this);
      }
   }

   static class EntrySetSerializedForm<E> implements Serializable {
      final ImmutableMultiset<E> multiset;

      EntrySetSerializedForm(ImmutableMultiset<E> multiset) {
         this.multiset = multiset;
      }

      Object readResolve() {
         return this.multiset.entrySet();
      }
   }

   private static class SerializedForm implements Serializable {
      final Object[] elements;
      final int[] counts;
      private static final long serialVersionUID = 0L;

      SerializedForm(Multiset<?> multiset) {
         int distinct = multiset.entrySet().size();
         this.elements = new Object[distinct];
         this.counts = new int[distinct];
         int i = 0;

         for (Multiset.Entry<?> entry : multiset.entrySet()) {
            this.elements[i] = entry.getElement();
            this.counts[i] = entry.getCount();
            i++;
         }
      }

      Object readResolve() {
         LinkedHashMultiset<Object> multiset = LinkedHashMultiset.create(this.elements.length);

         for (int i = 0; i < this.elements.length; i++) {
            multiset.add(this.elements[i], this.counts[i]);
         }

         return ImmutableMultiset.copyOf(multiset);
      }
   }
}
