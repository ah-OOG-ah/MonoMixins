package org.spongepowered.libraries.com.google.common.collect;

import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;

@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableSet<E> extends ImmutableSet.Indexed<E> {
   static final RegularImmutableSet<Object> EMPTY = new RegularImmutableSet<>(ObjectArrays.EMPTY_ARRAY, 0, null, 0);
   private final transient Object[] elements;
   @VisibleForTesting
   final transient Object[] table;
   private final transient int mask;
   private final transient int hashCode;

   RegularImmutableSet(Object[] elements, int hashCode, Object[] table, int mask) {
      this.elements = elements;
      this.table = table;
      this.mask = mask;
      this.hashCode = hashCode;
   }

   @Override
   public boolean contains(@Nullable Object target) {
      Object[] table = this.table;
      if (target != null && table != null) {
         int i = Hashing.smearedHash(target);

         while (true) {
            i &= this.mask;
            Object candidate = table[i];
            if (candidate == null) {
               return false;
            }

            if (candidate.equals(target)) {
               return true;
            }

            i++;
         }
      } else {
         return false;
      }
   }

   @Override
   public int size() {
      return this.elements.length;
   }

   @Override
   E get(int i) {
      return (E)this.elements[i];
   }

   @Override
   public Spliterator<E> spliterator() {
      return Spliterators.spliterator(this.elements, 1297);
   }

   @Override
   int copyIntoArray(Object[] dst, int offset) {
      System.arraycopy(this.elements, 0, dst, offset, this.elements.length);
      return offset + this.elements.length;
   }

   @Override
   ImmutableList<E> createAsList() {
      return (ImmutableList<E>)(this.table == null ? ImmutableList.of() : new RegularImmutableAsList<>(this, this.elements));
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   boolean isHashCodeFast() {
      return true;
   }
}
