package org.spongepowered.libraries.com.google.common.escape;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Function;

@GwtCompatible
public abstract class Escaper {
   private final Function<String, String> asFunction = new Function<String, String>() {
      public String apply(String from) {
         return Escaper.this.escape(from);
      }
   };

   protected Escaper() {
   }

   public abstract String escape(String var1);

   public final Function<String, String> asFunction() {
      return this.asFunction;
   }
}
