package org.spongepowered.libraries.com.google.common.reflect;

import java.util.Map;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.collect.ForwardingMap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public final class ImmutableTypeToInstanceMap<B> extends ForwardingMap<TypeToken<? extends B>, B> implements TypeToInstanceMap<B> {
   private final ImmutableMap<TypeToken<? extends B>, B> delegate;

   public static <B> ImmutableTypeToInstanceMap<B> of() {
      return new ImmutableTypeToInstanceMap<>(ImmutableMap.of());
   }

   public static <B> ImmutableTypeToInstanceMap.Builder<B> builder() {
      return new ImmutableTypeToInstanceMap.Builder<>();
   }

   private ImmutableTypeToInstanceMap(ImmutableMap<TypeToken<? extends B>, B> delegate) {
      this.delegate = delegate;
   }

   @Override
   public <T extends B> T getInstance(TypeToken<T> type) {
      return this.trustedGet(type.rejectTypeVariables());
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public <T extends B> T putInstance(TypeToken<T> type, T value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T extends B> T getInstance(Class<T> type) {
      return this.trustedGet(TypeToken.of(type));
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public <T extends B> T putInstance(Class<T> type, T value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   public B put(TypeToken<? extends B> key, B value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void putAll(Map<? extends TypeToken<? extends B>, ? extends B> map) {
      throw new UnsupportedOperationException();
   }

   @Override
   protected Map<TypeToken<? extends B>, B> delegate() {
      return this.delegate;
   }

   private <T extends B> T trustedGet(TypeToken<T> type) {
      return (T)this.delegate.get(type);
   }

   @Beta
   public static final class Builder<B> {
      private final ImmutableMap.Builder<TypeToken<? extends B>, B> mapBuilder = ImmutableMap.builder();

      private Builder() {
      }

      @CanIgnoreReturnValue
      public <T extends B> ImmutableTypeToInstanceMap.Builder<B> put(Class<T> key, T value) {
         this.mapBuilder.put(TypeToken.of(key), (B)value);
         return this;
      }

      @CanIgnoreReturnValue
      public <T extends B> ImmutableTypeToInstanceMap.Builder<B> put(TypeToken<T> key, T value) {
         this.mapBuilder.put(key.rejectTypeVariables(), (B)value);
         return this;
      }

      public ImmutableTypeToInstanceMap<B> build() {
         return new ImmutableTypeToInstanceMap<>(this.mapBuilder.build());
      }
   }
}
