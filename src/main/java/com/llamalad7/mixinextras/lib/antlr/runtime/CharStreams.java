package com.llamalad7.mixinextras.lib.antlr.runtime;

import java.nio.Buffer;
import java.nio.CharBuffer;

public final class CharStreams {
   public static CodePointCharStream fromString(String s) {
      return fromString(s, "<unknown>");
   }

   public static CodePointCharStream fromString(String s, String sourceName) {
      CodePointBuffer.Builder codePointBufferBuilder = CodePointBuffer.builder(s.length());
      CharBuffer cb = CharBuffer.allocate(s.length());
      cb.put(s);
      ((Buffer)cb).flip();
      codePointBufferBuilder.append(cb);
      return CodePointCharStream.fromBuffer(codePointBufferBuilder.build(), sourceName);
   }
}
