package com.llamalad7.mixinextras.wrapper;

import com.llamalad7.mixinextras.injector.LateApplyingInjectorInfo;
import com.llamalad7.mixinextras.sugar.impl.SingleIterationList;
import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import com.llamalad7.mixinextras.utils.MixinInternals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

public abstract class InjectorWrapperImpl {
   private final InjectionInfo wrapperInfo;
   protected final ClassNode classNode;
   private final boolean useGranularInject;

   protected InjectorWrapperImpl(InjectionInfo wrapper, MixinTargetContext mixin, MethodNode method, AnnotationNode annotation, boolean useGranularInject) {
      this.wrapperInfo = wrapper;
      this.classNode = mixin.getTargetClassNode();
      this.useGranularInject = useGranularInject;
   }

   public boolean usesGranularInject() {
      return this.useGranularInject;
   }

   protected abstract InjectionInfo getDelegate();

   protected abstract MethodNode getHandler();

   protected boolean isValid() {
      return this.getDelegate().isValid();
   }

   public int getOrder() {
      return CompatibilityHelper.getOrder(this.getDelegate());
   }

   protected void prepare() {
      this.getDelegate().prepare();
      MethodNode handler = this.getHandler();
      handler.visibleAnnotations.remove(InjectionInfo.getInjectorAnnotation(CompatibilityHelper.getMixin(this.wrapperInfo).getMixin(), handler));
   }

   protected void preInject() {
      CompatibilityHelper.preInject(this.getDelegate());
   }

   protected void doInject() {
      if (this.useGranularInject) {
         this.granularInject((target, node, call) -> {});
      } else {
         if (this.getDelegate() instanceof LateApplyingInjectorInfo) {
            ((LateApplyingInjectorInfo)this.getDelegate()).lateInject();
         } else {
            this.getDelegate().inject();
         }
      }
   }

   protected void granularInject(InjectorWrapperImpl.HandlerCallCallback callback) {
      InjectionInfo delegate = this.getDelegate();
      if (delegate instanceof WrapperInjectionInfo) {
         WrapperInjectionInfo wrapper = (WrapperInjectionInfo)delegate;
         wrapper.impl.granularInject(callback);
      } else {
         this.doGranularInject(callback);
      }
   }

   protected void doPostInject(Runnable postInject) {
      postInject.run();
   }

   protected void addCallbackInvocation(MethodNode handler) {
      this.getDelegate().addCallbackInvocation(handler);
   }

   protected RuntimeException granularInjectNotSupported() {
      return new IllegalStateException(this.getDelegate().getClass() + " does not support granular injection! Please report to LlamaLad7!");
   }

   private void doGranularInject(InjectorWrapperImpl.HandlerCallCallback callback) {
      InjectionInfo delegate = this.getDelegate();
      Map<Target, List<InjectionNodes.InjectionNode>> targets = MixinInternals.getTargets(delegate);
      Injector injector = MixinInternals.getInjector(delegate);

      for (Entry<Target, List<InjectionNodes.InjectionNode>> entry : targets.entrySet()) {
         Target target = entry.getKey();
         Set<MethodInsnNode> discoveredHandlerCalls = new HashSet<>(this.findHandlerCalls(target));

         for (InjectionNodes.InjectionNode node : entry.getValue()) {
            inject(injector, target, node);

            for (MethodInsnNode handlerCall : this.findHandlerCalls(target)) {
               if (discoveredHandlerCalls.add(handlerCall)) {
                  callback.onFound(target, node, handlerCall);
               }
            }
         }

         postInject(injector, target, entry.getValue());
      }

      targets.clear();
   }

   private List<MethodInsnNode> findHandlerCalls(Target target) {
      MethodNode handler = this.getHandler();
      List<MethodInsnNode> result = new ArrayList<>();

      for (AbstractInsnNode insn : target) {
         if (insn instanceof MethodInsnNode) {
            MethodInsnNode call = (MethodInsnNode)insn;
            if (call.owner.equals(this.classNode.name) && call.name.equals(handler.name) && call.desc.equals(handler.desc)) {
               result.add(call);
            }
         }
      }

      return result;
   }

   private static void inject(Injector injector, Target target, InjectionNodes.InjectionNode node) {
      injector.inject(target, new SingleIterationList<>(Collections.singletonList(node), 0));
   }

   private static void postInject(Injector injector, Target target, List<InjectionNodes.InjectionNode> nodes) {
      injector.inject(target, new SingleIterationList<>(nodes, 1));
   }

   @FunctionalInterface
   public interface Factory {
      InjectorWrapperImpl create(InjectionInfo var1, MixinTargetContext var2, MethodNode var3, AnnotationNode var4);
   }

   @FunctionalInterface
   public interface HandlerCallCallback {
      void onFound(Target var1, InjectionNodes.InjectionNode var2, MethodInsnNode var3);
   }
}
