package org.spongepowered.libraries.com.google.common.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
@GwtIncompatible
public final class CountingOutputStream extends FilterOutputStream {
   private long count;

   public CountingOutputStream(OutputStream out) {
      super(Preconditions.checkNotNull(out));
   }

   public long getCount() {
      return this.count;
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.out.write(b, off, len);
      this.count += len;
   }

   @Override
   public void write(int b) throws IOException {
      this.out.write(b);
      this.count++;
   }

   @Override
   public void close() throws IOException {
      this.out.close();
   }
}
