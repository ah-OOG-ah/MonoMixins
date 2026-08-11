package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.wrapper.WrapperInjectionInfo;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(SugarWrapper.class)
@InjectionInfo.HandlerPrefix("sugarWrapper")
public class SugarWrapperInjectionInfo extends WrapperInjectionInfo {
   public SugarWrapperInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(SugarWrapperImpl::new, mixin, method, annotation);
   }
}
