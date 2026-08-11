package org.spongepowered.libraries.com.google.common.hash;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.spongepowered.libraries.com.google.common.primitives.Longs;
import sun.misc.Unsafe;

final class LittleEndianByteArray {
   private static final LittleEndianByteArray.LittleEndianBytes byteArray;

   static long load64(byte[] input, int offset) {
      assert input.length >= offset + 8;

      return byteArray.getLongLittleEndian(input, offset);
   }

   static long load64Safely(byte[] input, int offset, int length) {
      long result = 0L;
      int limit = Math.min(length, 8);

      for (int i = 0; i < limit; i++) {
         result |= (input[offset + i] & 255L) << i * 8;
      }

      return result;
   }

   static void store64(byte[] sink, int offset, long value) {
      assert offset >= 0 && offset + 8 <= sink.length;

      byteArray.putLongLittleEndian(sink, offset, value);
   }

   static int load32(byte[] source, int offset) {
      return source[offset] & 0xFF | (source[offset + 1] & 0xFF) << 8 | (source[offset + 2] & 0xFF) << 16 | (source[offset + 3] & 0xFF) << 24;
   }

   static boolean usingUnsafe() {
      return byteArray instanceof LittleEndianByteArray.UnsafeByteArray;
   }

   private LittleEndianByteArray() {
   }

   static {
      LittleEndianByteArray.LittleEndianBytes theGetter = LittleEndianByteArray.JavaLittleEndianBytes.INSTANCE;

      try {
         String arch = System.getProperty("os.arch");
         if ("amd64".equals(arch) || "aarch64".equals(arch)) {
            theGetter = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)
               ? LittleEndianByteArray.UnsafeByteArray.UNSAFE_LITTLE_ENDIAN
               : LittleEndianByteArray.UnsafeByteArray.UNSAFE_BIG_ENDIAN;
         }
      } catch (Throwable var2) {
      }

      byteArray = theGetter;
   }

   private static enum JavaLittleEndianBytes implements LittleEndianByteArray.LittleEndianBytes {
      INSTANCE {
         @Override
         public long getLongLittleEndian(byte[] source, int offset) {
            return Longs.fromBytes(
               source[offset + 7],
               source[offset + 6],
               source[offset + 5],
               source[offset + 4],
               source[offset + 3],
               source[offset + 2],
               source[offset + 1],
               source[offset]
            );
         }

         @Override
         public void putLongLittleEndian(byte[] sink, int offset, long value) {
            long mask = 255L;

            for (int i = 0; i < 8; i++) {
               sink[offset + i] = (byte)((value & mask) >> i * 8);
               mask <<= 8;
            }
         }
      };

      private JavaLittleEndianBytes() {
      }
   }

   private interface LittleEndianBytes {
      long getLongLittleEndian(byte[] var1, int var2);

      void putLongLittleEndian(byte[] var1, int var2, long var3);
   }

   private static enum UnsafeByteArray implements LittleEndianByteArray.LittleEndianBytes {
      UNSAFE_LITTLE_ENDIAN {
         @Override
         public long getLongLittleEndian(byte[] array, int offset) {
            return LittleEndianByteArray.UnsafeByteArray.theUnsafe.getLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET);
         }

         @Override
         public void putLongLittleEndian(byte[] array, int offset, long value) {
            LittleEndianByteArray.UnsafeByteArray.theUnsafe.putLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET, value);
         }
      },
      UNSAFE_BIG_ENDIAN {
         @Override
         public long getLongLittleEndian(byte[] array, int offset) {
            long bigEndian = LittleEndianByteArray.UnsafeByteArray.theUnsafe
               .getLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET);
            return Long.reverseBytes(bigEndian);
         }

         @Override
         public void putLongLittleEndian(byte[] array, int offset, long value) {
            long littleEndianValue = Long.reverseBytes(value);
            LittleEndianByteArray.UnsafeByteArray.theUnsafe
               .putLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET, littleEndianValue);
         }
      };

      private static final Unsafe theUnsafe = getUnsafe();
      private static final int BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

      private UnsafeByteArray() {
      }

      private static Unsafe getUnsafe() {
         try {
            return Unsafe.getUnsafe();
         } catch (SecurityException var2) {
            try {
               return AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                  public Unsafe run() throws Exception {
                     Class<Unsafe> k = Unsafe.class;

                     for (Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                           return k.cast(x);
                        }
                     }

                     throw new NoSuchFieldError("the Unsafe");
                  }
               });
            } catch (PrivilegedActionException var1) {
               throw new RuntimeException("Could not initialize intrinsics", var1.getCause());
            }
         }
      }

      static {
         if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
            throw new AssertionError();
         }
      }
   }
}
