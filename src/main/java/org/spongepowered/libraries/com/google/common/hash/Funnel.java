package org.spongepowered.libraries.com.google.common.hash;

import java.io.Serializable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;

@Beta
public interface Funnel<T> extends Serializable {
   void funnel(T var1, PrimitiveSink var2);
}
