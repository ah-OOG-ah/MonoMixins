package org.spongepowered.libraries.com.google.common.io;

import java.io.IOException;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtIncompatible
public interface ByteProcessor<T> {
   @CanIgnoreReturnValue
   boolean processBytes(byte[] var1, int var2, int var3) throws IOException;

   T getResult();
}
