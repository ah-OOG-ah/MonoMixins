package com.gtnewhorizon.gtnhmixins;

import io.github.legacymoddingmc.unimixins.gtnhmixins.repackage.common.abstraction.ComparableVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.service.MixinService;

public class Reflection {
   public static final Class<?> pluginWrapperClass;
   public static final Class<?> mixinsClass;
   public static final Class<?> configClass;
   public static final Class<?> mixinConfigClass;
   public static final Class<?> mixinTransformerClass;
   public static final Class<?> mixinProcessorClass;
   public static final Class<?> mixinServiceLaunchWrapperClass;
   public static final Class<?> mixinEnvironmentClass;
   public static final Field coreModInstanceField;
   public static final Field configField;
   public static final Field mixinClassesField;
   public static final Field processorField;
   public static final Field extensionsField;
   public static final Field delegatedTransformersField;
   public static final Method registerConfigurationMethod;
   public static final Method selectConfigsMethod;
   public static final Method prepareConfigsMethod;
   private static final boolean mixin_0_7;

   public static void invokeSelectConfigs(Object transformer, MixinEnvironment env) {
      try {
         if (!mixin_0_7) {
            Object processor = processorField.get(transformer);
            Extensions extensions = (Extensions)extensionsField.get(processor);
            selectConfigsMethod.invoke(processor, env);
         } else {
            selectConfigsMethod.invoke(transformer, env);
         }
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public static void invokePrepareConfigs(Object transformer, MixinEnvironment env) {
      try {
         if (!mixin_0_7) {
            Object processor = processorField.get(transformer);
            Extensions extensions = (Extensions)extensionsField.get(processor);
            prepareConfigsMethod.invoke(processor, env, extensions);
         } else {
            prepareConfigsMethod.invoke(transformer, env);
         }
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public static void setDelegatedTransformersField(Object newValue) {
      try {
         if (!mixin_0_7) {
            delegatedTransformersField.set(MixinService.getService(), newValue);
         } else {
            delegatedTransformersField.set(MixinEnvironment.getCurrentEnvironment(), newValue);
         }
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   static {
      try {
         mixin_0_7 = new ComparableVersion((String)Launch.blackboard.get("mixin.initialised")).compareTo(new ComparableVersion("0.8")) < 0;
         pluginWrapperClass = Class.forName("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper");
         coreModInstanceField = pluginWrapperClass.getField("coreModInstance");
         coreModInstanceField.setAccessible(true);
         mixinsClass = Class.forName("org.spongepowered.asm.mixin.Mixins");
         registerConfigurationMethod = mixinsClass.getDeclaredMethod("registerConfiguration", Config.class);
         registerConfigurationMethod.setAccessible(true);
         mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
         if (!mixin_0_7) {
            processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
         } else {
            processorField = null;
         }

         if (!mixin_0_7) {
            mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
            selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, Extensions.class);
            prepareConfigsMethod.setAccessible(true);
            extensionsField = mixinProcessorClass.getDeclaredField("extensions");
            extensionsField.setAccessible(true);
         } else {
            mixinProcessorClass = null;
            selectConfigsMethod = mixinTransformerClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            prepareConfigsMethod = mixinTransformerClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
            prepareConfigsMethod.setAccessible(true);
            extensionsField = mixinTransformerClass.getDeclaredField("extensions");
            extensionsField.setAccessible(true);
         }

         mixinServiceLaunchWrapperClass = Class.forName("org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper");
         mixinEnvironmentClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");
         if (!mixin_0_7) {
            delegatedTransformersField = mixinServiceLaunchWrapperClass.getDeclaredField("delegatedTransformers");
            delegatedTransformersField.setAccessible(true);
         } else {
            delegatedTransformersField = mixinEnvironmentClass.getDeclaredField("transformers");
            delegatedTransformersField.setAccessible(true);
         }

         configClass = Class.forName("org.spongepowered.asm.mixin.transformer.Config");
         configField = configClass.getDeclaredField("config");
         configField.setAccessible(true);
         mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
         mixinClassesField = mixinConfigClass.getDeclaredField("mixinClasses");
         mixinClassesField.setAccessible(true);
      } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException var1) {
         throw new RuntimeException(var1);
      }
   }
}
