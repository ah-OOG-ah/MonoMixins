package org.spongepowered.libraries.com.google.common.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible(emulated = true)
public final class EnumHashBiMap<K extends Enum<K>, V> extends AbstractBiMap<K, V> {
   private transient Class<K> keyType;
   @GwtIncompatible
   private static final long serialVersionUID = 0L;

   public static <K extends Enum<K>, V> EnumHashBiMap<K, V> create(Class<K> keyType) {
      return new EnumHashBiMap<>(keyType);
   }

   public static <K extends Enum<K>, V> EnumHashBiMap<K, V> create(Map<K, ? extends V> map) {
      EnumHashBiMap<K, V> bimap = create(EnumBiMap.inferKeyType(map));
      bimap.putAll(map);
      return bimap;
   }

   private EnumHashBiMap(Class<K> keyType) {
      super(WellBehavedMap.wrap(new EnumMap<>(keyType)), Maps.newHashMapWithExpectedSize(((Enum[])keyType.getEnumConstants()).length));
      this.keyType = keyType;
   }

   K checkKey(K key) {
      return Preconditions.checkNotNull(key);
   }

   @CanIgnoreReturnValue
   public V put(K key, @Nullable V value) {
      return super.put(key, value);
   }

   @CanIgnoreReturnValue
   public V forcePut(K key, @Nullable V value) {
      return super.forcePut(key, value);
   }

   public Class<K> keyType() {
      return this.keyType;
   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.keyType);
      Serialization.writeMap(this, stream);
   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.keyType = (Class<K>)stream.readObject();
      this.setDelegates(WellBehavedMap.wrap(new EnumMap<>(this.keyType)), new HashMap<>(((Enum[])this.keyType.getEnumConstants()).length * 3 / 2));
      Serialization.populateMap(this, stream);
   }
}
