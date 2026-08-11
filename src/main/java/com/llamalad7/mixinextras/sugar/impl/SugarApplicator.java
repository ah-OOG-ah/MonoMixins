package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.injector.StackExtension;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.llamalad7.mixinextras.service.MixinExtrasService;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

abstract class SugarApplicator {
   private static final Map<String, Class<? extends SugarApplicator>> MAP = new HashMap<>();
   protected final IMixinInfo mixin;
   protected final InjectionInfo info;
   protected final AnnotationNode sugar;
   protected final Type paramType;
   protected final Type paramGeneric;
   protected final int paramLvtIndex;
   protected final int paramIndex;

   SugarApplicator(InjectionInfo info, SugarParameter parameter) {
      this.mixin = CompatibilityHelper.getMixin(info).getMixin();
      this.info = info;
      this.sugar = parameter.sugar;
      this.paramType = parameter.type;
      this.paramGeneric = parameter.genericType;
      this.paramLvtIndex = parameter.lvtIndex;
      this.paramIndex = parameter.paramIndex;
   }

   abstract void validate(Target var1, InjectionNodes.InjectionNode var2);

   abstract void prepare(Target var1, InjectionNodes.InjectionNode var2);

   abstract void inject(Target var1, InjectionNodes.InjectionNode var2, StackExtension var3);

   int postProcessingPriority() {
      throw new UnsupportedOperationException(
         String.format("Sugar type %s does not support post-processing! Please inform LlamaLad7!", ASMUtils.annotationToString(this.sugar))
      );
   }

   static SugarApplicator create(InjectionInfo info, SugarParameter parameter) {
      try {
         Class<? extends SugarApplicator> clazz = MAP.get(parameter.sugar.desc);
         Constructor<? extends SugarApplicator> ctor = clazz.getDeclaredConstructor(InjectionInfo.class, SugarParameter.class);
         return ctor.newInstance(info, parameter);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException var4) {
         throw new RuntimeException(var4);
      }
   }

   static boolean isSugar(String desc) {
      return MAP.containsKey(desc);
   }

   static {
      for (Pair<Class<? extends Annotation>, Class<? extends SugarApplicator>> pair : Arrays.asList(
         Pair.of(Cancellable.class, CancellableSugarApplicator.class),
         Pair.of(Local.class, LocalSugarApplicator.class),
         Pair.of(Share.class, ShareSugarApplicator.class)
      )) {
         for (String name : MixinExtrasService.getInstance().getAllClassNames(pair.getLeft().getName())) {
            MAP.put('L' + name.replace('.', '/') + ';', pair.getRight());
         }
      }
   }
}
