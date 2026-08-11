package org.spongepowered.libraries.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.RoundingMode;
import java.util.Arrays;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.CharMatcher;
import org.spongepowered.libraries.com.google.common.base.Objects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.math.IntMath;

@GwtCompatible(emulated = true)
public abstract class BaseEncoding {
   private static final BaseEncoding BASE64 = new BaseEncoding.Base64Encoding(
      "base64()", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", '='
   );
   private static final BaseEncoding BASE64_URL = new BaseEncoding.Base64Encoding(
      "base64Url()", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_", '='
   );
   private static final BaseEncoding BASE32 = new BaseEncoding.StandardBaseEncoding("base32()", "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", '=');
   private static final BaseEncoding BASE32_HEX = new BaseEncoding.StandardBaseEncoding("base32Hex()", "0123456789ABCDEFGHIJKLMNOPQRSTUV", '=');
   private static final BaseEncoding BASE16 = new BaseEncoding.Base16Encoding("base16()", "0123456789ABCDEF");

   BaseEncoding() {
   }

   public String encode(byte[] bytes) {
      return this.encode(bytes, 0, bytes.length);
   }

   public final String encode(byte[] bytes, int off, int len) {
      Preconditions.checkPositionIndexes(off, off + len, bytes.length);
      StringBuilder result = new StringBuilder(this.maxEncodedSize(len));

      try {
         this.encodeTo(result, bytes, off, len);
      } catch (IOException var6) {
         throw new AssertionError(var6);
      }

      return result.toString();
   }

   @GwtIncompatible
   public abstract OutputStream encodingStream(Writer var1);

   @GwtIncompatible
   public final ByteSink encodingSink(final CharSink encodedSink) {
      Preconditions.checkNotNull(encodedSink);
      return new ByteSink() {
         @Override
         public OutputStream openStream() throws IOException {
            return BaseEncoding.this.encodingStream(encodedSink.openStream());
         }
      };
   }

   private static byte[] extract(byte[] result, int length) {
      if (length == result.length) {
         return result;
      } else {
         byte[] trunc = new byte[length];
         System.arraycopy(result, 0, trunc, 0, length);
         return trunc;
      }
   }

   public abstract boolean canDecode(CharSequence var1);

   public final byte[] decode(CharSequence chars) {
      try {
         return this.decodeChecked(chars);
      } catch (BaseEncoding.DecodingException var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   final byte[] decodeChecked(CharSequence chars) throws BaseEncoding.DecodingException {
      CharSequence var4 = this.padding().trimTrailingFrom(chars);
      byte[] tmp = new byte[this.maxDecodedSize(var4.length())];
      int len = this.decodeTo(tmp, var4);
      return extract(tmp, len);
   }

   @GwtIncompatible
   public abstract InputStream decodingStream(Reader var1);

   @GwtIncompatible
   public final ByteSource decodingSource(final CharSource encodedSource) {
      Preconditions.checkNotNull(encodedSource);
      return new ByteSource() {
         @Override
         public InputStream openStream() throws IOException {
            return BaseEncoding.this.decodingStream(encodedSource.openStream());
         }
      };
   }

   abstract int maxEncodedSize(int var1);

   abstract void encodeTo(Appendable var1, byte[] var2, int var3, int var4) throws IOException;

   abstract int maxDecodedSize(int var1);

   abstract int decodeTo(byte[] var1, CharSequence var2) throws BaseEncoding.DecodingException;

   abstract CharMatcher padding();

   public abstract BaseEncoding omitPadding();

   public abstract BaseEncoding withPadChar(char var1);

   public abstract BaseEncoding withSeparator(String var1, int var2);

   public abstract BaseEncoding upperCase();

   public abstract BaseEncoding lowerCase();

   public static BaseEncoding base64() {
      return BASE64;
   }

   public static BaseEncoding base64Url() {
      return BASE64_URL;
   }

   public static BaseEncoding base32() {
      return BASE32;
   }

   public static BaseEncoding base32Hex() {
      return BASE32_HEX;
   }

   public static BaseEncoding base16() {
      return BASE16;
   }

   @GwtIncompatible
   static Reader ignoringReader(final Reader delegate, final CharMatcher toIgnore) {
      Preconditions.checkNotNull(delegate);
      Preconditions.checkNotNull(toIgnore);
      return new Reader() {
         @Override
         public int read() throws IOException {
            int readChar;
            do {
               readChar = delegate.read();
            } while (readChar != -1 && toIgnore.matches((char)readChar));

            return readChar;
         }

         @Override
         public int read(char[] cbuf, int off, int len) throws IOException {
            throw new UnsupportedOperationException();
         }

         @Override
         public void close() throws IOException {
            delegate.close();
         }
      };
   }

   static Appendable separatingAppendable(final Appendable delegate, final String separator, final int afterEveryChars) {
      Preconditions.checkNotNull(delegate);
      Preconditions.checkNotNull(separator);
      Preconditions.checkArgument(afterEveryChars > 0);
      return new Appendable() {
         int charsUntilSeparator = afterEveryChars;

         @Override
         public Appendable append(char c) throws IOException {
            if (this.charsUntilSeparator == 0) {
               delegate.append(separator);
               this.charsUntilSeparator = afterEveryChars;
            }

            delegate.append(c);
            this.charsUntilSeparator--;
            return this;
         }

         @Override
         public Appendable append(CharSequence chars, int off, int len) throws IOException {
            throw new UnsupportedOperationException();
         }

         @Override
         public Appendable append(CharSequence chars) throws IOException {
            throw new UnsupportedOperationException();
         }
      };
   }

   @GwtIncompatible
   static Writer separatingWriter(final Writer delegate, String separator, int afterEveryChars) {
      final Appendable seperatingAppendable = separatingAppendable(delegate, separator, afterEveryChars);
      return new Writer() {
         @Override
         public void write(int c) throws IOException {
            seperatingAppendable.append((char)c);
         }

         @Override
         public void write(char[] chars, int off, int len) throws IOException {
            throw new UnsupportedOperationException();
         }

         @Override
         public void flush() throws IOException {
            delegate.flush();
         }

         @Override
         public void close() throws IOException {
            delegate.close();
         }
      };
   }

   private static final class Alphabet extends CharMatcher {
      private final String name;
      private final char[] chars;
      final int mask;
      final int bitsPerChar;
      final int charsPerChunk;
      final int bytesPerChunk;
      private final byte[] decodabet;
      private final boolean[] validPadding;

      Alphabet(String name, char[] chars) {
         this.name = Preconditions.checkNotNull(name);
         this.chars = Preconditions.checkNotNull(chars);

         try {
            this.bitsPerChar = IntMath.log2(chars.length, RoundingMode.UNNECESSARY);
         } catch (ArithmeticException var8) {
            throw new IllegalArgumentException("Illegal alphabet length " + chars.length, var8);
         }

         int gcd = Math.min(8, Integer.lowestOneBit(this.bitsPerChar));

         try {
            this.charsPerChunk = 8 / gcd;
            this.bytesPerChunk = this.bitsPerChar / gcd;
         } catch (ArithmeticException var7) {
            throw new IllegalArgumentException("Illegal alphabet " + new String(chars), var7);
         }

         this.mask = chars.length - 1;
         byte[] decodabet = new byte[128];
         Arrays.fill(decodabet, (byte)-1);

         for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Preconditions.checkArgument(CharMatcher.ascii().matches(c), "Non-ASCII character: %s", c);
            Preconditions.checkArgument(decodabet[c] == -1, "Duplicate character: %s", c);
            decodabet[c] = (byte)i;
         }

         this.decodabet = decodabet;
         boolean[] validPadding = new boolean[this.charsPerChunk];

         for (int i = 0; i < this.bytesPerChunk; i++) {
            validPadding[IntMath.divide(i * 8, this.bitsPerChar, RoundingMode.CEILING)] = true;
         }

         this.validPadding = validPadding;
      }

      char encode(int bits) {
         return this.chars[bits];
      }

      boolean isValidPaddingStartPosition(int index) {
         return this.validPadding[index % this.charsPerChunk];
      }

      boolean canDecode(char ch) {
         return ch <= 127 && this.decodabet[ch] != -1;
      }

      int decode(char ch) throws BaseEncoding.DecodingException {
         if (ch <= 127 && this.decodabet[ch] != -1) {
            return this.decodabet[ch];
         } else {
            throw new BaseEncoding.DecodingException("Unrecognized character: " + (CharMatcher.invisible().matches(ch) ? "0x" + Integer.toHexString(ch) : ch));
         }
      }

      private boolean hasLowerCase() {
         for (char c : this.chars) {
            if (org.spongepowered.libraries.com.google.common.base.Ascii.isLowerCase(c)) {
               return true;
            }
         }

         return false;
      }

      private boolean hasUpperCase() {
         for (char c : this.chars) {
            if (org.spongepowered.libraries.com.google.common.base.Ascii.isUpperCase(c)) {
               return true;
            }
         }

         return false;
      }

      BaseEncoding.Alphabet upperCase() {
         if (!this.hasLowerCase()) {
            return this;
         } else {
            Preconditions.checkState(!this.hasUpperCase(), "Cannot call upperCase() on a mixed-case alphabet");
            char[] upperCased = new char[this.chars.length];

            for (int i = 0; i < this.chars.length; i++) {
               upperCased[i] = org.spongepowered.libraries.com.google.common.base.Ascii.toUpperCase(this.chars[i]);
            }

            return new BaseEncoding.Alphabet(this.name + ".upperCase()", upperCased);
         }
      }

      BaseEncoding.Alphabet lowerCase() {
         if (!this.hasUpperCase()) {
            return this;
         } else {
            Preconditions.checkState(!this.hasLowerCase(), "Cannot call lowerCase() on a mixed-case alphabet");
            char[] lowerCased = new char[this.chars.length];

            for (int i = 0; i < this.chars.length; i++) {
               lowerCased[i] = org.spongepowered.libraries.com.google.common.base.Ascii.toLowerCase(this.chars[i]);
            }

            return new BaseEncoding.Alphabet(this.name + ".lowerCase()", lowerCased);
         }
      }

      @Override
      public boolean matches(char c) {
         return CharMatcher.ascii().matches(c) && this.decodabet[c] != -1;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public boolean equals(@Nullable Object other) {
         if (other instanceof BaseEncoding.Alphabet) {
            BaseEncoding.Alphabet that = (BaseEncoding.Alphabet)other;
            return Arrays.equals(this.chars, that.chars);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.chars);
      }
   }

   static final class Base16Encoding extends BaseEncoding.StandardBaseEncoding {
      final char[] encoding = new char[512];

      Base16Encoding(String name, String alphabetChars) {
         this(new BaseEncoding.Alphabet(name, alphabetChars.toCharArray()));
      }

      private Base16Encoding(BaseEncoding.Alphabet alphabet) {
         super(alphabet, null);
         Preconditions.checkArgument(alphabet.chars.length == 16);

         for (int i = 0; i < 256; i++) {
            this.encoding[i] = alphabet.encode(i >>> 4);
            this.encoding[i | 256] = alphabet.encode(i & 15);
         }
      }

      @Override
      void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
         Preconditions.checkNotNull(target);
         Preconditions.checkPositionIndexes(off, off + len, bytes.length);

         for (int i = 0; i < len; i++) {
            int b = bytes[off + i] & 255;
            target.append(this.encoding[b]);
            target.append(this.encoding[b | 256]);
         }
      }

      @Override
      int decodeTo(byte[] target, CharSequence chars) throws BaseEncoding.DecodingException {
         Preconditions.checkNotNull(target);
         if (chars.length() % 2 == 1) {
            throw new BaseEncoding.DecodingException("Invalid input length " + chars.length());
         } else {
            int bytesWritten = 0;

            for (int i = 0; i < chars.length(); i += 2) {
               int decoded = this.alphabet.decode(chars.charAt(i)) << 4 | this.alphabet.decode(chars.charAt(i + 1));
               target[bytesWritten++] = (byte)decoded;
            }

            return bytesWritten;
         }
      }

      @Override
      BaseEncoding newInstance(BaseEncoding.Alphabet alphabet, @Nullable Character paddingChar) {
         return new BaseEncoding.Base16Encoding(alphabet);
      }
   }

   static final class Base64Encoding extends BaseEncoding.StandardBaseEncoding {
      Base64Encoding(String name, String alphabetChars, @Nullable Character paddingChar) {
         this(new BaseEncoding.Alphabet(name, alphabetChars.toCharArray()), paddingChar);
      }

      private Base64Encoding(BaseEncoding.Alphabet alphabet, @Nullable Character paddingChar) {
         super(alphabet, paddingChar);
         Preconditions.checkArgument(alphabet.chars.length == 64);
      }

      @Override
      void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
         Preconditions.checkNotNull(target);
         Preconditions.checkPositionIndexes(off, off + len, bytes.length);
         int i = off;

         for (int remaining = len; remaining >= 3; remaining -= 3) {
            int chunk = (bytes[i++] & 255) << 16 | (bytes[i++] & 255) << 8 | bytes[i++] & 255;
            target.append(this.alphabet.encode(chunk >>> 18));
            target.append(this.alphabet.encode(chunk >>> 12 & 63));
            target.append(this.alphabet.encode(chunk >>> 6 & 63));
            target.append(this.alphabet.encode(chunk & 63));
         }

         if (i < off + len) {
            this.encodeChunkTo(target, bytes, i, off + len - i);
         }
      }

      @Override
      int decodeTo(byte[] target, CharSequence chars) throws BaseEncoding.DecodingException {
         Preconditions.checkNotNull(target);
         CharSequence var6 = this.padding().trimTrailingFrom(chars);
         if (!this.alphabet.isValidPaddingStartPosition(var6.length())) {
            throw new BaseEncoding.DecodingException("Invalid input length " + var6.length());
         } else {
            int bytesWritten = 0;
            int i = 0;

            while (i < var6.length()) {
               int chunk = this.alphabet.decode(var6.charAt(i++)) << 18;
               chunk |= this.alphabet.decode(var6.charAt(i++)) << 12;
               target[bytesWritten++] = (byte)(chunk >>> 16);
               if (i < var6.length()) {
                  chunk |= this.alphabet.decode(var6.charAt(i++)) << 6;
                  target[bytesWritten++] = (byte)(chunk >>> 8 & 0xFF);
                  if (i < var6.length()) {
                     chunk |= this.alphabet.decode(var6.charAt(i++));
                     target[bytesWritten++] = (byte)(chunk & 0xFF);
                  }
               }
            }

            return bytesWritten;
         }
      }

      @Override
      BaseEncoding newInstance(BaseEncoding.Alphabet alphabet, @Nullable Character paddingChar) {
         return new BaseEncoding.Base64Encoding(alphabet, paddingChar);
      }
   }

   public static final class DecodingException extends IOException {
      DecodingException(String message) {
         super(message);
      }

      DecodingException(Throwable cause) {
         super(cause);
      }
   }

   static final class SeparatedBaseEncoding extends BaseEncoding {
      private final BaseEncoding delegate;
      private final String separator;
      private final int afterEveryChars;
      private final CharMatcher separatorChars;

      SeparatedBaseEncoding(BaseEncoding delegate, String separator, int afterEveryChars) {
         this.delegate = Preconditions.checkNotNull(delegate);
         this.separator = Preconditions.checkNotNull(separator);
         this.afterEveryChars = afterEveryChars;
         Preconditions.checkArgument(afterEveryChars > 0, "Cannot add a separator after every %s chars", afterEveryChars);
         this.separatorChars = CharMatcher.anyOf(separator).precomputed();
      }

      @Override
      CharMatcher padding() {
         return this.delegate.padding();
      }

      @Override
      int maxEncodedSize(int bytes) {
         int unseparatedSize = this.delegate.maxEncodedSize(bytes);
         return unseparatedSize + this.separator.length() * IntMath.divide(Math.max(0, unseparatedSize - 1), this.afterEveryChars, RoundingMode.FLOOR);
      }

      @GwtIncompatible
      @Override
      public OutputStream encodingStream(Writer output) {
         return this.delegate.encodingStream(separatingWriter(output, this.separator, this.afterEveryChars));
      }

      @Override
      void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
         this.delegate.encodeTo(separatingAppendable(target, this.separator, this.afterEveryChars), bytes, off, len);
      }

      @Override
      int maxDecodedSize(int chars) {
         return this.delegate.maxDecodedSize(chars);
      }

      @Override
      public boolean canDecode(CharSequence chars) {
         return this.delegate.canDecode(this.separatorChars.removeFrom(chars));
      }

      @Override
      int decodeTo(byte[] target, CharSequence chars) throws BaseEncoding.DecodingException {
         return this.delegate.decodeTo(target, this.separatorChars.removeFrom(chars));
      }

      @GwtIncompatible
      @Override
      public InputStream decodingStream(Reader reader) {
         return this.delegate.decodingStream(ignoringReader(reader, this.separatorChars));
      }

      @Override
      public BaseEncoding omitPadding() {
         return this.delegate.omitPadding().withSeparator(this.separator, this.afterEveryChars);
      }

      @Override
      public BaseEncoding withPadChar(char padChar) {
         return this.delegate.withPadChar(padChar).withSeparator(this.separator, this.afterEveryChars);
      }

      @Override
      public BaseEncoding withSeparator(String separator, int afterEveryChars) {
         throw new UnsupportedOperationException("Already have a separator");
      }

      @Override
      public BaseEncoding upperCase() {
         return this.delegate.upperCase().withSeparator(this.separator, this.afterEveryChars);
      }

      @Override
      public BaseEncoding lowerCase() {
         return this.delegate.lowerCase().withSeparator(this.separator, this.afterEveryChars);
      }

      @Override
      public String toString() {
         return this.delegate + ".withSeparator(\"" + this.separator + "\", " + this.afterEveryChars + ")";
      }
   }

   static class StandardBaseEncoding extends BaseEncoding {
      final BaseEncoding.Alphabet alphabet;
      @Nullable
      final Character paddingChar;
      private transient BaseEncoding upperCase;
      private transient BaseEncoding lowerCase;

      StandardBaseEncoding(String name, String alphabetChars, @Nullable Character paddingChar) {
         this(new BaseEncoding.Alphabet(name, alphabetChars.toCharArray()), paddingChar);
      }

      StandardBaseEncoding(BaseEncoding.Alphabet alphabet, @Nullable Character paddingChar) {
         this.alphabet = Preconditions.checkNotNull(alphabet);
         Preconditions.checkArgument(paddingChar == null || !alphabet.matches(paddingChar), "Padding character %s was already in alphabet", paddingChar);
         this.paddingChar = paddingChar;
      }

      @Override
      CharMatcher padding() {
         return this.paddingChar == null ? CharMatcher.none() : CharMatcher.is(this.paddingChar);
      }

      @Override
      int maxEncodedSize(int bytes) {
         return this.alphabet.charsPerChunk * IntMath.divide(bytes, this.alphabet.bytesPerChunk, RoundingMode.CEILING);
      }

      @GwtIncompatible
      @Override
      public OutputStream encodingStream(final Writer out) {
         Preconditions.checkNotNull(out);
         return new OutputStream() {
            int bitBuffer = 0;
            int bitBufferLength = 0;
            int writtenChars = 0;

            @Override
            public void write(int b) throws IOException {
               this.bitBuffer <<= 8;
               this.bitBuffer |= b & 0xFF;

               for (this.bitBufferLength += 8;
                  this.bitBufferLength >= StandardBaseEncoding.this.alphabet.bitsPerChar;
                  this.bitBufferLength = this.bitBufferLength - StandardBaseEncoding.this.alphabet.bitsPerChar
               ) {
                  int charIndex = this.bitBuffer >> this.bitBufferLength - StandardBaseEncoding.this.alphabet.bitsPerChar
                     & StandardBaseEncoding.this.alphabet.mask;
                  out.write(StandardBaseEncoding.this.alphabet.encode(charIndex));
                  this.writtenChars++;
               }
            }

            @Override
            public void flush() throws IOException {
               out.flush();
            }

            @Override
            public void close() throws IOException {
               if (this.bitBufferLength > 0) {
                  int charIndex = this.bitBuffer << StandardBaseEncoding.this.alphabet.bitsPerChar - this.bitBufferLength
                     & StandardBaseEncoding.this.alphabet.mask;
                  out.write(StandardBaseEncoding.this.alphabet.encode(charIndex));
                  this.writtenChars++;
                  if (StandardBaseEncoding.this.paddingChar != null) {
                     while (this.writtenChars % StandardBaseEncoding.this.alphabet.charsPerChunk != 0) {
                        out.write(StandardBaseEncoding.this.paddingChar);
                        this.writtenChars++;
                     }
                  }
               }

               out.close();
            }
         };
      }

      @Override
      void encodeTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
         Preconditions.checkNotNull(target);
         Preconditions.checkPositionIndexes(off, off + len, bytes.length);
         int i = 0;

         while (i < len) {
            this.encodeChunkTo(target, bytes, off + i, Math.min(this.alphabet.bytesPerChunk, len - i));
            i += this.alphabet.bytesPerChunk;
         }
      }

      void encodeChunkTo(Appendable target, byte[] bytes, int off, int len) throws IOException {
         Preconditions.checkNotNull(target);
         Preconditions.checkPositionIndexes(off, off + len, bytes.length);
         Preconditions.checkArgument(len <= this.alphabet.bytesPerChunk);
         long bitBuffer = 0L;

         for (int i = 0; i < len; i++) {
            bitBuffer |= bytes[off + i] & 255;
            bitBuffer <<= 8;
         }

         int bitOffset = (len + 1) * 8 - this.alphabet.bitsPerChar;

         int bitsProcessed;
         for (bitsProcessed = 0; bitsProcessed < len * 8; bitsProcessed += this.alphabet.bitsPerChar) {
            int charIndex = (int)(bitBuffer >>> bitOffset - bitsProcessed) & this.alphabet.mask;
            target.append(this.alphabet.encode(charIndex));
         }

         if (this.paddingChar != null) {
            while (bitsProcessed < this.alphabet.bytesPerChunk * 8) {
               target.append(this.paddingChar);
               bitsProcessed += this.alphabet.bitsPerChar;
            }
         }
      }

      @Override
      int maxDecodedSize(int chars) {
         return (int)(((long)this.alphabet.bitsPerChar * chars + 7L) / 8L);
      }

      @Override
      public boolean canDecode(CharSequence chars) {
         CharSequence var3 = this.padding().trimTrailingFrom(chars);
         if (!this.alphabet.isValidPaddingStartPosition(var3.length())) {
            return false;
         } else {
            for (int i = 0; i < var3.length(); i++) {
               if (!this.alphabet.canDecode(var3.charAt(i))) {
                  return false;
               }
            }

            return true;
         }
      }

      @Override
      int decodeTo(byte[] target, CharSequence chars) throws BaseEncoding.DecodingException {
         Preconditions.checkNotNull(target);
         CharSequence var10 = this.padding().trimTrailingFrom(chars);
         if (!this.alphabet.isValidPaddingStartPosition(var10.length())) {
            throw new BaseEncoding.DecodingException("Invalid input length " + var10.length());
         } else {
            int bytesWritten = 0;

            for (int charIdx = 0; charIdx < var10.length(); charIdx += this.alphabet.charsPerChunk) {
               long chunk = 0L;
               int charsProcessed = 0;

               for (int i = 0; i < this.alphabet.charsPerChunk; i++) {
                  chunk <<= this.alphabet.bitsPerChar;
                  if (charIdx + i < var10.length()) {
                     chunk |= this.alphabet.decode(var10.charAt(charIdx + charsProcessed++));
                  }
               }

               int minOffset = this.alphabet.bytesPerChunk * 8 - charsProcessed * this.alphabet.bitsPerChar;

               for (int offset = (this.alphabet.bytesPerChunk - 1) * 8; offset >= minOffset; offset -= 8) {
                  target[bytesWritten++] = (byte)(chunk >>> offset & 255L);
               }
            }

            return bytesWritten;
         }
      }

      @GwtIncompatible
      @Override
      public InputStream decodingStream(final Reader reader) {
         Preconditions.checkNotNull(reader);
         return new InputStream() {
            int bitBuffer = 0;
            int bitBufferLength = 0;
            int readChars = 0;
            boolean hitPadding = false;
            final CharMatcher paddingMatcher = StandardBaseEncoding.this.padding();

            @Override
            public int read() throws IOException {
               while (true) {
                  int readChar = reader.read();
                  if (readChar == -1) {
                     if (!this.hitPadding && !StandardBaseEncoding.this.alphabet.isValidPaddingStartPosition(this.readChars)) {
                        throw new BaseEncoding.DecodingException("Invalid input length " + this.readChars);
                     }

                     return -1;
                  }

                  this.readChars++;
                  char ch = (char)readChar;
                  if (!this.paddingMatcher.matches(ch)) {
                     if (this.hitPadding) {
                        throw new BaseEncoding.DecodingException("Expected padding character but found '" + ch + "' at index " + this.readChars);
                     }

                     this.bitBuffer = this.bitBuffer << StandardBaseEncoding.this.alphabet.bitsPerChar;
                     this.bitBuffer = this.bitBuffer | StandardBaseEncoding.this.alphabet.decode(ch);
                     this.bitBufferLength = this.bitBufferLength + StandardBaseEncoding.this.alphabet.bitsPerChar;
                     if (this.bitBufferLength >= 8) {
                        this.bitBufferLength -= 8;
                        return this.bitBuffer >> this.bitBufferLength & 0xFF;
                     }
                  } else {
                     if (!this.hitPadding && (this.readChars == 1 || !StandardBaseEncoding.this.alphabet.isValidPaddingStartPosition(this.readChars - 1))) {
                        throw new BaseEncoding.DecodingException("Padding cannot start at index " + this.readChars);
                     }

                     this.hitPadding = true;
                  }
               }
            }

            @Override
            public void close() throws IOException {
               reader.close();
            }
         };
      }

      @Override
      public BaseEncoding omitPadding() {
         return (BaseEncoding)(this.paddingChar == null ? this : this.newInstance(this.alphabet, null));
      }

      @Override
      public BaseEncoding withPadChar(char padChar) {
         return (BaseEncoding)(8 % this.alphabet.bitsPerChar != 0 && (this.paddingChar == null || this.paddingChar != padChar)
            ? this.newInstance(this.alphabet, padChar)
            : this);
      }

      @Override
      public BaseEncoding withSeparator(String separator, int afterEveryChars) {
         Preconditions.checkArgument(
            this.padding().or(this.alphabet).matchesNoneOf(separator), "Separator (%s) cannot contain alphabet or padding characters", separator
         );
         return new BaseEncoding.SeparatedBaseEncoding(this, separator, afterEveryChars);
      }

      @Override
      public BaseEncoding upperCase() {
         BaseEncoding result = this.upperCase;
         if (result == null) {
            BaseEncoding.Alphabet upper = this.alphabet.upperCase();
            result = this.upperCase = (BaseEncoding)(upper == this.alphabet ? this : this.newInstance(upper, this.paddingChar));
         }

         return result;
      }

      @Override
      public BaseEncoding lowerCase() {
         BaseEncoding result = this.lowerCase;
         if (result == null) {
            BaseEncoding.Alphabet lower = this.alphabet.lowerCase();
            result = this.lowerCase = (BaseEncoding)(lower == this.alphabet ? this : this.newInstance(lower, this.paddingChar));
         }

         return result;
      }

      BaseEncoding newInstance(BaseEncoding.Alphabet alphabet, @Nullable Character paddingChar) {
         return new BaseEncoding.StandardBaseEncoding(alphabet, paddingChar);
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder("BaseEncoding.");
         builder.append(this.alphabet.toString());
         if (8 % this.alphabet.bitsPerChar != 0) {
            if (this.paddingChar == null) {
               builder.append(".omitPadding()");
            } else {
               builder.append(".withPadChar('").append(this.paddingChar).append("')");
            }
         }

         return builder.toString();
      }

      @Override
      public boolean equals(@Nullable Object other) {
         if (!(other instanceof BaseEncoding.StandardBaseEncoding)) {
            return false;
         } else {
            BaseEncoding.StandardBaseEncoding that = (BaseEncoding.StandardBaseEncoding)other;
            return this.alphabet.equals(that.alphabet) && Objects.equal(this.paddingChar, that.paddingChar);
         }
      }

      @Override
      public int hashCode() {
         return this.alphabet.hashCode() ^ Objects.hashCode(this.paddingChar);
      }
   }
}
