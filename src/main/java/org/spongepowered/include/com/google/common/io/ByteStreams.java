package org.spongepowered.include.com.google.common.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class ByteStreams {
   private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
      @Override
      public void write(int b) {
      }

      @Override
      public void write(byte[] b) {
         Preconditions.checkNotNull(b);
      }

      @Override
      public void write(byte[] b, int off, int len) {
         Preconditions.checkNotNull(b);
      }

      @Override
      public String toString() {
         return "ByteStreams.nullOutputStream()";
      }
   };

   static byte[] createBuffer() {
      return new byte[8192];
   }

   @CanIgnoreReturnValue
   public static long copy(InputStream from, OutputStream to) throws IOException {
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(to);
      byte[] buf = createBuffer();
      long total = 0L;

      while (true) {
         int r = from.read(buf);
         if (r == -1) {
            return total;
         }

         to.write(buf, 0, r);
         total += r;
      }
   }

   public static byte[] toByteArray(InputStream in) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(32, in.available()));
      copy(in, out);
      return out.toByteArray();
   }

   static byte[] toByteArray(InputStream in, int expectedSize) throws IOException {
      byte[] bytes = new byte[expectedSize];
      int remaining = expectedSize;

      while (remaining > 0) {
         int off = expectedSize - remaining;
         int read = in.read(bytes, off, remaining);
         if (read == -1) {
            return Arrays.copyOf(bytes, off);
         }

         remaining -= read;
      }

      int b = in.read();
      if (b == -1) {
         return bytes;
      } else {
         ByteStreams.FastByteArrayOutputStream out = new ByteStreams.FastByteArrayOutputStream();
         out.write(b);
         copy(in, out);
         byte[] result = new byte[bytes.length + out.size()];
         System.arraycopy(bytes, 0, result, 0, bytes.length);
         out.writeTo(result, bytes.length);
         return result;
      }
   }

   private static final class FastByteArrayOutputStream extends ByteArrayOutputStream {
      private FastByteArrayOutputStream() {
      }

      void writeTo(byte[] b, int off) {
         System.arraycopy(this.buf, 0, b, off, this.count);
      }
   }
}
