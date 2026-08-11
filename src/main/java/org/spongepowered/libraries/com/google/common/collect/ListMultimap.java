package org.spongepowered.libraries.com.google.common.collect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V> {
   List<V> get(@Nullable K var1);

   @CanIgnoreReturnValue
   List<V> removeAll(@Nullable Object var1);

   @CanIgnoreReturnValue
   List<V> replaceValues(K var1, Iterable<? extends V> var2);

   @Override
   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@Nullable Object var1);
}
