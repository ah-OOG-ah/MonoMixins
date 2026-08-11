package com.llamalad7.mixinextras.wrapper.factory;

import com.llamalad7.mixinextras.utils.MixinInternals;
import com.llamalad7.mixinextras.wrapper.InjectorWrapperImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.util.Annotations;

public class FactoryRedirectWrapperImpl extends InjectorWrapperImpl {
   private final InjectionInfo delegate;
   private final MethodNode handler;

   protected FactoryRedirectWrapperImpl(InjectionInfo wrapper, MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(wrapper, mixin, method, annotation, true);
      method.visibleAnnotations.remove(annotation);
      method.visibleAnnotations.add(Annotations.getValue(annotation, "original"));
      this.handler = method;
      this.delegate = InjectionInfo.parse(mixin, method);
   }

   @Override
   protected InjectionInfo getDelegate() {
      return this.delegate;
   }

   @Override
   protected MethodNode getHandler() {
      return this.handler;
   }

   @Override
   protected void granularInject(InjectorWrapperImpl.HandlerCallCallback callback) {
      Map<InjectionNodes.InjectionNode, List<InjectionNodes.InjectionNode>> replacements = new HashMap<>();

      for (Entry<Target, List<InjectionNodes.InjectionNode>> entry : MixinInternals.getTargets(this.delegate).entrySet()) {
         for (InjectionNodes.InjectionNode source : entry.getValue()) {
            this.findReplacedNodes(entry.getKey(), source, it -> replacements.computeIfAbsent(source, k -> new ArrayList<>()).add(it));
         }
      }

      super.granularInject((target, sourceNode, call) -> {
         callback.onFound(target, sourceNode, call);
         List<InjectionNodes.InjectionNode> replacedNodes = replacements.get(sourceNode);
         if (replacedNodes != null) {
            for (InjectionNodes.InjectionNode replaced : replacedNodes) {
               replaced.replace(call);
            }
         }
      });
   }

   private void findReplacedNodes(Target target, InjectionNodes.InjectionNode source, Consumer<InjectionNodes.InjectionNode> sink) {
      if (!source.isRemoved() && source.getCurrentTarget().getOpcode() == 187) {
         sink.accept(source);
         sink.accept(target.addInjectionNode(target.findInitNodeFor((TypeInsnNode)source.getCurrentTarget())));
      }
   }
}
