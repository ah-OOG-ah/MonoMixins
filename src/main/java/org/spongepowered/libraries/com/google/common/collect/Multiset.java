package org.spongepowered.libraries.com.google.common.collect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.CompatibleWith;

@GwtCompatible
public interface Multiset<E> extends Collection<E> {
   @Override
   int size();

   int count(@Nullable @CompatibleWith("E") Object var1);

   @CanIgnoreReturnValue
   int add(@Nullable E var1, int var2);

   @CanIgnoreReturnValue
   int remove(@Nullable @CompatibleWith("E") Object var1, int var2);

   @CanIgnoreReturnValue
   int setCount(E var1, int var2);

   @CanIgnoreReturnValue
   boolean setCount(E var1, int var2, int var3);

   Set<E> elementSet();

   Set<Multiset.Entry<E>> entrySet();

   @Beta
   default void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> action.accept(entry.getElement(), entry.getCount()));
   }

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();

   @Override
   String toString();

   @Override
   Iterator<E> iterator();

   @Override
   boolean contains(@Nullable Object var1);

   @Override
   boolean containsAll(Collection<?> var1);

   @CanIgnoreReturnValue
   @Override
   boolean add(E var1);

   @CanIgnoreReturnValue
   @Override
   boolean remove(@Nullable Object var1);

   @CanIgnoreReturnValue
   @Override
   boolean removeAll(Collection<?> var1);

   @CanIgnoreReturnValue
   @Override
   boolean retainAll(Collection<?> var1);

   @Override
   default void forEach(Consumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> {
         E elem = entry.getElement();
         int count = entry.getCount();

         for (int i = 0; i < count; i++) {
            action.accept(elem);
         }
      });
   }

   @Override
   default Spliterator<E> spliterator() {
      return Multisets.spliteratorImpl(this);
   }

   public interface Entry<E> {
      E getElement();

      int getCount();

      @Override
      boolean equals(Object var1);

      @Override
      int hashCode();

      @Override
      String toString();
   }
}
