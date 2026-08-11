package org.spongepowered.libraries.com.google.common.collect;

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@Beta
@GwtIncompatible
public interface RangeMap<K extends Comparable, V> {
   @Nullable
   V get(K var1);

   @Nullable
   Entry<Range<K>, V> getEntry(K var1);

   Range<K> span();

   void put(Range<K> var1, V var2);

   void putAll(RangeMap<K, V> var1);

   void clear();

   void remove(Range<K> var1);

   Map<Range<K>, V> asMapOfRanges();

   Map<Range<K>, V> asDescendingMapOfRanges();

   RangeMap<K, V> subRangeMap(Range<K> var1);

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();

   @Override
   String toString();
}
