package com.llamalad7.mixinextras.injector;

import com.llamalad7.mixinextras.utils.InjectorUtils;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(WrapWithCondition.class)
@InjectionInfo.HandlerPrefix("wrapWithCondition")
public class WrapWithConditionV1InjectionInfo extends MixinExtrasInjectionInfo {
   public WrapWithConditionV1InjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   @Override
   protected Injector parseInjector(AnnotationNode injectAnnotation) {
      return new WrapWithConditionInjector(this);
   }

   @Override
   public void prepare() {
      super.prepare();
      InjectorUtils.checkForImmediatePops(this.targetNodes);
   }
}
