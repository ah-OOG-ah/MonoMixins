package org.spongepowered.libraries.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.primitives.UnsignedBytes;

@GwtIncompatible
final class ReaderInputStream extends InputStream {
   private final Reader reader;
   private final CharsetEncoder encoder;
   private final byte[] singleByte = new byte[1];
   private CharBuffer charBuffer;
   private ByteBuffer byteBuffer;
   private boolean endOfInput;
   private boolean draining;
   private boolean doneFlushing;

   ReaderInputStream(Reader reader, Charset charset, int bufferSize) {
      this(reader, charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE), bufferSize);
   }

   ReaderInputStream(Reader reader, CharsetEncoder encoder, int bufferSize) {
      this.reader = Preconditions.checkNotNull(reader);
      this.encoder = Preconditions.checkNotNull(encoder);
      Preconditions.checkArgument(bufferSize > 0, "bufferSize must be positive: %s", bufferSize);
      encoder.reset();
      this.charBuffer = CharBuffer.allocate(bufferSize);
      ((Buffer)this.charBuffer).flip();
      this.byteBuffer = ByteBuffer.allocate(bufferSize);
   }

   @Override
   public void close() throws IOException {
      this.reader.close();
   }

   @Override
   public int read() throws IOException {
      return this.read(this.singleByte) == 1 ? UnsignedBytes.toInt(this.singleByte[0]) : -1;
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      Preconditions.checkPositionIndexes(off, off + len, b.length);
      if (len == 0) {
         return 0;
      } else {
         int totalBytesRead = 0;
         boolean doneEncoding = this.endOfInput;

         while (true) {
            if (this.draining) {
               totalBytesRead += this.drain(b, off + totalBytesRead, len - totalBytesRead);
               if (totalBytesRead == len || this.doneFlushing) {
                  return totalBytesRead > 0 ? totalBytesRead : -1;
               }

               this.draining = false;
               ((Buffer)this.byteBuffer).clear();
            }

            while (true) {
               CoderResult result;
               if (this.doneFlushing) {
                  result = CoderResult.UNDERFLOW;
               } else if (doneEncoding) {
                  result = this.encoder.flush(this.byteBuffer);
               } else {
                  result = this.encoder.encode(this.charBuffer, this.byteBuffer, this.endOfInput);
               }

               if (result.isOverflow()) {
                  this.startDraining(true);
                  break;
               }

               if (result.isUnderflow()) {
                  if (doneEncoding) {
                     this.doneFlushing = true;
                     this.startDraining(false);
                     break;
                  }

                  if (this.endOfInput) {
                     doneEncoding = true;
                  } else {
                     this.readMoreChars();
                  }
               } else if (result.isError()) {
                  result.throwException();
                  return 0;
               }
            }
         }
      }
   }

   private static CharBuffer grow(CharBuffer buf) {
      char[] copy = Arrays.copyOf(buf.array(), buf.capacity() * 2);
      CharBuffer bigger = CharBuffer.wrap(copy);
      ((Buffer)bigger).position(buf.position());
      ((Buffer)bigger).limit(buf.limit());
      return bigger;
   }

   private void readMoreChars() throws IOException {
      if (availableCapacity(this.charBuffer) == 0) {
         if (this.charBuffer.position() > 0) {
            ((Buffer)this.charBuffer.compact()).flip();
         } else {
            this.charBuffer = grow(this.charBuffer);
         }
      }

      int limit = this.charBuffer.limit();
      int numChars = this.reader.read(this.charBuffer.array(), limit, availableCapacity(this.charBuffer));
      if (numChars == -1) {
         this.endOfInput = true;
      } else {
         ((Buffer)this.charBuffer).limit(limit + numChars);
      }
   }

   private static int availableCapacity(Buffer buffer) {
      return buffer.capacity() - buffer.limit();
   }

   private void startDraining(boolean overflow) {
      ((Buffer)this.byteBuffer).flip();
      if (overflow && this.byteBuffer.remaining() == 0) {
         this.byteBuffer = ByteBuffer.allocate(this.byteBuffer.capacity() * 2);
      } else {
         this.draining = true;
      }
   }

   private int drain(byte[] b, int off, int len) {
      int remaining = Math.min(len, this.byteBuffer.remaining());
      this.byteBuffer.get(b, off, remaining);
      return remaining;
   }
}
