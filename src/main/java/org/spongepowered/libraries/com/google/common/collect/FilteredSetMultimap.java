package org.spongepowered.libraries.com.google.common.collect;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
interface FilteredSetMultimap<K, V> extends FilteredMultimap<K, V>, SetMultimap<K, V> {
   SetMultimap<K, V> unfiltered();
}
