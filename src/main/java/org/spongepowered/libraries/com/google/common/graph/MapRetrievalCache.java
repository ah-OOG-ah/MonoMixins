package org.spongepowered.libraries.com.google.common.graph;

import java.util.Map;
import javax.annotation.Nullable;

class MapRetrievalCache<K, V> extends MapIteratorCache<K, V> {
   @Nullable
   private transient MapRetrievalCache.CacheEntry<K, V> cacheEntry1;
   @Nullable
   private transient MapRetrievalCache.CacheEntry<K, V> cacheEntry2;

   MapRetrievalCache(Map<K, V> backingMap) {
      super(backingMap);
   }

   @Override
   public V get(@Nullable Object key) {
      V value = this.getIfCached(key);
      if (value != null) {
         return value;
      } else {
         value = this.getWithoutCaching(key);
         if (value != null) {
            this.addToCache((K)key, value);
         }

         return value;
      }
   }

   @Override
   protected V getIfCached(@Nullable Object key) {
      V value = super.getIfCached(key);
      if (value != null) {
         return value;
      } else {
         MapRetrievalCache.CacheEntry<K, V> entry = this.cacheEntry1;
         if (entry != null && entry.key == key) {
            return entry.value;
         } else {
            entry = this.cacheEntry2;
            if (entry != null && entry.key == key) {
               this.addToCache(entry);
               return entry.value;
            } else {
               return null;
            }
         }
      }
   }

   @Override
   protected void clearCache() {
      super.clearCache();
      this.cacheEntry1 = null;
      this.cacheEntry2 = null;
   }

   private void addToCache(K key, V value) {
      this.addToCache(new MapRetrievalCache.CacheEntry<>(key, value));
   }

   private void addToCache(MapRetrievalCache.CacheEntry<K, V> entry) {
      this.cacheEntry2 = this.cacheEntry1;
      this.cacheEntry1 = entry;
   }

   private static final class CacheEntry<K, V> {
      final K key;
      final V value;

      CacheEntry(K key, V value) {
         this.key = key;
         this.value = value;
      }
   }
}
