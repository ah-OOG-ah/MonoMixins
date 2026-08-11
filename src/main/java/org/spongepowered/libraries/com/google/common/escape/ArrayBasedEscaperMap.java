package org.spongepowered.libraries.com.google.common.escape;

import java.util.Collections;
import java.util.Map;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
@GwtCompatible
public final class ArrayBasedEscaperMap {
   private final char[][] replacementArray;
   private static final char[][] EMPTY_REPLACEMENT_ARRAY = new char[0][0];

   public static ArrayBasedEscaperMap create(Map<Character, String> replacements) {
      return new ArrayBasedEscaperMap(createReplacementArray(replacements));
   }

   private ArrayBasedEscaperMap(char[][] replacementArray) {
      this.replacementArray = replacementArray;
   }

   char[][] getReplacementArray() {
      return this.replacementArray;
   }

   @VisibleForTesting
   static char[][] createReplacementArray(Map<Character, String> map) {
      Preconditions.checkNotNull(map);
      if (map.isEmpty()) {
         return EMPTY_REPLACEMENT_ARRAY;
      } else {
         char max = Collections.max(map.keySet());
         char[][] replacements = new char[max + 1][];

         for (char c : map.keySet()) {
            replacements[c] = map.get(c).toCharArray();
         }

         return replacements;
      }
   }
}
