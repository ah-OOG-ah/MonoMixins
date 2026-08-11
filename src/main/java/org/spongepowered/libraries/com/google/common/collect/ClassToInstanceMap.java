package org.spongepowered.libraries.com.google.common.collect;

import java.util.Map;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {
   @CanIgnoreReturnValue
   <T extends B> T getInstance(Class<T> var1);

   @CanIgnoreReturnValue
   <T extends B> T putInstance(Class<T> var1, @Nullable T var2);
}
