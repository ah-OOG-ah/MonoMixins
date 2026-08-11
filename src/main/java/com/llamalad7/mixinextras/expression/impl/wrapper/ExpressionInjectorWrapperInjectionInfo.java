package com.llamalad7.mixinextras.expression.impl.wrapper;

import com.llamalad7.mixinextras.wrapper.WrapperInjectionInfo;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(ExpressionInjectorWrapper.class)
@InjectionInfo.HandlerPrefix("expressionWrapper")
public class ExpressionInjectorWrapperInjectionInfo extends WrapperInjectionInfo {
   public ExpressionInjectorWrapperInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(ExpressionInjectorWrapperImpl::new, mixin, method, annotation);
   }
}
