package org.spongepowered.libraries.com.google.common.collect;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.spongepowered.libraries.com.google.errorprone.annotations.concurrent.LazyInit;
import org.spongepowered.libraries.com.google.j2objc.annotations.RetainedWith;

@GwtCompatible(serializable = true, emulated = true)
public class ImmutableListMultimap<K, V> extends ImmutableMultimap<K, V> implements ListMultimap<K, V> {
   @LazyInit
   @RetainedWith
   private transient ImmutableListMultimap<V, K> inverse;
   @GwtIncompatible
   private static final long serialVersionUID = 0L;

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction, "keyFunction");
      Preconditions.checkNotNull(valueFunction, "valueFunction");
      return Collector.of(
         ImmutableListMultimap::builder,
         (builder, t) -> builder.put((K)keyFunction.apply(t), (V)valueFunction.apply(t)),
         ImmutableListMultimap.Builder::combine,
         ImmutableListMultimap.Builder::build
      );
   }

   @Beta
   public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> flatteningToImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valuesFunction);
      return Collectors.collectingAndThen(
         Multimaps.flatteningToMultimap(
            input -> Preconditions.checkNotNull(keyFunction.apply(input)),
            input -> valuesFunction.apply(input).peek(Preconditions::checkNotNull),
            MultimapBuilder.linkedHashKeys().arrayListValues()::build
         ),
         ImmutableListMultimap::copyOf
      );
   }

   public static <K, V> ImmutableListMultimap<K, V> of() {
      return EmptyImmutableListMultimap.INSTANCE;
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      ImmutableListMultimap.Builder<K, V> builder = builder();
      builder.put(k1, v1);
      builder.put(k2, v2);
      builder.put(k3, v3);
      builder.put(k4, v4);
      builder.put(k5, v5);
      return builder.build();
   }

   public static <K, V> ImmutableListMultimap.Builder<K, V> builder() {
      return new ImmutableListMultimap.Builder<>();
   }

   public static <K, V> ImmutableListMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
      if (multimap.isEmpty()) {
         return of();
      } else {
         if (multimap instanceof ImmutableListMultimap) {
            ImmutableListMultimap<K, V> kvMultimap = (ImmutableListMultimap<K, V>)multimap;
            if (!kvMultimap.isPartialView()) {
               return kvMultimap;
            }
         }

         ImmutableMap.Builder<K, ImmutableList<V>> builder = new ImmutableMap.Builder<>(multimap.asMap().size());
         int size = 0;

         for (Entry<? extends K, ? extends Collection<? extends V>> entry : multimap.asMap().entrySet()) {
            ImmutableList<V> list = ImmutableList.copyOf((Collection<? extends V>)entry.getValue());
            if (!list.isEmpty()) {
               builder.put((K)entry.getKey(), list);
               size += list.size();
            }
         }

         return new ImmutableListMultimap<>(builder.build(), size);
      }
   }

   @Beta
   public static <K, V> ImmutableListMultimap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      return new ImmutableListMultimap.Builder<K, V>().putAll(entries).build();
   }

   ImmutableListMultimap(ImmutableMap<K, ImmutableList<V>> map, int size) {
      super(map, size);
   }

   public ImmutableList<V> get(@Nullable K key) {
      ImmutableList<V> list = (ImmutableList<V>)this.map.get(key);
      return list == null ? ImmutableList.of() : list;
   }

   public ImmutableListMultimap<V, K> inverse() {
      ImmutableListMultimap<V, K> result = this.inverse;
      return result == null ? (this.inverse = this.invert()) : result;
   }

   private ImmutableListMultimap<V, K> invert() {
      ImmutableListMultimap.Builder<V, K> builder = builder();

      for (Entry<K, V> entry : this.entries()) {
         builder.put(entry.getValue(), entry.getKey());
      }

      ImmutableListMultimap<V, K> invertedMultimap = builder.build();
      invertedMultimap.inverse = this;
      return invertedMultimap;
   }

   @Deprecated
   @CanIgnoreReturnValue
   public ImmutableList<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   public ImmutableList<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      Serialization.writeMultimap(this, stream);
   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      int keyCount = stream.readInt();
      if (keyCount < 0) {
         throw new InvalidObjectException("Invalid key count " + keyCount);
      } else {
         ImmutableMap.Builder<Object, ImmutableList<Object>> builder = ImmutableMap.builder();
         int tmpSize = 0;

         for (int i = 0; i < keyCount; i++) {
            Object key = stream.readObject();
            int valueCount = stream.readInt();
            if (valueCount <= 0) {
               throw new InvalidObjectException("Invalid value count " + valueCount);
            }

            ImmutableList.Builder<Object> valuesBuilder = ImmutableList.builder();

            for (int j = 0; j < valueCount; j++) {
               valuesBuilder.add(stream.readObject());
            }

            builder.put(key, valuesBuilder.build());
            tmpSize += valueCount;
         }

         ImmutableMap<Object, ImmutableList<Object>> tmpMap;
         try {
            tmpMap = builder.build();
         } catch (IllegalArgumentException var10) {
            throw (InvalidObjectException)new InvalidObjectException(var10.getMessage()).initCause(var10);
         }

         ImmutableMultimap.FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
         ImmutableMultimap.FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
      }
   }

   public static final class Builder<K, V> extends ImmutableMultimap.Builder<K, V> {
      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> put(K key, V value) {
         super.put(key, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         super.put(entry);
         return this;
      }

      @CanIgnoreReturnValue
      @Beta
      public ImmutableListMultimap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         super.putAll(entries);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(K key, Iterable<? extends V> values) {
         super.putAll(key, values);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(K key, V... values) {
         super.putAll(key, values);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
         super.putAll(multimap);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableListMultimap.Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
         super.combine(other);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
         super.orderKeysBy(keyComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableListMultimap.Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
         super.orderValuesBy(valueComparator);
         return this;
      }

      public ImmutableListMultimap<K, V> build() {
         return (ImmutableListMultimap<K, V>)super.build();
      }
   }
}
