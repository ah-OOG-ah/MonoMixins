package org.spongepowered.libraries.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.MoreObjects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.Lists;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtIncompatible
public final class Resources {
   private Resources() {
   }

   public static ByteSource asByteSource(URL url) {
      return new Resources.UrlByteSource(url);
   }

   public static CharSource asCharSource(URL url, Charset charset) {
      return asByteSource(url).asCharSource(charset);
   }

   public static byte[] toByteArray(URL url) throws IOException {
      return asByteSource(url).read();
   }

   public static String toString(URL url, Charset charset) throws IOException {
      return asCharSource(url, charset).read();
   }

   @CanIgnoreReturnValue
   public static <T> T readLines(URL url, Charset charset, LineProcessor<T> callback) throws IOException {
      return asCharSource(url, charset).readLines(callback);
   }

   public static List<String> readLines(URL url, Charset charset) throws IOException {
      return readLines(url, charset, new LineProcessor<List<String>>() {
         final List<String> result = Lists.newArrayList();

         @Override
         public boolean processLine(String line) {
            this.result.add(line);
            return true;
         }

         public List<String> getResult() {
            return this.result;
         }
      });
   }

   public static void copy(URL from, OutputStream to) throws IOException {
      asByteSource(from).copyTo(to);
   }

   @CanIgnoreReturnValue
   public static URL getResource(String resourceName) {
      ClassLoader loader = MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(), Resources.class.getClassLoader());
      URL url = loader.getResource(resourceName);
      Preconditions.checkArgument(url != null, "resource %s not found.", resourceName);
      return url;
   }

   public static URL getResource(Class<?> contextClass, String resourceName) {
      URL url = contextClass.getResource(resourceName);
      Preconditions.checkArgument(url != null, "resource %s relative to %s not found.", resourceName, contextClass.getName());
      return url;
   }

   private static final class UrlByteSource extends ByteSource {
      private final URL url;

      private UrlByteSource(URL url) {
         this.url = Preconditions.checkNotNull(url);
      }

      @Override
      public InputStream openStream() throws IOException {
         return this.url.openStream();
      }

      @Override
      public String toString() {
         return "Resources.asByteSource(" + this.url + ")";
      }
   }
}
