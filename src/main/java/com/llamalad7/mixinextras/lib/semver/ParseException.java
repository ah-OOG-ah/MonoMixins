package com.llamalad7.mixinextras.lib.semver;

public class ParseException extends RuntimeException {
   public ParseException() {
   }

   public ParseException(String message) {
      super(message);
   }

   public ParseException(String message, UnexpectedCharacterException cause) {
      super(message);
      this.initCause(cause);
   }

   @Override
   public String toString() {
      Throwable cause = this.getCause();
      String msg = this.getMessage();
      if (msg != null) {
         return msg + (cause != null ? " (" + cause + ")" : "");
      } else {
         return cause != null ? cause.toString() : "";
      }
   }
}
