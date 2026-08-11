package com.llamalad7.mixinextras.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.struct.InjectorRemap;

public class MixinAPInternals {
   private static final String ANNOTATED_MIXINS = "org.spongepowered.tools.obfuscation.AnnotatedMixins";
   private static final String ANNOTATED_MIXIN = "org.spongepowered.tools.obfuscation.AnnotatedMixin";
   private static final Class<?> ANNOTATED_MIXIN_CLASS;
   private static final InternalMethod<?, Object> ANNOTATED_MIXINS_GET_FOR_ENV;
   private static final InternalMethod<Object, Object> ANNOTATED_MIXINS_GET_MIXIN;
   private static final InternalMethod<?, Boolean> ANNOTATED_MIXINS_SHOULD_REMAP;
   private static final InternalMethod<Object, Void> ANNOTATED_MIXIN_REGISTER_INJECTION_POINT;
   private static final InternalMethod<Object, Void> ANNOTATED_MIXINS_WRITE_REFERENCES;

   public static void registerInjectionPoint(
      ProcessingEnvironment env, TypeElement mixin, ExecutableElement handler, AnnotationHandle injector, AnnotationHandle at
   ) {
      Object annotatedMixin = ANNOTATED_MIXINS_GET_MIXIN.call(ANNOTATED_MIXINS_GET_FOR_ENV.call(null, env), mixin);
      InjectorRemap remap = new InjectorRemap(ANNOTATED_MIXINS_SHOULD_REMAP.call(null, annotatedMixin, injector));
      ANNOTATED_MIXIN_REGISTER_INJECTION_POINT.call(annotatedMixin, handler, injector, "at", at, remap, "@At(%s)");
   }

   public static void writeReferences(ProcessingEnvironment env) {
      ANNOTATED_MIXINS_WRITE_REFERENCES.call(ANNOTATED_MIXINS_GET_FOR_ENV.call(null, env));
   }

   static {
      try {
         ANNOTATED_MIXIN_CLASS = Class.forName("org.spongepowered.tools.obfuscation.AnnotatedMixin");
      } catch (ClassNotFoundException var1) {
         throw new RuntimeException("Could not find class! Please report to LlamaLad7:", var1);
      }

      ANNOTATED_MIXINS_GET_FOR_ENV = InternalMethod.of(
         "org.spongepowered.tools.obfuscation.AnnotatedMixins", "getMixinsForEnvironment", ProcessingEnvironment.class
      );
      ANNOTATED_MIXINS_GET_MIXIN = InternalMethod.of("org.spongepowered.tools.obfuscation.AnnotatedMixins", "getMixin", TypeElement.class);
      ANNOTATED_MIXINS_SHOULD_REMAP = InternalMethod.of(
         "org.spongepowered.tools.obfuscation.AnnotatedMixins", "shouldRemap", ANNOTATED_MIXIN_CLASS, AnnotationHandle.class
      );
      ANNOTATED_MIXIN_REGISTER_INJECTION_POINT = InternalMethod.of(
         "org.spongepowered.tools.obfuscation.AnnotatedMixin",
         "registerInjectionPoint",
         ExecutableElement.class,
         AnnotationHandle.class,
         String.class,
         AnnotationHandle.class,
         InjectorRemap.class,
         String.class
      );
      ANNOTATED_MIXINS_WRITE_REFERENCES = InternalMethod.of("org.spongepowered.tools.obfuscation.AnnotatedMixins", "writeReferences");
   }
}
