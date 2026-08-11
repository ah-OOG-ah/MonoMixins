package com.llamalad7.mixinextras.injector;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(ModifyReceiver.class)
@InjectionInfo.HandlerPrefix("modifyReceiver")
public class ModifyReceiverInjectionInfo extends MixinExtrasInjectionInfo {
   public ModifyReceiverInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   @Override
   protected Injector parseInjector(AnnotationNode injectAnnotation) {
      return new ModifyReceiverInjector(this);
   }
}
