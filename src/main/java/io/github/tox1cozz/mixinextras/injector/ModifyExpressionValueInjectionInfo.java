package io.github.tox1cozz.mixinextras.injector;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(ModifyExpressionValue.class)
@InjectionInfo.HandlerPrefix("modifyExpressionValue")
public class ModifyExpressionValueInjectionInfo extends InjectionInfo {
   public ModifyExpressionValueInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   @Override
   protected Injector parseInjector(AnnotationNode injectAnnotation) {
      return new ModifyExpressionValueInjector(this);
   }
}
