package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.primitives.Primitives;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtIncompatible
public final class MutableClassToInstanceMap<B> extends ForwardingMap<Class<? extends B>, B> implements ClassToInstanceMap<B>, Serializable {
   private final Map<Class<? extends B>, B> delegate;

   public static <B> MutableClassToInstanceMap<B> create() {
      return new MutableClassToInstanceMap<>(new HashMap<>());
   }

   public static <B> MutableClassToInstanceMap<B> create(Map<Class<? extends B>, B> backingMap) {
      return new MutableClassToInstanceMap<>(backingMap);
   }

   private MutableClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
   }

   @Override
   protected Map<Class<? extends B>, B> delegate() {
      return this.delegate;
   }

   private static <B> Entry<Class<? extends B>, B> checkedEntry(final Entry<Class<? extends B>, B> entry) {
      return new ForwardingMapEntry<Class<? extends B>, B>() {
         @Override
         protected Entry<Class<? extends B>, B> delegate() {
            return entry;
         }

         @Override
         public B setValue(B value) {
            return super.setValue(MutableClassToInstanceMap.cast((Class<B>)this.getKey(), value));
         }
      };
   }

   @Override
   public Set<Entry<Class<? extends B>, B>> entrySet() {
      return new ForwardingSet<Entry<Class<? extends B>, B>>() {
         @Override
         protected Set<Entry<Class<? extends B>, B>> delegate() {
            return MutableClassToInstanceMap.this.delegate().entrySet();
         }

         @Override
         public Spliterator<Entry<Class<? extends B>, B>> spliterator() {
            return CollectSpliterators.map(this.delegate().spliterator(), x$0 -> MutableClassToInstanceMap.checkedEntry((Entry<Class<? extends B>, B>)x$0));
         }

         @Override
         public Iterator<Entry<Class<? extends B>, B>> iterator() {
            return new TransformedIterator<Entry<Class<? extends B>, B>, Entry<Class<? extends B>, B>>(this.delegate().iterator()) {
               Entry<Class<? extends B>, B> transform(Entry<Class<? extends B>, B> from) {
                  return MutableClassToInstanceMap.checkedEntry(from);
               }
            };
         }

         @Override
         public Object[] toArray() {
            return this.standardToArray();
         }

         @Override
         public <T> T[] toArray(T[] array) {
            return (T[])this.standardToArray(array);
         }
      };
   }

   @CanIgnoreReturnValue
   public B put(Class<? extends B> key, B value) {
      return super.put(key, cast((Class<B>)key, value));
   }

   @Override
   public void putAll(Map<? extends Class<? extends B>, ? extends B> map) {
      Map<Class<? extends B>, B> copy = new LinkedHashMap<>(map);

      for (Entry<? extends Class<? extends B>, B> entry : copy.entrySet()) {
         cast((Class<? extends B>)entry.getKey(), entry.getValue());
      }

      super.putAll(copy);
   }

   @CanIgnoreReturnValue
   @Override
   public <T extends B> T putInstance(Class<T> type, T value) {
      return cast(type, this.put(type, (B)value));
   }

   @Override
   public <T extends B> T getInstance(Class<T> type) {
      return cast(type, this.get(type));
   }

   @CanIgnoreReturnValue
   private static <B, T extends B> T cast(Class<T> type, B value) {
      return Primitives.wrap(type).cast(value);
   }

   private Object writeReplace() {
      return new MutableClassToInstanceMap.SerializedForm<>(this.delegate());
   }

   private static final class SerializedForm<B> implements Serializable {
      private final Map<Class<? extends B>, B> backingMap;
      private static final long serialVersionUID = 0L;

      SerializedForm(Map<Class<? extends B>, B> backingMap) {
         this.backingMap = backingMap;
      }

      Object readResolve() {
         return MutableClassToInstanceMap.create(this.backingMap);
      }
   }
}
