package io.github.legacymoddingmc.unimixins.compat.asm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BytePatternMatcher {
   final BytePatternMatcher.Mode mode;
   final byte[][][] byFirst = new byte[256][][];
   int minPatternLen = Integer.MAX_VALUE;

   public BytePatternMatcher(String strPattern, BytePatternMatcher.Mode mode) {
      this(new String[]{strPattern}, mode);
   }

   public BytePatternMatcher(String[] strPatterns, BytePatternMatcher.Mode mode) {
      this.mode = mode;
      byte[][] patterns = new byte[strPatterns.length][];
      int[] bucketSizes = new int[256];
      int[] bucketIndices = new int[Math.min(strPatterns.length, 256)];
      int bucketsCount = 0;

      for (int i = 0; i < strPatterns.length; i++) {
         byte[] pattern = strPatterns[i].getBytes(StandardCharsets.UTF_8);
         patterns[i] = pattern;
         if (pattern.length < this.minPatternLen) {
            this.minPatternLen = pattern.length;
         }

         int bucketIndex = pattern[0] & 255;
         if (bucketSizes[bucketIndex]++ == 0) {
            bucketIndices[bucketsCount++] = bucketIndex;
         }
      }

      Arrays.sort(patterns, (a, b) -> Integer.compare(a.length, b.length));

      for (int i = 0; i < bucketsCount; i++) {
         int bucketIndex = bucketIndices[i];
         this.byFirst[bucketIndex] = new byte[bucketSizes[bucketIndex]][];
         bucketSizes[bucketIndex] = 0;
      }

      for (byte[] patternx : patterns) {
         int bucketIndex = patternx[0] & 255;
         this.byFirst[bucketIndex][bucketSizes[bucketIndex]++] = patternx;
      }
   }

   public boolean matches(byte[] bytes, int start, int len) {
      if (len < this.minPatternLen) {
         return false;
      } else {
         switch (this.mode) {
            case Contains:
               return this.contains(bytes, start, len);
            case Equals:
               return this.equals(bytes, start, len);
            case StartsWith:
               return this.startsWith(bytes, start, len);
            default:
               return false;
         }
      }
   }

   private boolean contains(byte[] bytes, int start, int len) {
      int end = start + len;

      for (int pos = start; pos <= end - this.minPatternLen; pos++) {
         byte[][] patterns = this.byFirst[bytes[pos] & 255];
         if (patterns != null) {
            for (byte[] pattern : patterns) {
               if (pattern.length > end - pos) {
                  break;
               }

               int k = pattern.length - 1;

               while (k > 0 && bytes[pos + k] == pattern[k]) {
                  k--;
               }

               if (k == 0) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean equals(byte[] bytes, int start, int len) {
      byte[][] patterns = this.byFirst[bytes[start] & 255];
      if (patterns == null) {
         return false;
      } else {
         for (byte[] pattern : patterns) {
            if (pattern.length >= len) {
               if (pattern.length > len) {
                  break;
               }

               int k = pattern.length - 1;

               while (k > 0 && bytes[start + k] == pattern[k]) {
                  k--;
               }

               if (k == 0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean startsWith(byte[] bytes, int start, int len) {
      byte[][] patterns = this.byFirst[bytes[start] & 255];
      if (patterns == null) {
         return false;
      } else {
         for (byte[] pattern : patterns) {
            if (pattern.length > len) {
               break;
            }

            int k = pattern.length - 1;

            while (k > 0 && bytes[start + k] == pattern[k]) {
               k--;
            }

            if (k == 0) {
               return true;
            }
         }

         return false;
      }
   }

   public static enum Mode {
      Contains,
      Equals,
      StartsWith;
   }
}
