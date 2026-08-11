package com.llamalad7.mixinextras.expression.impl.wrapper;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.transformer.MixinTransformer;
import com.llamalad7.mixinextras.utils.ASMUtils;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class ExpressionInjectorWrapperTransformer implements MixinTransformer {
   @Override
   public void transform(IMixinInfo mixinInfo, ClassNode mixinNode) {
      for (MethodNode method : mixinNode.methods) {
         if (ASMUtils.getRepeatedMEAnnotation(method, Expression.class) != null) {
            AnnotationNode ann = InjectionInfo.getInjectorAnnotation(mixinInfo, method);
            this.wrapInjectorAnnotation(method, ann);
         }
      }
   }

   private void wrapInjectorAnnotation(MethodNode method, AnnotationNode ann) {
      AnnotationNode wrapped = new AnnotationNode(Type.getDescriptor(ExpressionInjectorWrapper.class));
      wrapped.visit("original", ann);
      method.visibleAnnotations.remove(ann);
      method.visibleAnnotations.add(wrapped);
   }
}
