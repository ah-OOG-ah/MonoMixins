package org.spongepowered.include.com.google.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.spongepowered.include.com.google.common.base.Preconditions;

public abstract class ByteSink {
   protected ByteSink() {
   }

   public CharSink asCharSink(Charset charset) {
      return new ByteSink.AsCharSink(charset);
   }

   public abstract OutputStream openStream() throws IOException;

   public void write(byte[] bytes) throws IOException {
      Preconditions.checkNotNull(bytes);
      Closer closer = Closer.create();

      try {
         OutputStream out = closer.register(this.openStream());
         out.write(bytes);
         out.flush();
      } catch (Throwable var7) {
         throw closer.rethrow(var7);
      } finally {
         closer.close();
      }
   }

   private final class AsCharSink extends CharSink {
      private final Charset charset;

      private AsCharSink(Charset charset) {
         this.charset = Preconditions.checkNotNull(charset);
      }

      @Override
      public Writer openStream() throws IOException {
         return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
      }

      @Override
      public String toString() {
         return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
      }
   }
}
