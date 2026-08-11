package org.spongepowered.libraries.com.google.common.collect;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.CompatibleWith;

@GwtCompatible
public interface Multimap<K, V> {
   int size();

   boolean isEmpty();

   boolean containsKey(@Nullable @CompatibleWith("K") Object var1);

   boolean containsValue(@Nullable @CompatibleWith("V") Object var1);

   boolean containsEntry(@Nullable @CompatibleWith("K") Object var1, @Nullable @CompatibleWith("V") Object var2);

   @CanIgnoreReturnValue
   boolean put(@Nullable K var1, @Nullable V var2);

   @CanIgnoreReturnValue
   boolean remove(@Nullable @CompatibleWith("K") Object var1, @Nullable @CompatibleWith("V") Object var2);

   @CanIgnoreReturnValue
   boolean putAll(@Nullable K var1, Iterable<? extends V> var2);

   @CanIgnoreReturnValue
   boolean putAll(Multimap<? extends K, ? extends V> var1);

   @CanIgnoreReturnValue
   Collection<V> replaceValues(@Nullable K var1, Iterable<? extends V> var2);

   @CanIgnoreReturnValue
   Collection<V> removeAll(@Nullable @CompatibleWith("K") Object var1);

   void clear();

   Collection<V> get(@Nullable K var1);

   Set<K> keySet();

   Multiset<K> keys();

   Collection<V> values();

   Collection<Entry<K, V>> entries();

   default void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.entries().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
   }

   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@Nullable Object var1);

   @Override
   int hashCode();
}
