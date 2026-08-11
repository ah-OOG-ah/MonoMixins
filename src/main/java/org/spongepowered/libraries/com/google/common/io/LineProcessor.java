package org.spongepowered.libraries.com.google.common.io;

import java.io.IOException;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
@GwtIncompatible
public interface LineProcessor<T> {
   @CanIgnoreReturnValue
   boolean processLine(String var1) throws IOException;

   T getResult();
}
