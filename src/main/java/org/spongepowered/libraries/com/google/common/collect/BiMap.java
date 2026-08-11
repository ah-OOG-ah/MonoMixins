package org.spongepowered.libraries.com.google.common.collect;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public interface BiMap<K, V> extends Map<K, V> {
   @Nullable
   @CanIgnoreReturnValue
   @Override
   V put(@Nullable K var1, @Nullable V var2);

   @Nullable
   @CanIgnoreReturnValue
   V forcePut(@Nullable K var1, @Nullable V var2);

   @Override
   void putAll(Map<? extends K, ? extends V> var1);

   Set<V> values();

   BiMap<V, K> inverse();
}
