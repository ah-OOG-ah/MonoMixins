package com.llamalad7.mixinextras.lib.antlr.runtime.misc;

import java.util.Iterator;

public class Utils {
   public static <T> String join(Iterator<T> iter, String separator) {
      StringBuilder buf = new StringBuilder();

      while (iter.hasNext()) {
         buf.append(iter.next());
         if (iter.hasNext()) {
            buf.append(separator);
         }
      }

      return buf.toString();
   }

   public static String escapeWhitespace(String s, boolean escapeSpaces) {
      StringBuilder buf = new StringBuilder();

      for (char c : s.toCharArray()) {
         if (c == ' ' && escapeSpaces) {
            buf.append('·');
         } else if (c == '\t') {
            buf.append("\\t");
         } else if (c == '\n') {
            buf.append("\\n");
         } else if (c == '\r') {
            buf.append("\\r");
         } else {
            buf.append(c);
         }
      }

      return buf.toString();
   }
}
