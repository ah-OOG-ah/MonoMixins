package com.llamalad7.mixinextras.lib.semver;

import com.llamalad7.mixinextras.lib.semver.util.Stream;
import com.llamalad7.mixinextras.lib.semver.util.UnexpectedElementException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

class VersionParser {
   private final boolean isStrictModeOn;
   private final Stream<Character> chars;

   VersionParser(String input, boolean strictModeOn) {
      this.isStrictModeOn = strictModeOn;
      if (input != null && !input.isEmpty()) {
         Character[] elements = new Character[input.length()];

         for (int i = 0; i < input.length(); i++) {
            elements[i] = input.charAt(i);
         }

         this.chars = new Stream<>(elements);
      } else {
         throw new IllegalArgumentException("Input string is NULL or empty");
      }
   }

   static Version parseValidSemVer(String version, boolean strictModeOn) {
      VersionParser parser = new VersionParser(version, strictModeOn);
      return parser.parseValidSemVer();
   }

   private Version parseValidSemVer() {
      long[] versionParts = this.parseVersionCore();
      String[] preRelease = new String[0];
      String[] build = new String[0];
      Character next = this.consumeNextCharacter(VersionParser.CharType.HYPHEN, VersionParser.CharType.PLUS, VersionParser.CharType.EOI);
      if (VersionParser.CharType.HYPHEN.isMatchedBy(next)) {
         preRelease = this.parsePreRelease();
         next = this.consumeNextCharacter(VersionParser.CharType.PLUS, VersionParser.CharType.EOI);
      }

      if (VersionParser.CharType.PLUS.isMatchedBy(next)) {
         build = this.parseBuild();
      }

      this.consumeNextCharacter(VersionParser.CharType.EOI);
      return new Version(versionParts[0], versionParts[1], versionParts[2], preRelease, build);
   }

   private long[] parseVersionCore() {
      long major = this.numericIdentifier();
      long minor = 0L;
      if (this.isStrictModeOn || this.chars.positiveLookahead(VersionParser.CharType.DOT)) {
         this.consumeNextCharacter(VersionParser.CharType.DOT);
         minor = this.numericIdentifier();
      }

      long patch = 0L;
      if (this.isStrictModeOn || this.chars.positiveLookahead(VersionParser.CharType.DOT)) {
         this.consumeNextCharacter(VersionParser.CharType.DOT);
         patch = this.numericIdentifier();
      }

      return new long[]{major, minor, patch};
   }

   private String[] parsePreRelease() {
      this.ensureValidLookahead(VersionParser.CharType.DIGIT, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN);
      List<String> idents = new ArrayList<>();

      while (true) {
         idents.add(this.preReleaseIdentifier());
         if (!this.chars.positiveLookahead(VersionParser.CharType.DOT)) {
            return idents.toArray(new String[0]);
         }

         this.consumeNextCharacter(VersionParser.CharType.DOT);
      }
   }

   private String preReleaseIdentifier() {
      this.checkForEmptyIdentifier();
      VersionParser.CharType boundary = this.nearestCharType(VersionParser.CharType.DOT, VersionParser.CharType.PLUS, VersionParser.CharType.EOI);
      return this.chars.positiveLookaheadBefore(boundary, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN)
         ? this.alphanumericIdentifier()
         : String.valueOf(this.numericIdentifier());
   }

   private String[] parseBuild() {
      this.ensureValidLookahead(VersionParser.CharType.DIGIT, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN);
      List<String> idents = new ArrayList<>();

      while (true) {
         idents.add(this.buildIdentifier());
         if (!this.chars.positiveLookahead(VersionParser.CharType.DOT)) {
            return idents.toArray(new String[0]);
         }

         this.consumeNextCharacter(VersionParser.CharType.DOT);
      }
   }

   private String buildIdentifier() {
      this.checkForEmptyIdentifier();
      VersionParser.CharType boundary = this.nearestCharType(VersionParser.CharType.DOT, VersionParser.CharType.EOI);
      return this.chars.positiveLookaheadBefore(boundary, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN)
         ? this.alphanumericIdentifier()
         : this.digits();
   }

   private long numericIdentifier() {
      this.checkForLeadingZeroes();

      try {
         return Long.parseLong(this.digits());
      } catch (NumberFormatException var2) {
         throw new ParseException("Numeric identifier overflow");
      }
   }

   private String alphanumericIdentifier() {
      StringBuilder sb = new StringBuilder();

      do {
         sb.append(this.consumeNextCharacter(VersionParser.CharType.DIGIT, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN));
      } while (this.chars.positiveLookahead(VersionParser.CharType.DIGIT, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN));

      return sb.toString();
   }

   private String digits() {
      StringBuilder sb = new StringBuilder();

      do {
         sb.append(this.consumeNextCharacter(VersionParser.CharType.DIGIT));
      } while (this.chars.positiveLookahead(VersionParser.CharType.DIGIT));

      return sb.toString();
   }

   private VersionParser.CharType nearestCharType(VersionParser.CharType... types) {
      for (Character chr : this.chars) {
         for (VersionParser.CharType type : types) {
            if (type.isMatchedBy(chr)) {
               return type;
            }
         }
      }

      return VersionParser.CharType.EOI;
   }

   private void checkForLeadingZeroes() {
      Character la1 = this.chars.lookahead(1);
      Character la2 = this.chars.lookahead(2);
      if (la1 != null && la1 == '0' && VersionParser.CharType.DIGIT.isMatchedBy(la2)) {
         throw new ParseException("Numeric identifier MUST NOT contain leading zeroes");
      }
   }

   private void checkForEmptyIdentifier() {
      Character la = this.chars.lookahead(1);
      if (VersionParser.CharType.DOT.isMatchedBy(la) || VersionParser.CharType.PLUS.isMatchedBy(la) || VersionParser.CharType.EOI.isMatchedBy(la)) {
         throw new ParseException(
            "Identifiers MUST NOT be empty",
            new UnexpectedCharacterException(
               la, this.chars.currentOffset(), VersionParser.CharType.DIGIT, VersionParser.CharType.LETTER, VersionParser.CharType.HYPHEN
            )
         );
      }
   }

   private Character consumeNextCharacter(VersionParser.CharType... expected) {
      try {
         return this.chars.consume(expected);
      } catch (UnexpectedElementException var3) {
         throw new UnexpectedCharacterException(var3);
      }
   }

   private void ensureValidLookahead(VersionParser.CharType... expected) {
      if (!this.chars.positiveLookahead(expected)) {
         throw new UnexpectedCharacterException(this.chars.lookahead(1), this.chars.currentOffset(), expected);
      }
   }

   static enum CharType implements Stream.ElementType<Character> {
      DIGIT {
         public boolean isMatchedBy(Character chr) {
            return chr == null ? false : chr >= '0' && chr <= '9';
         }
      },
      LETTER {
         public boolean isMatchedBy(Character chr) {
            return chr == null ? false : chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z';
         }
      },
      DOT {
         public boolean isMatchedBy(Character chr) {
            return chr == null ? false : chr == '.';
         }
      },
      HYPHEN {
         public boolean isMatchedBy(Character chr) {
            return chr == null ? false : chr == '-';
         }
      },
      PLUS {
         public boolean isMatchedBy(Character chr) {
            return chr == null ? false : chr == '+';
         }
      },
      EOI {
         public boolean isMatchedBy(Character chr) {
            return chr == null;
         }
      },
      ILLEGAL {
         public boolean isMatchedBy(Character chr) {
            EnumSet<VersionParser.CharType> itself = EnumSet.of(ILLEGAL);

            for (VersionParser.CharType type : EnumSet.complementOf(itself)) {
               if (type.isMatchedBy(chr)) {
                  return false;
               }
            }

            return true;
         }
      };

      private CharType() {
      }

      static VersionParser.CharType forCharacter(Character chr) {
         for (VersionParser.CharType type : values()) {
            if (type.isMatchedBy(chr)) {
               return type;
            }
         }

         return null;
      }
   }
}
