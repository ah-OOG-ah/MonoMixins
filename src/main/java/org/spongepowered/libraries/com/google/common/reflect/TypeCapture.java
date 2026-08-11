package org.spongepowered.libraries.com.google.common.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

abstract class TypeCapture<T> {
   final Type capture() {
      Type superclass = this.getClass().getGenericSuperclass();
      Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
      return ((ParameterizedType)superclass).getActualTypeArguments()[0];
   }
}
