package org.spongepowered.asm.lib.commons;

import java.util.Collections;
import java.util.Map;
import org.spongepowered.asm.lib.Handle;

public class SimpleRemapper extends Remapper {
   private final Map<String, String> mapping;

   @Deprecated(forRemoval = false)
   public SimpleRemapper(Map<String, String> mapping) {
      this.mapping = mapping;
   }

   public SimpleRemapper(int api, Map<String, String> mapping) {
      super(api);
      this.mapping = mapping;
   }

   @Deprecated(forRemoval = false)
   public SimpleRemapper(String oldName, String newName) {
      this.mapping = Collections.singletonMap(oldName, newName);
   }

   public SimpleRemapper(int api, String oldName, String newName) {
      super(api);
      this.mapping = Collections.singletonMap(oldName, newName);
   }

   @Override
   public String mapMethodName(String owner, String name, String descriptor) {
      String remappedName = this.map(stringConcat$0(owner, name, descriptor));
      return remappedName == null ? name : remappedName;
   }

   @Deprecated(forRemoval = false)
   @Override
   public String mapInvokeDynamicMethodName(String name, String descriptor) {
      String remappedName = this.map(stringConcat$1(name, descriptor));
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapBasicInvokeDynamicMethodName(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
      String remappedName = this.map(stringConcat$2(name, descriptor));
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapAnnotationAttributeName(String descriptor, String name) {
      String remappedName = this.map(stringConcat$3(descriptor, name));
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String mapFieldName(String owner, String name, String descriptor) {
      String remappedName = this.map(stringConcat$4(owner, name));
      return remappedName == null ? name : remappedName;
   }

   @Override
   public String map(String key) {
      return this.mapping.get(key);
   }
}
