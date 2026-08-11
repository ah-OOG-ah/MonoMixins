package com.llamalad7.mixinextras.wrapper.factory;

import com.llamalad7.mixinextras.transformer.MixinTransformer;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.Annotations;

public class FactoryRedirectWrapperMixinTransformer implements MixinTransformer {
   @Override
   public void transform(IMixinInfo mixinInfo, ClassNode mixinNode) {
      for (MethodNode method : mixinNode.methods) {
         AnnotationNode redirect = Annotations.getVisible(method, Redirect.class);
         if (redirect != null) {
            AnnotationNode at = Annotations.getValue(redirect, "at");
            if (at != null) {
               String value = Annotations.getValue(at);
               if ("NEW".equals(value)) {
                  this.wrapInjectorAnnotation(method, redirect);
               }
            }
         }
      }
   }

   private void wrapInjectorAnnotation(MethodNode method, AnnotationNode redirect) {
      AnnotationNode wrapped = new AnnotationNode(Type.getDescriptor(FactoryRedirectWrapper.class));
      wrapped.visit("original", redirect);
      method.visibleAnnotations.remove(redirect);
      method.visibleAnnotations.add(wrapped);
   }
}
