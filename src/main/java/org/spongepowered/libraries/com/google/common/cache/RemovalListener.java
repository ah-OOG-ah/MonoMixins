package org.spongepowered.libraries.com.google.common.cache;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface RemovalListener<K, V> {
   void onRemoval(RemovalNotification<K, V> var1);
}
