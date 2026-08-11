package org.spongepowered.libraries.com.google.common.io;

import java.io.DataInput;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtIncompatible
public interface ByteArrayDataInput extends DataInput {
   @Override
   void readFully(byte[] var1);

   @Override
   void readFully(byte[] var1, int var2, int var3);

   @Override
   int skipBytes(int var1);

   @CanIgnoreReturnValue
   @Override
   boolean readBoolean();

   @CanIgnoreReturnValue
   @Override
   byte readByte();

   @CanIgnoreReturnValue
   @Override
   int readUnsignedByte();

   @CanIgnoreReturnValue
   @Override
   short readShort();

   @CanIgnoreReturnValue
   @Override
   int readUnsignedShort();

   @CanIgnoreReturnValue
   @Override
   char readChar();

   @CanIgnoreReturnValue
   @Override
   int readInt();

   @CanIgnoreReturnValue
   @Override
   long readLong();

   @CanIgnoreReturnValue
   @Override
   float readFloat();

   @CanIgnoreReturnValue
   @Override
   double readDouble();

   @CanIgnoreReturnValue
   @Override
   String readLine();

   @CanIgnoreReturnValue
   @Override
   String readUTF();
}
