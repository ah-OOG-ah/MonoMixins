package org.spongepowered.libraries.com.google.common.cache;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface Weigher<K, V> {
   int weigh(K var1, V var2);
}
