package com.llamalad7.mixinextras.lib.semver.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Stream<E> implements Iterable<E> {
   private final E[] elements;
   private int offset = 0;

   public Stream(E[] elements) {
      this.elements = (E[])((Object[])elements.clone());
   }

   public E consume() {
      return this.offset >= this.elements.length ? null : this.elements[this.offset++];
   }

   @SafeVarargs
   public final <T extends Stream.ElementType<E>> E consume(T... expected) {
      E lookahead = this.lookahead(1);

      for (Stream.ElementType<E> type : expected) {
         if (type.isMatchedBy(lookahead)) {
            return this.consume();
         }
      }

      throw new UnexpectedElementException(lookahead, this.offset, expected);
   }

   public E lookahead(int position) {
      int idx = this.offset + position - 1;
      return idx < this.elements.length ? this.elements[idx] : null;
   }

   public int currentOffset() {
      return this.offset;
   }

   @SafeVarargs
   public final <T extends Stream.ElementType<E>> boolean positiveLookahead(T... expected) {
      for (Stream.ElementType<E> type : expected) {
         if (type.isMatchedBy(this.lookahead(1))) {
            return true;
         }
      }

      return false;
   }

   @SafeVarargs
   public final <T extends Stream.ElementType<E>> boolean positiveLookaheadBefore(Stream.ElementType<E> before, T... expected) {
      for (int i = 1; i <= this.elements.length; i++) {
         E lookahead = this.lookahead(i);
         if (before.isMatchedBy(lookahead)) {
            break;
         }

         for (Stream.ElementType<E> type : expected) {
            if (type.isMatchedBy(lookahead)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Iterator<E> iterator() {
      return new Iterator<E>() {
         private int index = Stream.this.offset;

         @Override
         public boolean hasNext() {
            return this.index < Stream.this.elements.length;
         }

         @Override
         public E next() {
            if (this.index >= Stream.this.elements.length) {
               throw new NoSuchElementException();
            } else {
               return Stream.this.elements[this.index++];
            }
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   public interface ElementType<E> {
      boolean isMatchedBy(E var1);
   }
}
