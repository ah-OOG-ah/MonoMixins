package org.spongepowered.libraries.com.google.common.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Objects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.j2objc.annotations.RetainedWith;

@GwtCompatible(emulated = true)
abstract class AbstractBiMap<K, V> extends ForwardingMap<K, V> implements BiMap<K, V>, Serializable {
   private transient Map<K, V> delegate;
   @RetainedWith
   transient AbstractBiMap<V, K> inverse;
   private transient Set<K> keySet;
   private transient Set<V> valueSet;
   private transient Set<Entry<K, V>> entrySet;
   @GwtIncompatible
   private static final long serialVersionUID = 0L;

   AbstractBiMap(Map<K, V> forward, Map<V, K> backward) {
      this.setDelegates(forward, backward);
   }

   private AbstractBiMap(Map<K, V> backward, AbstractBiMap<V, K> forward) {
      this.delegate = backward;
      this.inverse = forward;
   }

   @Override
   protected Map<K, V> delegate() {
      return this.delegate;
   }

   @CanIgnoreReturnValue
   K checkKey(@Nullable K key) {
      return key;
   }

   @CanIgnoreReturnValue
   V checkValue(@Nullable V value) {
      return value;
   }

   void setDelegates(Map<K, V> forward, Map<V, K> backward) {
      Preconditions.checkState(this.delegate == null);
      Preconditions.checkState(this.inverse == null);
      Preconditions.checkArgument(forward.isEmpty());
      Preconditions.checkArgument(backward.isEmpty());
      Preconditions.checkArgument(forward != backward);
      this.delegate = forward;
      this.inverse = this.makeInverse(backward);
   }

   AbstractBiMap<V, K> makeInverse(Map<V, K> backward) {
      return new AbstractBiMap.Inverse<>(backward, this);
   }

   void setInverse(AbstractBiMap<V, K> inverse) {
      this.inverse = inverse;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.inverse.containsKey(value);
   }

   @CanIgnoreReturnValue
   @Override
   public V put(@Nullable K key, @Nullable V value) {
      return this.putInBothMaps(key, value, false);
   }

   @CanIgnoreReturnValue
   @Override
   public V forcePut(@Nullable K key, @Nullable V value) {
      return this.putInBothMaps(key, value, true);
   }

   private V putInBothMaps(@Nullable K key, @Nullable V value, boolean force) {
      this.checkKey(key);
      this.checkValue(value);
      boolean containedKey = this.containsKey(key);
      if (containedKey && Objects.equal(value, this.get(key))) {
         return value;
      } else {
         if (force) {
            this.inverse().remove(value);
         } else {
            Preconditions.checkArgument(!this.containsValue(value), "value already present: %s", value);
         }

         V oldValue = this.delegate.put(key, value);
         this.updateInverseMap(key, containedKey, oldValue, value);
         return oldValue;
      }
   }

   private void updateInverseMap(K key, boolean containedKey, V oldValue, V newValue) {
      if (containedKey) {
         this.removeFromInverseMap(oldValue);
      }

      this.inverse.delegate.put(newValue, key);
   }

   @CanIgnoreReturnValue
   @Override
   public V remove(@Nullable Object key) {
      return this.containsKey(key) ? this.removeFromBothMaps(key) : null;
   }

   @CanIgnoreReturnValue
   private V removeFromBothMaps(Object key) {
      V oldValue = this.delegate.remove(key);
      this.removeFromInverseMap(oldValue);
      return oldValue;
   }

   private void removeFromInverseMap(V oldValue) {
      this.inverse.delegate.remove(oldValue);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
         this.put((K)entry.getKey(), (V)entry.getValue());
      }
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      this.delegate.replaceAll(function);
      this.inverse.delegate.clear();
      Entry<K, V> broken = null;
      Iterator<Entry<K, V>> itr = this.delegate.entrySet().iterator();

      while (itr.hasNext()) {
         Entry<K, V> entry = itr.next();
         K k = entry.getKey();
         V v = entry.getValue();
         K conflict = this.inverse.delegate.putIfAbsent(v, k);
         if (conflict != null) {
            broken = entry;
            itr.remove();
         }
      }

      if (broken != null) {
         throw new IllegalArgumentException("value already present: " + broken.getValue());
      }
   }

   @Override
   public void clear() {
      this.delegate.clear();
      this.inverse.delegate.clear();
   }

   @Override
   public BiMap<V, K> inverse() {
      return this.inverse;
   }

   @Override
   public Set<K> keySet() {
      Set<K> result = this.keySet;
      return result == null ? (this.keySet = new AbstractBiMap.KeySet()) : result;
   }

   @Override
   public Set<V> values() {
      Set<V> result = this.valueSet;
      return result == null ? (this.valueSet = new AbstractBiMap.ValueSet()) : result;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> result = this.entrySet;
      return result == null ? (this.entrySet = new AbstractBiMap.EntrySet()) : result;
   }

   Iterator<Entry<K, V>> entrySetIterator() {
      final Iterator<Entry<K, V>> iterator = this.delegate.entrySet().iterator();
      return new Iterator<Entry<K, V>>() {
         Entry<K, V> entry;

         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         public Entry<K, V> next() {
            this.entry = iterator.next();
            return AbstractBiMap.this.new BiMapEntry(this.entry);
         }

         @Override
         public void remove() {
            CollectPreconditions.checkRemove(this.entry != null);
            V value = this.entry.getValue();
            iterator.remove();
            AbstractBiMap.this.removeFromInverseMap(value);
         }
      };
   }

   class BiMapEntry extends ForwardingMapEntry<K, V> {
      private final Entry<K, V> delegate;

      BiMapEntry(Entry<K, V> delegate) {
         this.delegate = delegate;
      }

      @Override
      protected Entry<K, V> delegate() {
         return this.delegate;
      }

      @Override
      public V setValue(V value) {
         Preconditions.checkState(AbstractBiMap.this.entrySet().contains(this), "entry no longer in map");
         if (Objects.equal(value, this.getValue())) {
            return value;
         } else {
            Preconditions.checkArgument(!AbstractBiMap.this.containsValue(value), "value already present: %s", value);
            V oldValue = this.delegate.setValue(value);
            Preconditions.checkState(Objects.equal(value, AbstractBiMap.this.get(this.getKey())), "entry no longer in map");
            AbstractBiMap.this.updateInverseMap((K)this.getKey(), true, oldValue, value);
            return oldValue;
         }
      }
   }

   private class EntrySet extends ForwardingSet<Entry<K, V>> {
      final Set<Entry<K, V>> esDelegate = AbstractBiMap.this.delegate.entrySet();

      private EntrySet() {
      }

      @Override
      protected Set<Entry<K, V>> delegate() {
         return this.esDelegate;
      }

      @Override
      public void clear() {
         AbstractBiMap.this.clear();
      }

      @Override
      public boolean remove(Object object) {
         if (!this.esDelegate.contains(object)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry<?, ?>)object;
            AbstractBiMap.this.inverse.delegate.remove(entry.getValue());
            this.esDelegate.remove(entry);
            return true;
         }
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
         return AbstractBiMap.this.entrySetIterator();
      }

      @Override
      public Object[] toArray() {
         return this.standardToArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.standardToArray(array);
      }

      @Override
      public boolean contains(Object o) {
         return Maps.containsEntryImpl(this.delegate(), o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
         return this.standardContainsAll(c);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         return this.standardRemoveAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         return this.standardRetainAll(c);
      }
   }

   static class Inverse<K, V> extends AbstractBiMap<K, V> {
      @GwtIncompatible
      private static final long serialVersionUID = 0L;

      Inverse(Map<K, V> backward, AbstractBiMap<V, K> forward) {
         super(backward, forward);
      }

      @Override
      K checkKey(K key) {
         return this.inverse.checkValue(key);
      }

      @Override
      V checkValue(V value) {
         return this.inverse.checkKey(value);
      }

      @GwtIncompatible
      private void writeObject(ObjectOutputStream stream) throws IOException {
         stream.defaultWriteObject();
         stream.writeObject(this.inverse());
      }

      @GwtIncompatible
      private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
         stream.defaultReadObject();
         this.setInverse((AbstractBiMap<V, K>)stream.readObject());
      }

      @GwtIncompatible
      Object readResolve() {
         return this.inverse().inverse();
      }
   }

   private class KeySet extends ForwardingSet<K> {
      private KeySet() {
      }

      @Override
      protected Set<K> delegate() {
         return AbstractBiMap.this.delegate.keySet();
      }

      @Override
      public void clear() {
         AbstractBiMap.this.clear();
      }

      @Override
      public boolean remove(Object key) {
         if (!this.contains(key)) {
            return false;
         } else {
            AbstractBiMap.this.removeFromBothMaps(key);
            return true;
         }
      }

      @Override
      public boolean removeAll(Collection<?> keysToRemove) {
         return this.standardRemoveAll(keysToRemove);
      }

      @Override
      public boolean retainAll(Collection<?> keysToRetain) {
         return this.standardRetainAll(keysToRetain);
      }

      @Override
      public Iterator<K> iterator() {
         return Maps.keyIterator(AbstractBiMap.this.entrySet().iterator());
      }
   }

   private class ValueSet extends ForwardingSet<V> {
      final Set<V> valuesDelegate = AbstractBiMap.this.inverse.keySet();

      private ValueSet() {
      }

      @Override
      protected Set<V> delegate() {
         return this.valuesDelegate;
      }

      @Override
      public Iterator<V> iterator() {
         return Maps.valueIterator(AbstractBiMap.this.entrySet().iterator());
      }

      @Override
      public Object[] toArray() {
         return this.standardToArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.standardToArray(array);
      }

      @Override
      public String toString() {
         return this.standardToString();
      }
   }
}
