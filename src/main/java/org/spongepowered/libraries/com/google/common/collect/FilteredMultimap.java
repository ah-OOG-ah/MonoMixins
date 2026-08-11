package org.spongepowered.libraries.com.google.common.collect;

import java.util.Map.Entry;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Predicate;

@GwtCompatible
interface FilteredMultimap<K, V> extends Multimap<K, V> {
   Multimap<K, V> unfiltered();

   Predicate<? super Entry<K, V>> entryPredicate();
}
