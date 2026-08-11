package com.llamalad7.mixinextras.lib.semver.util;

import java.util.Arrays;

public class UnexpectedElementException extends RuntimeException {
   private final Object unexpected;
   private final int position;
   private final Stream.ElementType<?>[] expected;

   UnexpectedElementException(Object unexpected, int position, Stream.ElementType<?>... expected) {
      super(createMessage(unexpected, position, expected));
      this.unexpected = unexpected;
      this.position = position;
      this.expected = expected;
   }

   public Object getUnexpectedElement() {
      return this.unexpected;
   }

   public int getPosition() {
      return this.position;
   }

   public Stream.ElementType<?>[] getExpectedElementTypes() {
      return this.expected;
   }

   @Override
   public String toString() {
      return this.getMessage();
   }

   private static String createMessage(Object unexpected, int position, Stream.ElementType<?>... expected) {
      String msg = String.format("Unexpected element '%s' at position %d", unexpected, position);
      if (expected.length > 0) {
         msg = msg + String.format(", expecting %s", Arrays.toString((Object[])expected));
      }

      return msg;
   }
}
