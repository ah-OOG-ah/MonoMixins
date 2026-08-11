package org.spongepowered.include.com.google.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.common.collect.ImmutableSet;
import org.spongepowered.include.com.google.common.collect.Lists;
import org.spongepowered.include.com.google.common.collect.TreeTraverser;
import org.spongepowered.include.com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class Files {
   private static final TreeTraverser<File> FILE_TREE_TRAVERSER = new TreeTraverser<File>() {
      @Override
      public String toString() {
         return "Files.fileTreeTraverser()";
      }
   };

   public static ByteSource asByteSource(File file) {
      return new Files.FileByteSource(file);
   }

   static byte[] readFile(InputStream in, long expectedSize) throws IOException {
      if (expectedSize > 2147483647L) {
         throw new OutOfMemoryError("file is too large to fit in a byte array: " + expectedSize + " bytes");
      } else {
         return expectedSize == 0L ? ByteStreams.toByteArray(in) : ByteStreams.toByteArray(in, (int)expectedSize);
      }
   }

   public static ByteSink asByteSink(File file, FileWriteMode... modes) {
      return new Files.FileByteSink(file, modes);
   }

   public static CharSource asCharSource(File file, Charset charset) {
      return asByteSource(file).asCharSource(charset);
   }

   public static CharSink asCharSink(File file, Charset charset, FileWriteMode... modes) {
      return asByteSink(file, modes).asCharSink(charset);
   }

   private static FileWriteMode[] modes(boolean append) {
      return append ? new FileWriteMode[]{FileWriteMode.APPEND} : new FileWriteMode[0];
   }

   public static void write(byte[] from, File to) throws IOException {
      asByteSink(to).write(from);
   }

   public static void write(CharSequence from, File to, Charset charset) throws IOException {
      asCharSink(to, charset).write(from);
   }

   public static void append(CharSequence from, File to, Charset charset) throws IOException {
      write(from, to, charset, true);
   }

   private static void write(CharSequence from, File to, Charset charset, boolean append) throws IOException {
      asCharSink(to, charset, modes(append)).write(from);
   }

   public static List<String> readLines(File file, Charset charset) throws IOException {
      return readLines(file, charset, new LineProcessor<List<String>>() {
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

   @CanIgnoreReturnValue
   public static <T> T readLines(File file, Charset charset, LineProcessor<T> callback) throws IOException {
      return asCharSource(file, charset).readLines(callback);
   }

   private static final class FileByteSink extends ByteSink {
      private final File file;
      private final ImmutableSet<FileWriteMode> modes;

      private FileByteSink(File file, FileWriteMode... modes) {
         this.file = Preconditions.checkNotNull(file);
         this.modes = ImmutableSet.copyOf(modes);
      }

      public FileOutputStream openStream() throws IOException {
         return new FileOutputStream(this.file, this.modes.contains(FileWriteMode.APPEND));
      }

      @Override
      public String toString() {
         return "Files.asByteSink(" + this.file + ", " + this.modes + ")";
      }
   }

   private static final class FileByteSource extends ByteSource {
      private final File file;

      private FileByteSource(File file) {
         this.file = Preconditions.checkNotNull(file);
      }

      public FileInputStream openStream() throws IOException {
         return new FileInputStream(this.file);
      }

      @Override
      public byte[] read() throws IOException {
         Closer closer = Closer.create();

         byte[] var3;
         try {
            FileInputStream in = closer.register(this.openStream());
            var3 = Files.readFile(in, in.getChannel().size());
         } catch (Throwable var7) {
            throw closer.rethrow(var7);
         } finally {
            closer.close();
         }

         return var3;
      }

      @Override
      public String toString() {
         return "Files.asByteSource(" + this.file + ")";
      }
   }
}
