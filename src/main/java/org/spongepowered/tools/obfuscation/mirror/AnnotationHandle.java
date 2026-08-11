package org.spongepowered.tools.obfuscation.mirror;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.util.asm.IAnnotationHandle;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

public final class AnnotationHandle implements IAnnotationHandle {
   public static final AnnotationHandle MISSING = new AnnotationHandle(null);
   private final AnnotationMirror annotation;

   private AnnotationHandle(AnnotationMirror annotation) {
      this.annotation = annotation;
   }

   public AnnotationMirror asMirror() {
      return this.annotation;
   }

   @Override
   public boolean exists() {
      return this.annotation != null;
   }

   @Override
   public String getDesc() {
      return this.annotation == null ? "java/lang/Annotation" : TypeUtils.getInternalName(this.annotation.getAnnotationType());
   }

   @Override
   public String toString() {
      return this.annotation == null ? "@{UnknownAnnotation}" : "@" + this.annotation.getAnnotationType().asElement().getSimpleName();
   }

   @Override
   public <T> T getValue(String key, T defaultValue) {
      if (this.annotation == null) {
         return defaultValue;
      } else {
         AnnotationValue value = this.getAnnotationValue(key);
         if (defaultValue instanceof Enum && value != null) {
            VariableElement varValue = (VariableElement)value.getValue();
            return varValue == null ? defaultValue : Enum.valueOf((Class<T>)defaultValue.getClass(), varValue.getSimpleName().toString());
         } else {
            return (T)(value != null ? value.getValue() : defaultValue);
         }
      }
   }

   @Override
   public <T> T getValue() {
      return this.getValue("value", null);
   }

   @Override
   public <T> T getValue(String key) {
      return this.getValue(key, null);
   }

   @Override
   public boolean getBoolean(String key, boolean defaultValue) {
      return this.getValue(key, defaultValue);
   }

   @Override
   public IAnnotationHandle getAnnotation(String key) {
      Object value = this.getValue(key);
      if (value instanceof AnnotationMirror) {
         return of((AnnotationMirror)value);
      } else {
         if (value instanceof AnnotationValue) {
            Object mirror = ((AnnotationValue)value).getValue();
            if (mirror instanceof AnnotationMirror) {
               return of((AnnotationMirror)mirror);
            }
         }

         return null;
      }
   }

   @Override
   public <T> List<T> getList() {
      return this.getList("value");
   }

   @Override
   public <T> List<T> getList(String key) {
      List<AnnotationValue> list = this.getValue(key, Collections.emptyList());
      return unwrapAnnotationValueList(list);
   }

   @Override
   public List<IAnnotationHandle> getAnnotationList(String key) {
      Object val = this.getValue(key, null);
      if (val == null) {
         return Collections.emptyList();
      } else if (val instanceof AnnotationMirror) {
         return ImmutableList.of(of((AnnotationMirror)val));
      } else {
         List<AnnotationValue> list = (List<AnnotationValue>)val;
         List<AnnotationHandle> annotations = new ArrayList<>(list.size());

         for (AnnotationValue value : list) {
            annotations.add(new AnnotationHandle((AnnotationMirror)value.getValue()));
         }

         return Collections.unmodifiableList(annotations);
      }
   }

   @Override
   public Type getTypeValue(String key) {
      TypeMirror typeMirror = this.getValue(key);
      return typeMirror == null ? Type.VOID_TYPE : Type.getType(TypeUtils.getInternalName(typeMirror));
   }

   @Override
   public List<Type> getTypeList(String key) {
      List<Type> list = this.getList(key);
      ListIterator<Type> iter = list.listIterator();

      while (iter.hasNext()) {
         Object next = iter.next();
         if (next instanceof TypeMirror) {
            iter.set(Type.getType(TypeUtils.getInternalName((TypeMirror)next)));
         }
      }

      return list;
   }

   protected AnnotationValue getAnnotationValue(String key) {
      for (ExecutableElement elem : this.annotation.getElementValues().keySet()) {
         if (elem.getSimpleName().contentEquals(key)) {
            return this.annotation.getElementValues().get(elem);
         }
      }

      return null;
   }

   protected static <T> List<T> unwrapAnnotationValueList(List<AnnotationValue> list) {
      if (list == null) {
         return Collections.emptyList();
      } else {
         List<T> unfolded = new ArrayList<>(list.size());

         for (AnnotationValue value : list) {
            unfolded.add((T)value.getValue());
         }

         return unfolded;
      }
   }

   protected static AnnotationMirror getAnnotation(Element elem, Class<? extends Annotation> annotationClass) {
      if (elem == null) {
         return null;
      } else {
         List<? extends AnnotationMirror> annotations = elem.getAnnotationMirrors();
         if (annotations == null) {
            return null;
         } else {
            for (AnnotationMirror annotation : annotations) {
               Element element = annotation.getAnnotationType().asElement();
               if (element instanceof TypeElement) {
                  TypeElement annotationElement = (TypeElement)element;
                  if (annotationElement.getQualifiedName().contentEquals(annotationClass.getName())) {
                     return annotation;
                  }
               }
            }

            return null;
         }
      }
   }

   public static AnnotationMirror asMirror(IAnnotationHandle handle) {
      return handle instanceof AnnotationHandle ? ((AnnotationHandle)handle).asMirror() : null;
   }

   public static AnnotationHandle of(AnnotationMirror annotation) {
      return new AnnotationHandle(annotation);
   }

   public static AnnotationHandle of(Element elem, Class<? extends Annotation> annotationClass) {
      return new AnnotationHandle(getAnnotation(elem, annotationClass));
   }
}
