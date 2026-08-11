package org.spongepowered.libraries.com.google.common.collect;

import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Predicate;

@GwtCompatible
final class FilteredKeyListMultimap<K, V> extends FilteredKeyMultimap<K, V> implements ListMultimap<K, V> {
   FilteredKeyListMultimap(ListMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      super(unfiltered, keyPredicate);
   }

   public ListMultimap<K, V> unfiltered() {
      return (ListMultimap<K, V>)super.unfiltered();
   }

   @Override
   public List<V> get(K key) {
      return (List<V>)super.get(key);
   }

   @Override
   public List<V> removeAll(@Nullable Object key) {
      return (List<V>)super.removeAll(key);
   }

   @Override
   public List<V> replaceValues(K key, Iterable<? extends V> values) {
      return (List<V>)super.replaceValues(key, values);
   }
}
