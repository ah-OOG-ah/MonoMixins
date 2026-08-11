package com.llamalad7.mixinextras.ap.expressions;

import com.llamalad7.mixinextras.utils.MixinAPInternals;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;

public abstract class DefinitionInfo {
   private final ProcessingEnvironment processingEnv;
   private final TypeElement mixin;
   private final ExecutableElement handler;
   private final AnnotationHandle injector;
   private final AnnotationHandle at;

   public DefinitionInfo(
      String atType, ProcessingEnvironment processingEnv, TypeElement mixin, ExecutableElement handler, AnnotationHandle injector, String target, Boolean remap
   ) {
      this.processingEnv = processingEnv;
      this.mixin = mixin;
      this.handler = handler;
      this.injector = injector;
      this.at = AnnotationHandle.of(new DefinitionInfo.SyntheticAt(atType, target, remap));
   }

   public void remap() {
      MixinAPInternals.registerInjectionPoint(this.processingEnv, this.mixin, this.handler, this.injector, this.at);
   }

   public static class Field extends DefinitionInfo {
      public Field(ProcessingEnvironment processingEnv, TypeElement mixin, ExecutableElement handler, AnnotationHandle injector, String target, Boolean remap) {
         super("FIELD", processingEnv, mixin, handler, injector, target, remap);
      }
   }

   public static class Method extends DefinitionInfo {
      public Method(ProcessingEnvironment processingEnv, TypeElement mixin, ExecutableElement handler, AnnotationHandle injector, String target, Boolean remap) {
         super("INVOKE", processingEnv, mixin, handler, injector, target, remap);
      }
   }

   private class SyntheticAt implements AnnotationMirror {
      private final String type;
      private final String target;
      private final Boolean remap;
      private final DeclaredType atType;
      private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;

      public SyntheticAt(String type, String target, Boolean remap) {
         this.type = type;
         this.target = target;
         this.remap = remap;
         this.atType = DefinitionInfo.this.processingEnv
            .getTypeUtils()
            .getDeclaredType(DefinitionInfo.this.processingEnv.getElementUtils().getTypeElement(At.class.getName()));
         this.elementValues = this.makeElementValues();
      }

      @Override
      public DeclaredType getAnnotationType() {
         return this.atType;
      }

      @Override
      public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
         return this.elementValues;
      }

      private Map<? extends ExecutableElement, ? extends AnnotationValue> makeElementValues() {
         Map<ExecutableElement, AnnotationValue> result = new HashMap<>();
         result.put(this.getAtMethod("value"), this.makeStringConstant(this.type));
         result.put(this.getAtMethod("target"), this.makeStringConstant(this.target));
         if (this.remap != null) {
            result.put(this.getAtMethod("remap"), this.makeBooleanConstant(this.remap));
         }

         return result;
      }

      private ExecutableElement getAtMethod(String name) {
         for (Element e : this.atType.asElement().getEnclosedElements()) {
            if (e instanceof ExecutableElement && e.getSimpleName().contentEquals(name)) {
               return (ExecutableElement)e;
            }
         }

         throw new IllegalStateException(String.format("Could not find method %s in At! Please inform LlamaLad7!", name));
      }

      private AnnotationValue makeStringConstant(final String cst) {
         return new AnnotationValue() {
            @Override
            public Object getValue() {
               return cst;
            }

            @Override
            public String toString() {
               return '"' + cst + '"';
            }

            @Override
            public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
               return v.visitString(cst, p);
            }
         };
      }

      private AnnotationValue makeBooleanConstant(final boolean cst) {
         return new AnnotationValue() {
            @Override
            public Object getValue() {
               return cst;
            }

            @Override
            public String toString() {
               return Boolean.toString(cst);
            }

            @Override
            public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
               return v.visitBoolean(cst, p);
            }
         };
      }
   }
}
