package org.spongepowered.libraries.com.google.thirdparty.publicsuffix;

import java.util.List;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Joiner;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.Lists;

@GwtCompatible
final class TrieParser {
   private static final Joiner PREFIX_JOINER = Joiner.on("");

   static ImmutableMap<String, PublicSuffixType> parseTrie(CharSequence encoded) {
      ImmutableMap.Builder<String, PublicSuffixType> builder = ImmutableMap.builder();
      int encodedLen = encoded.length();
      int idx = 0;

      while (idx < encodedLen) {
         idx += doParseTrieToBuilder(Lists.newLinkedList(), encoded.subSequence(idx, encodedLen), builder);
      }

      return builder.build();
   }

   private static int doParseTrieToBuilder(List<CharSequence> stack, CharSequence encoded, ImmutableMap.Builder<String, PublicSuffixType> builder) {
      int encodedLen = encoded.length();
      int idx = 0;

      char c;
      for (c = 0; idx < encodedLen; idx++) {
         c = encoded.charAt(idx);
         if (c == '&' || c == '?' || c == '!' || c == ':' || c == ',') {
            break;
         }
      }

      stack.add(0, reverse(encoded.subSequence(0, idx)));
      if (c == '!' || c == '?' || c == ':' || c == ',') {
         String domain = PREFIX_JOINER.join(stack);
         if (domain.length() > 0) {
            builder.put(domain, PublicSuffixType.fromCode(c));
         }
      }

      idx++;
      if (c != '?' && c != ',') {
         while (idx < encodedLen) {
            idx += doParseTrieToBuilder(stack, encoded.subSequence(idx, encodedLen), builder);
            if (encoded.charAt(idx) == '?' || encoded.charAt(idx) == ',') {
               idx++;
               break;
            }
         }
      }

      stack.remove(0);
      return idx;
   }

   private static CharSequence reverse(CharSequence s) {
      return new StringBuilder(s).reverse();
   }
}
