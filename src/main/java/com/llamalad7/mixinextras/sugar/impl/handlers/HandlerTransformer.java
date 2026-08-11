package com.llamalad7.mixinextras.sugar.impl.handlers;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.llamalad7.mixinextras.service.MixinExtrasService;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.impl.SugarParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public abstract class HandlerTransformer {
   private static final Map<String, Class<? extends HandlerTransformer>> MAP = new HashMap<>();
   protected final IMixinInfo mixin;
   protected final SugarParameter parameter;

   HandlerTransformer(IMixinInfo mixin, SugarParameter parameter) {
      this.mixin = mixin;
      this.parameter = parameter;
   }

   public abstract boolean isRequired(MethodNode var1);

   public abstract void transform(HandlerInfo var1);

   public static HandlerTransformer create(IMixinInfo mixin, SugarParameter parameter) {
      try {
         Class<? extends HandlerTransformer> clazz = MAP.get(parameter.sugar.desc);
         if (clazz == null) {
            return null;
         } else {
            Constructor<? extends HandlerTransformer> ctor = clazz.getDeclaredConstructor(IMixinInfo.class, SugarParameter.class);
            return ctor.newInstance(mixin, parameter);
         }
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException var4) {
         throw new RuntimeException(var4);
      }
   }

   static {
      for (Pair<Class<? extends Annotation>, Class<? extends HandlerTransformer>> pair : Arrays.asList(Pair.of(Local.class, LocalHandlerTransformer.class))) {
         for (String name : MixinExtrasService.getInstance().getAllClassNames(pair.getLeft().getName())) {
            MAP.put('L' + name.replace('.', '/') + ';', pair.getRight());
         }
      }
   }
}
