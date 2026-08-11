package org.spongepowered.libraries.com.google.common.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.Joiner;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Predicate;
import org.spongepowered.libraries.com.google.common.collect.FluentIterable;
import org.spongepowered.libraries.com.google.common.collect.ForwardingSet;
import org.spongepowered.libraries.com.google.common.collect.ImmutableList;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableSet;
import org.spongepowered.libraries.com.google.common.collect.Maps;
import org.spongepowered.libraries.com.google.common.collect.Ordering;
import org.spongepowered.libraries.com.google.common.primitives.Primitives;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public abstract class TypeToken<T> extends TypeCapture<T> implements Serializable {
   private final Type runtimeType;
   private transient TypeResolver typeResolver;

   protected TypeToken() {
      this.runtimeType = this.capture();
      Preconditions.checkState(
         !(this.runtimeType instanceof TypeVariable),
         "Cannot construct a TypeToken for a type variable.\nYou probably meant to call new TypeToken<%s>(getClass()) that can resolve the type variable for you.\nIf you do need to create a TypeToken of a type variable, please use TypeToken.of() instead.",
         this.runtimeType
      );
   }

   protected TypeToken(Class<?> declaringClass) {
      Type captured = super.capture();
      if (captured instanceof Class) {
         this.runtimeType = captured;
      } else {
         this.runtimeType = of(declaringClass).resolveType(captured).runtimeType;
      }
   }

   private TypeToken(Type type) {
      this.runtimeType = Preconditions.checkNotNull(type);
   }

   public static <T> TypeToken<T> of(Class<T> type) {
      return new TypeToken.SimpleTypeToken<>(type);
   }

   public static TypeToken<?> of(Type type) {
      return new TypeToken.SimpleTypeToken(type);
   }

   public final Class<? super T> getRawType() {
      return this.getRawTypes().iterator().next();
   }

   public final Type getType() {
      return this.runtimeType;
   }

   public final <X> TypeToken<T> where(TypeParameter<X> typeParam, TypeToken<X> typeArg) {
      TypeResolver resolver = new TypeResolver().where(ImmutableMap.of(new TypeResolver.TypeVariableKey(typeParam.typeVariable), typeArg.runtimeType));
      return new TypeToken.SimpleTypeToken<>(resolver.resolveType(this.runtimeType));
   }

   public final <X> TypeToken<T> where(TypeParameter<X> typeParam, Class<X> typeArg) {
      return this.where(typeParam, of(typeArg));
   }

   public final TypeToken<?> resolveType(Type type) {
      Preconditions.checkNotNull(type);
      TypeResolver resolver = this.typeResolver;
      if (resolver == null) {
         resolver = this.typeResolver = TypeResolver.accordingTo(this.runtimeType);
      }

      return of(resolver.resolveType(type));
   }

   private Type[] resolveInPlace(Type[] types) {
      for (int i = 0; i < types.length; i++) {
         types[i] = this.resolveType(types[i]).getType();
      }

      return types;
   }

   private TypeToken<?> resolveSupertype(Type type) {
      TypeToken<?> supertype = this.resolveType(type);
      supertype.typeResolver = this.typeResolver;
      return supertype;
   }

   @Nullable
   final TypeToken<? super T> getGenericSuperclass() {
      if (this.runtimeType instanceof TypeVariable) {
         return this.boundAsSuperclass(((TypeVariable)this.runtimeType).getBounds()[0]);
      } else if (this.runtimeType instanceof WildcardType) {
         return this.boundAsSuperclass(((WildcardType)this.runtimeType).getUpperBounds()[0]);
      } else {
         Type superclass = this.getRawType().getGenericSuperclass();
         return (TypeToken<? super T>)(superclass == null ? null : this.resolveSupertype(superclass));
      }
   }

   @Nullable
   private TypeToken<? super T> boundAsSuperclass(Type bound) {
      TypeToken<?> token = of(bound);
      return (TypeToken<? super T>)(token.getRawType().isInterface() ? null : token);
   }

   final ImmutableList<TypeToken<? super T>> getGenericInterfaces() {
      if (this.runtimeType instanceof TypeVariable) {
         return this.boundsAsInterfaces(((TypeVariable)this.runtimeType).getBounds());
      } else if (this.runtimeType instanceof WildcardType) {
         return this.boundsAsInterfaces(((WildcardType)this.runtimeType).getUpperBounds());
      } else {
         ImmutableList.Builder<TypeToken<? super T>> builder = ImmutableList.builder();

         for (Type interfaceType : this.getRawType().getGenericInterfaces()) {
            TypeToken<? super T> resolvedInterface = (TypeToken<? super T>)this.resolveSupertype(interfaceType);
            builder.add(resolvedInterface);
         }

         return builder.build();
      }
   }

   private ImmutableList<TypeToken<? super T>> boundsAsInterfaces(Type[] bounds) {
      ImmutableList.Builder<TypeToken<? super T>> builder = ImmutableList.builder();

      for (Type bound : bounds) {
         TypeToken<? super T> boundType = (TypeToken<? super T>)of(bound);
         if (boundType.getRawType().isInterface()) {
            builder.add(boundType);
         }
      }

      return builder.build();
   }

   public final TypeToken<T>.TypeSet getTypes() {
      return new TypeToken.TypeSet();
   }

   public final TypeToken<? super T> getSupertype(Class<? super T> superclass) {
      Preconditions.checkArgument(this.someRawTypeIsSubclassOf(superclass), "%s is not a super class of %s", superclass, this);
      if (this.runtimeType instanceof TypeVariable) {
         return this.getSupertypeFromUpperBounds(superclass, ((TypeVariable)this.runtimeType).getBounds());
      } else if (this.runtimeType instanceof WildcardType) {
         return this.getSupertypeFromUpperBounds(superclass, ((WildcardType)this.runtimeType).getUpperBounds());
      } else {
         return superclass.isArray() ? this.getArraySupertype(superclass) : this.resolveSupertype(toGenericType(superclass).runtimeType);
      }
   }

   public final TypeToken<? extends T> getSubtype(Class<?> subclass) {
      Preconditions.checkArgument(!(this.runtimeType instanceof TypeVariable), "Cannot get subtype of type variable <%s>", this);
      if (this.runtimeType instanceof WildcardType) {
         return this.getSubtypeFromLowerBounds(subclass, ((WildcardType)this.runtimeType).getLowerBounds());
      } else if (this.isArray()) {
         return this.getArraySubtype(subclass);
      } else {
         Preconditions.checkArgument(this.getRawType().isAssignableFrom(subclass), "%s isn't a subclass of %s", subclass, this);
         Type resolvedTypeArgs = this.resolveTypeArgsForSubclass(subclass);
         return (TypeToken<? extends T>)of(resolvedTypeArgs);
      }
   }

   public final boolean isSupertypeOf(TypeToken<?> type) {
      return type.isSubtypeOf(this.getType());
   }

   public final boolean isSupertypeOf(Type type) {
      return of(type).isSubtypeOf(this.getType());
   }

   public final boolean isSubtypeOf(TypeToken<?> type) {
      return this.isSubtypeOf(type.getType());
   }

   public final boolean isSubtypeOf(Type supertype) {
      Preconditions.checkNotNull(supertype);
      if (supertype instanceof WildcardType) {
         return any(((WildcardType)supertype).getLowerBounds()).isSupertypeOf(this.runtimeType);
      } else if (this.runtimeType instanceof WildcardType) {
         return any(((WildcardType)this.runtimeType).getUpperBounds()).isSubtypeOf(supertype);
      } else if (!(this.runtimeType instanceof TypeVariable)) {
         if (this.runtimeType instanceof GenericArrayType) {
            return of(supertype).isSupertypeOfArray((GenericArrayType)this.runtimeType);
         } else if (supertype instanceof Class) {
            return this.someRawTypeIsSubclassOf((Class<?>)supertype);
         } else if (supertype instanceof ParameterizedType) {
            return this.isSubtypeOfParameterizedType((ParameterizedType)supertype);
         } else {
            return supertype instanceof GenericArrayType ? this.isSubtypeOfArrayType((GenericArrayType)supertype) : false;
         }
      } else {
         return this.runtimeType.equals(supertype) || any(((TypeVariable)this.runtimeType).getBounds()).isSubtypeOf(supertype);
      }
   }

   public final boolean isArray() {
      return this.getComponentType() != null;
   }

   public final boolean isPrimitive() {
      return this.runtimeType instanceof Class && ((Class)this.runtimeType).isPrimitive();
   }

   public final TypeToken<T> wrap() {
      if (this.isPrimitive()) {
         Class<T> type = (Class<T>)this.runtimeType;
         return of(Primitives.wrap(type));
      } else {
         return this;
      }
   }

   private boolean isWrapper() {
      return Primitives.allWrapperTypes().contains(this.runtimeType);
   }

   public final TypeToken<T> unwrap() {
      if (this.isWrapper()) {
         Class<T> type = (Class<T>)this.runtimeType;
         return of(Primitives.unwrap(type));
      } else {
         return this;
      }
   }

   @Nullable
   public final TypeToken<?> getComponentType() {
      Type componentType = Types.getComponentType(this.runtimeType);
      return componentType == null ? null : of(componentType);
   }

   public final Invokable<T, Object> method(Method method) {
      Preconditions.checkArgument(this.someRawTypeIsSubclassOf(method.getDeclaringClass()), "%s not declared by %s", method, this);
      return new Invokable.MethodInvokable<T>(method) {
         @Override
         Type getGenericReturnType() {
            return TypeToken.this.resolveType(super.getGenericReturnType()).getType();
         }

         @Override
         Type[] getGenericParameterTypes() {
            return TypeToken.this.resolveInPlace(super.getGenericParameterTypes());
         }

         @Override
         Type[] getGenericExceptionTypes() {
            return TypeToken.this.resolveInPlace(super.getGenericExceptionTypes());
         }

         @Override
         public TypeToken<T> getOwnerType() {
            return TypeToken.this;
         }

         @Override
         public String toString() {
            return this.getOwnerType() + "." + super.toString();
         }
      };
   }

   public final Invokable<T, T> constructor(Constructor<?> constructor) {
      Preconditions.checkArgument(constructor.getDeclaringClass() == this.getRawType(), "%s not declared by %s", constructor, this.getRawType());
      return new Invokable.ConstructorInvokable<T>(constructor) {
         @Override
         Type getGenericReturnType() {
            return TypeToken.this.resolveType(super.getGenericReturnType()).getType();
         }

         @Override
         Type[] getGenericParameterTypes() {
            return TypeToken.this.resolveInPlace(super.getGenericParameterTypes());
         }

         @Override
         Type[] getGenericExceptionTypes() {
            return TypeToken.this.resolveInPlace(super.getGenericExceptionTypes());
         }

         @Override
         public TypeToken<T> getOwnerType() {
            return TypeToken.this;
         }

         @Override
         public String toString() {
            return this.getOwnerType() + "(" + Joiner.on(", ").join(this.getGenericParameterTypes()) + ")";
         }
      };
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o instanceof TypeToken) {
         TypeToken<?> that = (TypeToken<?>)o;
         return this.runtimeType.equals(that.runtimeType);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.runtimeType.hashCode();
   }

   @Override
   public String toString() {
      return Types.toString(this.runtimeType);
   }

   protected Object writeReplace() {
      return of(new TypeResolver().resolveType(this.runtimeType));
   }

   @CanIgnoreReturnValue
   final TypeToken<T> rejectTypeVariables() {
      (new TypeVisitor() {
         @Override
         void visitTypeVariable(TypeVariable<?> type) {
            throw new IllegalArgumentException(TypeToken.this.runtimeType + "contains a type variable and is not safe for the operation");
         }

         @Override
         void visitWildcardType(WildcardType type) {
            this.visit(type.getLowerBounds());
            this.visit(type.getUpperBounds());
         }

         @Override
         void visitParameterizedType(ParameterizedType type) {
            this.visit(type.getActualTypeArguments());
            this.visit(type.getOwnerType());
         }

         @Override
         void visitGenericArrayType(GenericArrayType type) {
            this.visit(type.getGenericComponentType());
         }
      }).visit(this.runtimeType);
      return this;
   }

   private boolean someRawTypeIsSubclassOf(Class<?> superclass) {
      for (Class<?> rawType : this.getRawTypes()) {
         if (superclass.isAssignableFrom(rawType)) {
            return true;
         }
      }

      return false;
   }

   private boolean isSubtypeOfParameterizedType(ParameterizedType supertype) {
      Class<?> matchedClass = of(supertype).getRawType();
      if (!this.someRawTypeIsSubclassOf(matchedClass)) {
         return false;
      } else {
         Type[] typeParams = matchedClass.getTypeParameters();
         Type[] toTypeArgs = supertype.getActualTypeArguments();

         for (int i = 0; i < typeParams.length; i++) {
            if (!this.resolveType(typeParams[i]).is(toTypeArgs[i])) {
               return false;
            }
         }

         return Modifier.isStatic(((Class)supertype.getRawType()).getModifiers())
            || supertype.getOwnerType() == null
            || this.isOwnedBySubtypeOf(supertype.getOwnerType());
      }
   }

   private boolean isSubtypeOfArrayType(GenericArrayType supertype) {
      if (this.runtimeType instanceof Class) {
         Class<?> fromClass = (Class<?>)this.runtimeType;
         return !fromClass.isArray() ? false : of(fromClass.getComponentType()).isSubtypeOf(supertype.getGenericComponentType());
      } else if (this.runtimeType instanceof GenericArrayType) {
         GenericArrayType fromArrayType = (GenericArrayType)this.runtimeType;
         return of(fromArrayType.getGenericComponentType()).isSubtypeOf(supertype.getGenericComponentType());
      } else {
         return false;
      }
   }

   private boolean isSupertypeOfArray(GenericArrayType subtype) {
      if (this.runtimeType instanceof Class) {
         Class<?> thisClass = (Class<?>)this.runtimeType;
         return !thisClass.isArray()
            ? thisClass.isAssignableFrom(Object[].class)
            : of(subtype.getGenericComponentType()).isSubtypeOf(thisClass.getComponentType());
      } else {
         return this.runtimeType instanceof GenericArrayType
            ? of(subtype.getGenericComponentType()).isSubtypeOf(((GenericArrayType)this.runtimeType).getGenericComponentType())
            : false;
      }
   }

   private boolean is(Type formalType) {
      if (this.runtimeType.equals(formalType)) {
         return true;
      } else {
         return !(formalType instanceof WildcardType)
            ? false
            : every(((WildcardType)formalType).getUpperBounds()).isSupertypeOf(this.runtimeType)
               && every(((WildcardType)formalType).getLowerBounds()).isSubtypeOf(this.runtimeType);
      }
   }

   private static TypeToken.Bounds every(Type[] bounds) {
      return new TypeToken.Bounds(bounds, false);
   }

   private static TypeToken.Bounds any(Type[] bounds) {
      return new TypeToken.Bounds(bounds, true);
   }

   private ImmutableSet<Class<? super T>> getRawTypes() {
      final ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
      (new TypeVisitor() {
         @Override
         void visitTypeVariable(TypeVariable<?> t) {
            this.visit(t.getBounds());
         }

         @Override
         void visitWildcardType(WildcardType t) {
            this.visit(t.getUpperBounds());
         }

         @Override
         void visitParameterizedType(ParameterizedType t) {
            builder.add((Class<?>)t.getRawType());
         }

         @Override
         void visitClass(Class<?> t) {
            builder.add(t);
         }

         @Override
         void visitGenericArrayType(GenericArrayType t) {
            builder.add(Types.getArrayClass(TypeToken.of(t.getGenericComponentType()).getRawType()));
         }
      }).visit(this.runtimeType);
      return (ImmutableSet<Class<? super T>>)builder.build();
   }

   private boolean isOwnedBySubtypeOf(Type supertype) {
      for (TypeToken<?> type : this.getTypes()) {
         Type ownerType = type.getOwnerTypeIfPresent();
         if (ownerType != null && of(ownerType).isSubtypeOf(supertype)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   private Type getOwnerTypeIfPresent() {
      if (this.runtimeType instanceof ParameterizedType) {
         return ((ParameterizedType)this.runtimeType).getOwnerType();
      } else {
         return this.runtimeType instanceof Class ? ((Class)this.runtimeType).getEnclosingClass() : null;
      }
   }

   @VisibleForTesting
   static <T> TypeToken<? extends T> toGenericType(Class<T> cls) {
      if (cls.isArray()) {
         Type arrayOfGenericType = Types.newArrayType(toGenericType(cls.getComponentType()).runtimeType);
         return (TypeToken<? extends T>)of(arrayOfGenericType);
      } else {
         TypeVariable<Class<T>>[] typeParams = cls.getTypeParameters();
         Type ownerType = cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers()) ? toGenericType(cls.getEnclosingClass()).runtimeType : null;
         return typeParams.length <= 0 && (ownerType == null || ownerType == cls.getEnclosingClass())
            ? of(cls)
            : of(Types.newParameterizedTypeWithOwner(ownerType, cls, typeParams));
      }
   }

   private TypeToken<? super T> getSupertypeFromUpperBounds(Class<? super T> supertype, Type[] upperBounds) {
      for (Type upperBound : upperBounds) {
         TypeToken<? super T> bound = (TypeToken<? super T>)of(upperBound);
         if (bound.isSubtypeOf(supertype)) {
            return bound.getSupertype(supertype);
         }
      }

      throw new IllegalArgumentException(supertype + " isn't a super type of " + this);
   }

   private TypeToken<? extends T> getSubtypeFromLowerBounds(Class<?> subclass, Type[] lowerBounds) {
      int var4 = lowerBounds.length;
      byte var5 = 0;
      if (var5 < var4) {
         Type lowerBound = lowerBounds[var5];
         TypeToken<? extends T> bound = (TypeToken<? extends T>)of(lowerBound);
         return bound.getSubtype(subclass);
      } else {
         throw new IllegalArgumentException(subclass + " isn't a subclass of " + this);
      }
   }

   private TypeToken<? super T> getArraySupertype(Class<? super T> supertype) {
      TypeToken componentType = Preconditions.checkNotNull(this.getComponentType(), "%s isn't a super type of %s", supertype, this);
      TypeToken<?> componentSupertype = componentType.getSupertype((Class<? super T>)supertype.getComponentType());
      return (TypeToken<? super T>)of(newArrayClassOrGenericArrayType(componentSupertype.runtimeType));
   }

   private TypeToken<? extends T> getArraySubtype(Class<?> subclass) {
      TypeToken<?> componentSubtype = this.getComponentType().getSubtype(subclass.getComponentType());
      return (TypeToken<? extends T>)of(newArrayClassOrGenericArrayType(componentSubtype.runtimeType));
   }

   private Type resolveTypeArgsForSubclass(Class<?> subclass) {
      if (!(this.runtimeType instanceof Class) || subclass.getTypeParameters().length != 0 && this.getRawType().getTypeParameters().length == 0) {
         TypeToken<?> genericSubtype = toGenericType(subclass);
         Type supertypeWithArgsFromSubtype = genericSubtype.getSupertype(this.getRawType()).runtimeType;
         return new TypeResolver().where(supertypeWithArgsFromSubtype, this.runtimeType).resolveType(genericSubtype.runtimeType);
      } else {
         return subclass;
      }
   }

   private static Type newArrayClassOrGenericArrayType(Type componentType) {
      return Types.JavaVersion.JAVA7.newArrayType(componentType);
   }

   private static class Bounds {
      private final Type[] bounds;
      private final boolean target;

      Bounds(Type[] bounds, boolean target) {
         this.bounds = bounds;
         this.target = target;
      }

      boolean isSubtypeOf(Type supertype) {
         for (Type bound : this.bounds) {
            if (TypeToken.of(bound).isSubtypeOf(supertype) == this.target) {
               return this.target;
            }
         }

         return !this.target;
      }

      boolean isSupertypeOf(Type subtype) {
         TypeToken<?> type = TypeToken.of(subtype);

         for (Type bound : this.bounds) {
            if (type.isSubtypeOf(bound) == this.target) {
               return this.target;
            }
         }

         return !this.target;
      }
   }

   private final class ClassSet extends TypeToken<T>.TypeSet {
      private transient ImmutableSet<TypeToken<? super T>> classes;
      private static final long serialVersionUID = 0L;

      private ClassSet() {
      }

      @Override
      protected Set<TypeToken<? super T>> delegate() {
         ImmutableSet<TypeToken<? super T>> result = this.classes;
         if (result == null) {
            ImmutableList<TypeToken<? super T>> collectedTypes = (ImmutableList<TypeToken<? super T>>)TypeToken.TypeCollector.FOR_GENERIC_TYPE
               .classesOnly()
               .collectTypes(TypeToken.this);
            return this.classes = FluentIterable.from(collectedTypes).filter(TypeToken.TypeFilter.IGNORE_TYPE_VARIABLE_OR_WILDCARD).toSet();
         } else {
            return result;
         }
      }

      @Override
      public TypeToken<T>.TypeSet classes() {
         return this;
      }

      @Override
      public Set<Class<? super T>> rawTypes() {
         ImmutableList<Class<? super T>> collectedTypes = (ImmutableList<Class<? super T>>)TypeToken.TypeCollector.FOR_RAW_TYPE
            .classesOnly()
            .collectTypes(TypeToken.this.getRawTypes());
         return ImmutableSet.copyOf(collectedTypes);
      }

      @Override
      public TypeToken<T>.TypeSet interfaces() {
         throw new UnsupportedOperationException("classes().interfaces() not supported.");
      }

      private Object readResolve() {
         return TypeToken.this.getTypes().classes();
      }
   }

   private final class InterfaceSet extends TypeToken<T>.TypeSet {
      private final transient TypeToken<T>.TypeSet allTypes;
      private transient ImmutableSet<TypeToken<? super T>> interfaces;
      private static final long serialVersionUID = 0L;

      InterfaceSet(TypeToken<T>.TypeSet allTypes) {
         this.allTypes = allTypes;
      }

      @Override
      protected Set<TypeToken<? super T>> delegate() {
         ImmutableSet<TypeToken<? super T>> result = this.interfaces;
         return result == null ? (this.interfaces = FluentIterable.from(this.allTypes).filter(TypeToken.TypeFilter.INTERFACE_ONLY).toSet()) : result;
      }

      @Override
      public TypeToken<T>.TypeSet interfaces() {
         return this;
      }

      @Override
      public Set<Class<? super T>> rawTypes() {
         ImmutableList<Class<? super T>> collectedTypes = (ImmutableList<Class<? super T>>)TypeToken.TypeCollector.FOR_RAW_TYPE
            .collectTypes(TypeToken.this.getRawTypes());
         return FluentIterable.from(collectedTypes).filter(new Predicate<Class<?>>() {
            public boolean apply(Class<?> type) {
               return type.isInterface();
            }
         }).toSet();
      }

      @Override
      public TypeToken<T>.TypeSet classes() {
         throw new UnsupportedOperationException("interfaces().classes() not supported.");
      }

      private Object readResolve() {
         return TypeToken.this.getTypes().interfaces();
      }
   }

   private static final class SimpleTypeToken<T> extends TypeToken<T> {
      private static final long serialVersionUID = 0L;

      SimpleTypeToken(Type type) {
         super(type);
      }
   }

   private abstract static class TypeCollector<K> {
      static final TypeToken.TypeCollector<TypeToken<?>> FOR_GENERIC_TYPE = new TypeToken.TypeCollector<TypeToken<?>>() {
         Class<?> getRawType(TypeToken<?> type) {
            return type.getRawType();
         }

         Iterable<? extends TypeToken<?>> getInterfaces(TypeToken<?> type) {
            return type.getGenericInterfaces();
         }

         @Nullable
         TypeToken<?> getSuperclass(TypeToken<?> type) {
            return type.getGenericSuperclass();
         }
      };
      static final TypeToken.TypeCollector<Class<?>> FOR_RAW_TYPE = new TypeToken.TypeCollector<Class<?>>() {
         Class<?> getRawType(Class<?> type) {
            return type;
         }

         Iterable<? extends Class<?>> getInterfaces(Class<?> type) {
            return Arrays.asList(type.getInterfaces());
         }

         @Nullable
         Class<?> getSuperclass(Class<?> type) {
            return type.getSuperclass();
         }
      };

      private TypeCollector() {
      }

      final TypeToken.TypeCollector<K> classesOnly() {
         return new TypeToken.TypeCollector.ForwardingTypeCollector<K>(this) {
            @Override
            Iterable<? extends K> getInterfaces(K type) {
               return ImmutableSet.of();
            }

            @Override
            ImmutableList<K> collectTypes(Iterable<? extends K> types) {
               ImmutableList.Builder<K> builder = ImmutableList.builder();

               for (K type : types) {
                  if (!this.getRawType(type).isInterface()) {
                     builder.add(type);
                  }
               }

               return super.collectTypes(builder.build());
            }
         };
      }

      final ImmutableList<K> collectTypes(K type) {
         return this.collectTypes(ImmutableList.of(type));
      }

      ImmutableList<K> collectTypes(Iterable<? extends K> types) {
         Map<K, Integer> map = Maps.newHashMap();

         for (K type : types) {
            this.collectTypes(type, map);
         }

         return sortKeysByValue(map, Ordering.natural().reverse());
      }

      @CanIgnoreReturnValue
      private int collectTypes(K type, Map<? super K, Integer> map) {
         Integer existing = map.get(type);
         if (existing != null) {
            return existing;
         } else {
            int aboveMe = this.getRawType(type).isInterface() ? 1 : 0;

            for (K interfaceType : this.getInterfaces(type)) {
               aboveMe = Math.max(aboveMe, this.collectTypes(interfaceType, map));
            }

            K superclass = this.getSuperclass(type);
            if (superclass != null) {
               aboveMe = Math.max(aboveMe, this.collectTypes(superclass, map));
            }

            map.put(type, aboveMe + 1);
            return aboveMe + 1;
         }
      }

      private static <K, V> ImmutableList<K> sortKeysByValue(final Map<K, V> map, final Comparator<? super V> valueComparator) {
         Ordering<K> keyOrdering = new Ordering<K>() {
            @Override
            public int compare(K left, K right) {
               return valueComparator.compare(map.get(left), map.get(right));
            }
         };
         return keyOrdering.immutableSortedCopy(map.keySet());
      }

      abstract Class<?> getRawType(K var1);

      abstract Iterable<? extends K> getInterfaces(K var1);

      @Nullable
      abstract K getSuperclass(K var1);

      private static class ForwardingTypeCollector<K> extends TypeToken.TypeCollector<K> {
         private final TypeToken.TypeCollector<K> delegate;

         ForwardingTypeCollector(TypeToken.TypeCollector<K> delegate) {
            this.delegate = delegate;
         }

         @Override
         Class<?> getRawType(K type) {
            return this.delegate.getRawType(type);
         }

         @Override
         Iterable<? extends K> getInterfaces(K type) {
            return this.delegate.getInterfaces(type);
         }

         @Override
         K getSuperclass(K type) {
            return this.delegate.getSuperclass(type);
         }
      }
   }

   private static enum TypeFilter implements Predicate<TypeToken<?>> {
      IGNORE_TYPE_VARIABLE_OR_WILDCARD {
         public boolean apply(TypeToken<?> type) {
            return !(type.runtimeType instanceof TypeVariable) && !(type.runtimeType instanceof WildcardType);
         }
      },
      INTERFACE_ONLY {
         public boolean apply(TypeToken<?> type) {
            return type.getRawType().isInterface();
         }
      };

      private TypeFilter() {
      }
   }

   public class TypeSet extends ForwardingSet<TypeToken<? super T>> implements Serializable {
      private transient ImmutableSet<TypeToken<? super T>> types;
      private static final long serialVersionUID = 0L;

      TypeSet() {
      }

      public TypeToken<T>.TypeSet interfaces() {
         return TypeToken.this.new InterfaceSet(this);
      }

      public TypeToken<T>.TypeSet classes() {
         return TypeToken.this.new ClassSet();
      }

      @Override
      protected Set<TypeToken<? super T>> delegate() {
         ImmutableSet<TypeToken<? super T>> filteredTypes = this.types;
         if (filteredTypes == null) {
            ImmutableList<TypeToken<? super T>> collectedTypes = (ImmutableList<TypeToken<? super T>>)TypeToken.TypeCollector.FOR_GENERIC_TYPE
               .collectTypes(TypeToken.this);
            return this.types = FluentIterable.from(collectedTypes).filter(TypeToken.TypeFilter.IGNORE_TYPE_VARIABLE_OR_WILDCARD).toSet();
         } else {
            return filteredTypes;
         }
      }

      public Set<Class<? super T>> rawTypes() {
         ImmutableList<Class<? super T>> collectedTypes = (ImmutableList<Class<? super T>>)TypeToken.TypeCollector.FOR_RAW_TYPE
            .collectTypes(TypeToken.this.getRawTypes());
         return ImmutableSet.copyOf(collectedTypes);
      }
   }
}
