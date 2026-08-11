package com.llamalad7.mixinextras.lib.apache.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClassUtils {
   public static final String PACKAGE_SEPARATOR = String.valueOf('.');
   public static final String INNER_CLASS_SEPARATOR = String.valueOf('$');
   private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
   private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
   private static final Map<String, String> abbreviationMap;
   private static final Map<String, String> reverseAbbreviationMap;

   public static List<Class<?>> getAllInterfaces(Class<?> cls) {
      if (cls == null) {
         return null;
      } else {
         LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
         getAllInterfaces(cls, interfacesFound);
         return new ArrayList<>(interfacesFound);
      }
   }

   private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound) {
      while (cls != null) {
         Class<?>[] interfaces = cls.getInterfaces();

         for (Class<?> i : interfaces) {
            if (interfacesFound.add(i)) {
               getAllInterfaces(i, interfacesFound);
            }
         }

         cls = cls.getSuperclass();
      }
   }

   static {
      primitiveWrapperMap.put(boolean.class, Boolean.class);
      primitiveWrapperMap.put(byte.class, Byte.class);
      primitiveWrapperMap.put(char.class, Character.class);
      primitiveWrapperMap.put(short.class, Short.class);
      primitiveWrapperMap.put(int.class, Integer.class);
      primitiveWrapperMap.put(long.class, Long.class);
      primitiveWrapperMap.put(double.class, Double.class);
      primitiveWrapperMap.put(float.class, Float.class);
      primitiveWrapperMap.put(void.class, void.class);

      for (Class<?> primitiveClass : primitiveWrapperMap.keySet()) {
         Class<?> wrapperClass = primitiveWrapperMap.get(primitiveClass);
         if (!primitiveClass.equals(wrapperClass)) {
            wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
         }
      }

      Map<String, String> m = new HashMap<>();
      m.put("int", "I");
      m.put("boolean", "Z");
      m.put("float", "F");
      m.put("long", "J");
      m.put("short", "S");
      m.put("byte", "B");
      m.put("double", "D");
      m.put("char", "C");
      m.put("void", "V");
      Map<String, String> r = new HashMap<>();

      for (Entry<String, String> e : m.entrySet()) {
         r.put(e.getValue(), e.getKey());
      }

      abbreviationMap = Collections.unmodifiableMap(m);
      reverseAbbreviationMap = Collections.unmodifiableMap(r);
   }
}
