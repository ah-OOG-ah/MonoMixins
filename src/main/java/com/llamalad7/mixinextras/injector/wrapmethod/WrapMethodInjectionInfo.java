package com.llamalad7.mixinextras.injector.wrapmethod;

import com.llamalad7.mixinextras.injector.MixinExtrasInjectionInfo;
import java.util.List;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(WrapMethod.class)
@InjectionInfo.HandlerPrefix("wrapMethod")
public class WrapMethodInjectionInfo extends MixinExtrasInjectionInfo {
   public WrapMethodInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   @Override
   protected Injector parseInjector(AnnotationNode injectAnnotation) {
      return new WrapMethodInjector(this);
   }

   @Override
   protected void parseInjectionPoints(List<AnnotationNode> ats) {
      this.injectionPoints.add(new WrapMethodInjectionPoint());
   }
}
