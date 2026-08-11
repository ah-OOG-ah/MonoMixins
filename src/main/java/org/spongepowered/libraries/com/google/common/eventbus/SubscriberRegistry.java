package org.spongepowered.libraries.com.google.common.eventbus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.MoreObjects;
import org.spongepowered.libraries.com.google.common.base.Objects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Throwables;
import org.spongepowered.libraries.com.google.common.cache.CacheBuilder;
import org.spongepowered.libraries.com.google.common.cache.CacheLoader;
import org.spongepowered.libraries.com.google.common.cache.LoadingCache;
import org.spongepowered.libraries.com.google.common.collect.HashMultimap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableList;
import org.spongepowered.libraries.com.google.common.collect.ImmutableSet;
import org.spongepowered.libraries.com.google.common.collect.Iterators;
import org.spongepowered.libraries.com.google.common.collect.Lists;
import org.spongepowered.libraries.com.google.common.collect.Maps;
import org.spongepowered.libraries.com.google.common.collect.Multimap;
import org.spongepowered.libraries.com.google.common.reflect.TypeToken;
import org.spongepowered.libraries.com.google.common.util.concurrent.UncheckedExecutionException;
import org.spongepowered.libraries.com.google.j2objc.annotations.Weak;

final class SubscriberRegistry {
   private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = Maps.newConcurrentMap();
   @Weak
   private final EventBus bus;
   private static final LoadingCache<Class<?>, ImmutableList<Method>> subscriberMethodsCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build(new CacheLoader<Class<?>, ImmutableList<Method>>() {
         public ImmutableList<Method> load(Class<?> concreteClass) throws Exception {
            return SubscriberRegistry.getAnnotatedMethodsNotCached(concreteClass);
         }
      });
   private static final LoadingCache<Class<?>, ImmutableSet<Class<?>>> flattenHierarchyCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build(new CacheLoader<Class<?>, ImmutableSet<Class<?>>>() {
         public ImmutableSet<Class<?>> load(Class<?> concreteClass) {
            return ImmutableSet.copyOf(TypeToken.of(concreteClass).getTypes().rawTypes());
         }
      });

   SubscriberRegistry(EventBus bus) {
      this.bus = Preconditions.checkNotNull(bus);
   }

   void register(Object listener) {
      Multimap<Class<?>, Subscriber> listenerMethods = this.findAllSubscribers(listener);

      for (Entry<Class<?>, Collection<Subscriber>> entry : listenerMethods.asMap().entrySet()) {
         Class<?> eventType = entry.getKey();
         Collection<Subscriber> eventMethodsInListener = entry.getValue();
         CopyOnWriteArraySet<Subscriber> eventSubscribers = this.subscribers.get(eventType);
         if (eventSubscribers == null) {
            CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet<>();
            eventSubscribers = MoreObjects.firstNonNull(this.subscribers.putIfAbsent(eventType, newSet), newSet);
         }

         eventSubscribers.addAll(eventMethodsInListener);
      }
   }

   void unregister(Object listener) {
      Multimap<Class<?>, Subscriber> listenerMethods = this.findAllSubscribers(listener);

      for (Entry<Class<?>, Collection<Subscriber>> entry : listenerMethods.asMap().entrySet()) {
         Class<?> eventType = entry.getKey();
         Collection<Subscriber> listenerMethodsForType = entry.getValue();
         CopyOnWriteArraySet<Subscriber> currentSubscribers = this.subscribers.get(eventType);
         if (currentSubscribers == null || !currentSubscribers.removeAll(listenerMethodsForType)) {
            throw new IllegalArgumentException("missing event subscriber for an annotated method. Is " + listener + " registered?");
         }
      }
   }

   @VisibleForTesting
   Set<Subscriber> getSubscribersForTesting(Class<?> eventType) {
      return MoreObjects.firstNonNull(this.subscribers.get(eventType), ImmutableSet.<Subscriber>of());
   }

   Iterator<Subscriber> getSubscribers(Object event) {
      ImmutableSet<Class<?>> eventTypes = flattenHierarchy(event.getClass());
      List<Iterator<Subscriber>> subscriberIterators = Lists.newArrayListWithCapacity(eventTypes.size());

      for (Class<?> eventType : eventTypes) {
         CopyOnWriteArraySet<Subscriber> eventSubscribers = this.subscribers.get(eventType);
         if (eventSubscribers != null) {
            subscriberIterators.add(eventSubscribers.iterator());
         }
      }

      return Iterators.concat(subscriberIterators.iterator());
   }

   private Multimap<Class<?>, Subscriber> findAllSubscribers(Object listener) {
      Multimap<Class<?>, Subscriber> methodsInListener = HashMultimap.create();
      Class<?> clazz = listener.getClass();

      for (Method method : getAnnotatedMethods(clazz)) {
         Class<?>[] parameterTypes = method.getParameterTypes();
         Class<?> eventType = parameterTypes[0];
         methodsInListener.put(eventType, Subscriber.create(this.bus, listener, method));
      }

      return methodsInListener;
   }

   private static ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
      return subscriberMethodsCache.getUnchecked(clazz);
   }

   private static ImmutableList<Method> getAnnotatedMethodsNotCached(Class<?> clazz) {
      Set<? extends Class<?>> supertypes = TypeToken.of(clazz).getTypes().rawTypes();
      Map<SubscriberRegistry.MethodIdentifier, Method> identifiers = Maps.newHashMap();

      for (Class<?> supertype : supertypes) {
         for (Method method : supertype.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class) && !method.isSynthetic()) {
               Class<?>[] parameterTypes = method.getParameterTypes();
               Preconditions.checkArgument(
                  parameterTypes.length == 1,
                  "Method %s has @Subscribe annotation but has %s parameters.Subscriber methods must have exactly 1 parameter.",
                  method,
                  parameterTypes.length
               );
               SubscriberRegistry.MethodIdentifier ident = new SubscriberRegistry.MethodIdentifier(method);
               if (!identifiers.containsKey(ident)) {
                  identifiers.put(ident, method);
               }
            }
         }
      }

      return ImmutableList.copyOf(identifiers.values());
   }

   @VisibleForTesting
   static ImmutableSet<Class<?>> flattenHierarchy(Class<?> concreteClass) {
      try {
         return flattenHierarchyCache.getUnchecked(concreteClass);
      } catch (UncheckedExecutionException var2) {
         throw Throwables.propagate(var2.getCause());
      }
   }

   private static final class MethodIdentifier {
      private final String name;
      private final List<Class<?>> parameterTypes;

      MethodIdentifier(Method method) {
         this.name = method.getName();
         this.parameterTypes = Arrays.asList(method.getParameterTypes());
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.name, this.parameterTypes);
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (!(o instanceof SubscriberRegistry.MethodIdentifier)) {
            return false;
         } else {
            SubscriberRegistry.MethodIdentifier ident = (SubscriberRegistry.MethodIdentifier)o;
            return this.name.equals(ident.name) && this.parameterTypes.equals(ident.parameterTypes);
         }
      }
   }
}
