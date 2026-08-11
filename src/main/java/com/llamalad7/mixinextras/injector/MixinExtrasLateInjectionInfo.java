package com.llamalad7.mixinextras.injector;

import java.util.List;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public abstract class MixinExtrasLateInjectionInfo extends MixinExtrasInjectionInfo implements LateApplyingInjectorInfo {
   private LateApplyingInjectorInfo injectionInfoToQueue = this;
   private boolean hasInjectStarted = false;

   public MixinExtrasLateInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   public MixinExtrasLateInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation, String atKey) {
      super(mixin, method, annotation, atKey);
   }

   @Override
   public void inject() {
      this.hasInjectStarted = true;
      int callbackTotal = 0;

      for (List<InjectionNodes.InjectionNode> nodes : this.targetNodes.values()) {
         callbackTotal += nodes.size();
      }

      for (int i = 0; i < callbackTotal; i++) {
         super.addCallbackInvocation(this.method);
      }

      LateInjectionApplicatorExtension.offerInjection(this.mixin.getTarget(), this.injectionInfoToQueue);
   }

   @Override
   public void postInject() {
   }

   @Override
   public void addCallbackInvocation(MethodNode handler) {
      if (!this.hasInjectStarted) {
         super.addCallbackInvocation(handler);
      }
   }

   @Override
   public void lateInject() {
      super.inject();
   }

   @Override
   public void latePostInject() {
      super.postInject();
   }

   @Override
   public void wrap(LateApplyingInjectorInfo outer) {
      this.injectionInfoToQueue = outer;
   }
}
