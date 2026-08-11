package com.llamalad7.mixinextras.lib.antlr.runtime;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

public class CodePointBuffer {
   private final CodePointBuffer.Type type;
   private final ByteBuffer byteBuffer;
   private final CharBuffer charBuffer;
   private final IntBuffer intBuffer;

   private CodePointBuffer(CodePointBuffer.Type type, ByteBuffer byteBuffer, CharBuffer charBuffer, IntBuffer intBuffer) {
      this.type = type;
      this.byteBuffer = byteBuffer;
      this.charBuffer = charBuffer;
      this.intBuffer = intBuffer;
   }

   public int position() {
      switch (this.type) {
         case BYTE:
            return this.byteBuffer.position();
         case CHAR:
            return this.charBuffer.position();
         case INT:
            return this.intBuffer.position();
         default:
            throw new UnsupportedOperationException("Not reached");
      }
   }

   public int remaining() {
      switch (this.type) {
         case BYTE:
            return this.byteBuffer.remaining();
         case CHAR:
            return this.charBuffer.remaining();
         case INT:
            return this.intBuffer.remaining();
         default:
            throw new UnsupportedOperationException("Not reached");
      }
   }

   CodePointBuffer.Type getType() {
      return this.type;
   }

   int arrayOffset() {
      switch (this.type) {
         case BYTE:
            return this.byteBuffer.arrayOffset();
         case CHAR:
            return this.charBuffer.arrayOffset();
         case INT:
            return this.intBuffer.arrayOffset();
         default:
            throw new UnsupportedOperationException("Not reached");
      }
   }

   byte[] byteArray() {
      assert this.type == CodePointBuffer.Type.BYTE;

      return this.byteBuffer.array();
   }

   char[] charArray() {
      assert this.type == CodePointBuffer.Type.CHAR;

      return this.charBuffer.array();
   }

   int[] intArray() {
      assert this.type == CodePointBuffer.Type.INT;

      return this.intBuffer.array();
   }

   public static CodePointBuffer.Builder builder(int initialBufferSize) {
      return new CodePointBuffer.Builder(initialBufferSize);
   }

   public static class Builder {
      private CodePointBuffer.Type type = CodePointBuffer.Type.BYTE;
      private ByteBuffer byteBuffer;
      private CharBuffer charBuffer;
      private IntBuffer intBuffer;
      private int prevHighSurrogate;

      private Builder(int initialBufferSize) {
         this.byteBuffer = ByteBuffer.allocate(initialBufferSize);
         this.charBuffer = null;
         this.intBuffer = null;
         this.prevHighSurrogate = -1;
      }

      public CodePointBuffer build() {
         switch (this.type) {
            case BYTE:
               ((Buffer)this.byteBuffer).flip();
               break;
            case CHAR:
               ((Buffer)this.charBuffer).flip();
               break;
            case INT:
               ((Buffer)this.intBuffer).flip();
         }

         return new CodePointBuffer(this.type, this.byteBuffer, this.charBuffer, this.intBuffer);
      }

      private static int roundUpToNextPowerOfTwo(int i) {
         int nextPowerOfTwo = 32 - Integer.numberOfLeadingZeros(i - 1);
         return (int)Math.pow(2.0, nextPowerOfTwo);
      }

      public void ensureRemaining(int remainingNeeded) {
         switch (this.type) {
            case BYTE:
               if (this.byteBuffer.remaining() < remainingNeeded) {
                  int newCapacity = roundUpToNextPowerOfTwo(this.byteBuffer.capacity() + remainingNeeded);
                  ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
                  ((Buffer)this.byteBuffer).flip();
                  newBuffer.put(this.byteBuffer);
                  this.byteBuffer = newBuffer;
               }
               break;
            case CHAR:
               if (this.charBuffer.remaining() < remainingNeeded) {
                  int newCapacity = roundUpToNextPowerOfTwo(this.charBuffer.capacity() + remainingNeeded);
                  CharBuffer newBuffer = CharBuffer.allocate(newCapacity);
                  ((Buffer)this.charBuffer).flip();
                  newBuffer.put(this.charBuffer);
                  this.charBuffer = newBuffer;
               }
               break;
            case INT:
               if (this.intBuffer.remaining() < remainingNeeded) {
                  int newCapacity = roundUpToNextPowerOfTwo(this.intBuffer.capacity() + remainingNeeded);
                  IntBuffer newBuffer = IntBuffer.allocate(newCapacity);
                  ((Buffer)this.intBuffer).flip();
                  newBuffer.put(this.intBuffer);
                  this.intBuffer = newBuffer;
               }
         }
      }

      public void append(CharBuffer utf16In) {
         this.ensureRemaining(utf16In.remaining());
         if (utf16In.hasArray()) {
            this.appendArray(utf16In);
         } else {
            throw new UnsupportedOperationException("TODO");
         }
      }

      private void appendArray(CharBuffer utf16In) {
         assert utf16In.hasArray();

         switch (this.type) {
            case BYTE:
               this.appendArrayByte(utf16In);
               break;
            case CHAR:
               this.appendArrayChar(utf16In);
               break;
            case INT:
               this.appendArrayInt(utf16In);
         }
      }

      private void appendArrayByte(CharBuffer utf16In) {
         assert this.prevHighSurrogate == -1;

         char[] in = utf16In.array();
         int inOffset = utf16In.arrayOffset() + utf16In.position();
         int inLimit = utf16In.arrayOffset() + utf16In.limit();
         byte[] outByte = this.byteBuffer.array();

         int outOffset;
         for (outOffset = this.byteBuffer.arrayOffset() + this.byteBuffer.position(); inOffset < inLimit; outOffset++) {
            char c = in[inOffset];
            if (c > 255) {
               ((Buffer)utf16In).position(inOffset - utf16In.arrayOffset());
               ((Buffer)this.byteBuffer).position(outOffset - this.byteBuffer.arrayOffset());
               if (!Character.isHighSurrogate(c)) {
                  this.byteToCharBuffer(utf16In.remaining());
                  this.appendArrayChar(utf16In);
                  return;
               }

               this.byteToIntBuffer(utf16In.remaining());
               this.appendArrayInt(utf16In);
               return;
            }

            outByte[outOffset] = (byte)(c & 255);
            inOffset++;
         }

         ((Buffer)utf16In).position(inOffset - utf16In.arrayOffset());
         ((Buffer)this.byteBuffer).position(outOffset - this.byteBuffer.arrayOffset());
      }

      private void appendArrayChar(CharBuffer utf16In) {
         assert this.prevHighSurrogate == -1;

         char[] in = utf16In.array();
         int inOffset = utf16In.arrayOffset() + utf16In.position();
         int inLimit = utf16In.arrayOffset() + utf16In.limit();
         char[] outChar = this.charBuffer.array();

         int outOffset;
         for (outOffset = this.charBuffer.arrayOffset() + this.charBuffer.position(); inOffset < inLimit; outOffset++) {
            char c = in[inOffset];
            if (Character.isHighSurrogate(c)) {
               ((Buffer)utf16In).position(inOffset - utf16In.arrayOffset());
               ((Buffer)this.charBuffer).position(outOffset - this.charBuffer.arrayOffset());
               this.charToIntBuffer(utf16In.remaining());
               this.appendArrayInt(utf16In);
               return;
            }

            outChar[outOffset] = c;
            inOffset++;
         }

         ((Buffer)utf16In).position(inOffset - utf16In.arrayOffset());
         ((Buffer)this.charBuffer).position(outOffset - this.charBuffer.arrayOffset());
      }

      private void appendArrayInt(CharBuffer utf16In) {
         char[] in = utf16In.array();
         int inOffset = utf16In.arrayOffset() + utf16In.position();
         int inLimit = utf16In.arrayOffset() + utf16In.limit();
         int[] outInt = this.intBuffer.array();
         int outOffset = this.intBuffer.arrayOffset() + this.intBuffer.position();

         while (inOffset < inLimit) {
            char c = in[inOffset];
            inOffset++;
            if (this.prevHighSurrogate != -1) {
               if (Character.isLowSurrogate(c)) {
                  outInt[outOffset] = Character.toCodePoint((char)this.prevHighSurrogate, c);
                  outOffset++;
                  this.prevHighSurrogate = -1;
               } else {
                  outInt[outOffset] = this.prevHighSurrogate;
                  outOffset++;
                  if (Character.isHighSurrogate(c)) {
                     this.prevHighSurrogate = c & '\uffff';
                  } else {
                     outInt[outOffset] = c & '\uffff';
                     outOffset++;
                     this.prevHighSurrogate = -1;
                  }
               }
            } else if (Character.isHighSurrogate(c)) {
               this.prevHighSurrogate = c & '\uffff';
            } else {
               outInt[outOffset] = c & '\uffff';
               outOffset++;
            }
         }

         if (this.prevHighSurrogate != -1) {
            outInt[outOffset] = this.prevHighSurrogate & 65535;
            outOffset++;
         }

         ((Buffer)utf16In).position(inOffset - utf16In.arrayOffset());
         ((Buffer)this.intBuffer).position(outOffset - this.intBuffer.arrayOffset());
      }

      private void byteToCharBuffer(int toAppend) {
         ((Buffer)this.byteBuffer).flip();
         CharBuffer newBuffer = CharBuffer.allocate(Math.max(this.byteBuffer.remaining() + toAppend, this.byteBuffer.capacity() / 2));

         while (this.byteBuffer.hasRemaining()) {
            newBuffer.put((char)(this.byteBuffer.get() & 255));
         }

         this.type = CodePointBuffer.Type.CHAR;
         this.byteBuffer = null;
         this.charBuffer = newBuffer;
      }

      private void byteToIntBuffer(int toAppend) {
         ((Buffer)this.byteBuffer).flip();
         IntBuffer newBuffer = IntBuffer.allocate(Math.max(this.byteBuffer.remaining() + toAppend, this.byteBuffer.capacity() / 4));

         while (this.byteBuffer.hasRemaining()) {
            newBuffer.put(this.byteBuffer.get() & 255);
         }

         this.type = CodePointBuffer.Type.INT;
         this.byteBuffer = null;
         this.intBuffer = newBuffer;
      }

      private void charToIntBuffer(int toAppend) {
         ((Buffer)this.charBuffer).flip();
         IntBuffer newBuffer = IntBuffer.allocate(Math.max(this.charBuffer.remaining() + toAppend, this.charBuffer.capacity() / 2));

         while (this.charBuffer.hasRemaining()) {
            newBuffer.put(this.charBuffer.get() & '\uffff');
         }

         this.type = CodePointBuffer.Type.INT;
         this.charBuffer = null;
         this.intBuffer = newBuffer;
      }
   }

   public static enum Type {
      BYTE,
      CHAR,
      INT;
   }
}
