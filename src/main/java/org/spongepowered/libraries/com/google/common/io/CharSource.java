package org.spongepowered.libraries.com.google.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Ascii;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Splitter;
import org.spongepowered.libraries.com.google.common.collect.AbstractIterator;
import org.spongepowered.libraries.com.google.common.collect.ImmutableList;
import org.spongepowered.libraries.com.google.common.collect.Lists;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtIncompatible
public abstract class CharSource {
   protected CharSource() {
   }

   @Beta
   public ByteSource asByteSource(Charset charset) {
      return new CharSource.AsByteSource(charset);
   }

   public abstract Reader openStream() throws IOException;

   public BufferedReader openBufferedStream() throws IOException {
      Reader reader = this.openStream();
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
   }

   @Beta
   public Optional<Long> lengthIfKnown() {
      return Optional.absent();
   }

   @Beta
   public long length() throws IOException {
      Optional<Long> lengthIfKnown = this.lengthIfKnown();
      if (lengthIfKnown.isPresent()) {
         return lengthIfKnown.get();
      } else {
         Closer closer = Closer.create();

         long var4;
         try {
            Reader reader = closer.register(this.openStream());
            var4 = this.countBySkipping(reader);
         } catch (Throwable var9) {
            throw closer.rethrow(var9);
         } finally {
            closer.close();
         }

         return var4;
      }
   }

   private long countBySkipping(Reader reader) throws IOException {
      long count = 0L;

      long read;
      while ((read = reader.skip(Long.MAX_VALUE)) != 0L) {
         count += read;
      }

      return count;
   }

   @CanIgnoreReturnValue
   public long copyTo(Appendable appendable) throws IOException {
      Preconditions.checkNotNull(appendable);
      Closer closer = Closer.create();

      long var4;
      try {
         Reader reader = closer.register(this.openStream());
         var4 = CharStreams.copy(reader, appendable);
      } catch (Throwable var9) {
         throw closer.rethrow(var9);
      } finally {
         closer.close();
      }

      return var4;
   }

   @CanIgnoreReturnValue
   public long copyTo(CharSink sink) throws IOException {
      Preconditions.checkNotNull(sink);
      Closer closer = Closer.create();

      long var5;
      try {
         Reader reader = closer.register(this.openStream());
         Writer writer = closer.register(sink.openStream());
         var5 = CharStreams.copy(reader, writer);
      } catch (Throwable var10) {
         throw closer.rethrow(var10);
      } finally {
         closer.close();
      }

      return var5;
   }

   public String read() throws IOException {
      Closer closer = Closer.create();

      String var3;
      try {
         Reader reader = closer.register(this.openStream());
         var3 = CharStreams.toString(reader);
      } catch (Throwable var7) {
         throw closer.rethrow(var7);
      } finally {
         closer.close();
      }

      return var3;
   }

   @Nullable
   public String readFirstLine() throws IOException {
      Closer closer = Closer.create();

      String var3;
      try {
         BufferedReader reader = closer.register(this.openBufferedStream());
         var3 = reader.readLine();
      } catch (Throwable var7) {
         throw closer.rethrow(var7);
      } finally {
         closer.close();
      }

      return var3;
   }

   public ImmutableList<String> readLines() throws IOException {
      Closer closer = Closer.create();

      ImmutableList var5;
      try {
         BufferedReader reader = closer.register(this.openBufferedStream());
         List<String> result = Lists.newArrayList();

         String line;
         while ((line = reader.readLine()) != null) {
            result.add(line);
         }

         var5 = ImmutableList.copyOf(result);
      } catch (Throwable var9) {
         throw closer.rethrow(var9);
      } finally {
         closer.close();
      }

      return var5;
   }

   @Beta
   @CanIgnoreReturnValue
   public <T> T readLines(LineProcessor<T> processor) throws IOException {
      Preconditions.checkNotNull(processor);
      Closer closer = Closer.create();

      Object var4;
      try {
         Reader reader = closer.register(this.openStream());
         var4 = CharStreams.readLines(reader, processor);
      } catch (Throwable var8) {
         throw closer.rethrow(var8);
      } finally {
         closer.close();
      }

      return (T)var4;
   }

   public boolean isEmpty() throws IOException {
      Optional<Long> lengthIfKnown = this.lengthIfKnown();
      if (lengthIfKnown.isPresent() && lengthIfKnown.get() == 0L) {
         return true;
      } else {
         Closer closer = Closer.create();

         boolean var4;
         try {
            Reader reader = closer.register(this.openStream());
            var4 = reader.read() == -1;
         } catch (Throwable var8) {
            throw closer.rethrow(var8);
         } finally {
            closer.close();
         }

         return var4;
      }
   }

   public static CharSource concat(Iterable<? extends CharSource> sources) {
      return new CharSource.ConcatenatedCharSource(sources);
   }

   public static CharSource concat(Iterator<? extends CharSource> sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static CharSource concat(CharSource... sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static CharSource wrap(CharSequence charSequence) {
      return new CharSource.CharSequenceCharSource(charSequence);
   }

   public static CharSource empty() {
      return CharSource.EmptyCharSource.INSTANCE;
   }

   private final class AsByteSource extends ByteSource {
      final Charset charset;

      AsByteSource(Charset charset) {
         this.charset = Preconditions.checkNotNull(charset);
      }

      @Override
      public CharSource asCharSource(Charset charset) {
         return charset.equals(this.charset) ? CharSource.this : super.asCharSource(charset);
      }

      @Override
      public InputStream openStream() throws IOException {
         return new ReaderInputStream(CharSource.this.openStream(), this.charset, 8192);
      }

      @Override
      public String toString() {
         return CharSource.this.toString() + ".asByteSource(" + this.charset + ")";
      }
   }

   private static class CharSequenceCharSource extends CharSource {
      private static final Splitter LINE_SPLITTER = Splitter.onPattern("\r\n|\n|\r");
      private final CharSequence seq;

      protected CharSequenceCharSource(CharSequence seq) {
         this.seq = Preconditions.checkNotNull(seq);
      }

      @Override
      public Reader openStream() {
         return new CharSequenceReader(this.seq);
      }

      @Override
      public String read() {
         return this.seq.toString();
      }

      @Override
      public boolean isEmpty() {
         return this.seq.length() == 0;
      }

      @Override
      public long length() {
         return this.seq.length();
      }

      @Override
      public Optional<Long> lengthIfKnown() {
         return Optional.of((long)this.seq.length());
      }

      private Iterable<String> lines() {
         return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
               return new AbstractIterator<String>() {
                  Iterator<String> lines = CharSource.CharSequenceCharSource.LINE_SPLITTER.split(CharSequenceCharSource.this.seq).iterator();

                  protected String computeNext() {
                     if (this.lines.hasNext()) {
                        String next = this.lines.next();
                        if (this.lines.hasNext() || !next.isEmpty()) {
                           return next;
                        }
                     }

                     return this.endOfData();
                  }
               };
            }
         };
      }

      @Override
      public String readFirstLine() {
         Iterator<String> lines = this.lines().iterator();
         return lines.hasNext() ? lines.next() : null;
      }

      @Override
      public ImmutableList<String> readLines() {
         return ImmutableList.copyOf(this.lines());
      }

      @Override
      public <T> T readLines(LineProcessor<T> processor) throws IOException {
         for (String line : this.lines()) {
            if (!processor.processLine(line)) {
               break;
            }
         }

         return processor.getResult();
      }

      @Override
      public String toString() {
         return "CharSource.wrap(" + Ascii.truncate(this.seq, 30, "...") + ")";
      }
   }

   private static final class ConcatenatedCharSource extends CharSource {
      private final Iterable<? extends CharSource> sources;

      ConcatenatedCharSource(Iterable<? extends CharSource> sources) {
         this.sources = Preconditions.checkNotNull(sources);
      }

      @Override
      public Reader openStream() throws IOException {
         return new MultiReader(this.sources.iterator());
      }

      @Override
      public boolean isEmpty() throws IOException {
         for (CharSource source : this.sources) {
            if (!source.isEmpty()) {
               return false;
            }
         }

         return true;
      }

      @Override
      public Optional<Long> lengthIfKnown() {
         long result = 0L;

         for (CharSource source : this.sources) {
            Optional<Long> lengthIfKnown = source.lengthIfKnown();
            if (!lengthIfKnown.isPresent()) {
               return Optional.absent();
            }

            result += lengthIfKnown.get();
         }

         return Optional.of(result);
      }

      @Override
      public long length() throws IOException {
         long result = 0L;

         for (CharSource source : this.sources) {
            result += source.length();
         }

         return result;
      }

      @Override
      public String toString() {
         return "CharSource.concat(" + this.sources + ")";
      }
   }

   private static final class EmptyCharSource extends CharSource.CharSequenceCharSource {
      private static final CharSource.EmptyCharSource INSTANCE = new CharSource.EmptyCharSource();

      private EmptyCharSource() {
         super("");
      }

      @Override
      public String toString() {
         return "CharSource.empty()";
      }
   }
}
