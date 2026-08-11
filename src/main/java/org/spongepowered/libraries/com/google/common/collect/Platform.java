package org.spongepowered.libraries.com.google.common.collect;

import java.lang.reflect.Array;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
final class Platform {
   static <T> T[] newArray(T[] reference, int length) {
      Class<?> type = reference.getClass().getComponentType();
      return (T[])((Object[])Array.newInstance(type, length));
   }

   static MapMaker tryWeakKeys(MapMaker mapMaker) {
      return mapMaker.weakKeys();
   }

   private Platform() {
   }
}
