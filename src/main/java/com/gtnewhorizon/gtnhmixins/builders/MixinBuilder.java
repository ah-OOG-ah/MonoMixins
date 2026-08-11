package com.gtnewhorizon.gtnhmixins.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class MixinBuilder extends AbstractBuilder {
   public MixinBuilder() {
   }

   public MixinBuilder(String description) {
   }

   public MixinBuilder addCommonMixins(@Nonnull String... mixins) {
      return (MixinBuilder)super.addCommonClasses(mixins);
   }

   public MixinBuilder addClientMixins(@Nonnull String... mixins) {
      return (MixinBuilder)super.addClientClasses(mixins);
   }

   public MixinBuilder addServerMixins(@Nonnull String... mixins) {
      return (MixinBuilder)super.addServerClasses(mixins);
   }

   public MixinBuilder addSidedMixins(@Nonnull IBaseTransformer.Side side, @Nonnull String... mixins) {
      return (MixinBuilder)super.addSidedClasses(side, mixins);
   }

   public MixinBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
      return (MixinBuilder)super.setApplyIf(applyIf);
   }

   public MixinBuilder addRequiredMod(@Nonnull ITargetMod mod) {
      return (MixinBuilder)super.addRequiredMod(mod);
   }

   public MixinBuilder addExcludedMod(@Nonnull ITargetMod mod) {
      return (MixinBuilder)super.addExcludedMod(mod);
   }

   public MixinBuilder setPhase(IBaseTransformer.Phase phase) {
      this.phase = phase;
      return this;
   }

   protected static <E extends Enum<E> & IMixins> void loadMixins(Class<E> mixinsEnum, List<String> toLoad, List<String> toNotLoad) {
      List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, null, toNotLoad);
      Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
      loadClasses(builders, loadedTargets, toLoad, toNotLoad);
   }

   protected static <E extends Enum<E> & IMixins> void loadEarlyMixins(
      Class<E> mixinsEnum, Set<String> loadedCoreMods, List<String> toLoad, List<String> toNotLoad
   ) {
      List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, IBaseTransformer.Phase.EARLY, toNotLoad);
      Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, IBaseTransformer.Phase.EARLY, loadedCoreMods, Collections.emptySet());
      loadClasses(builders, loadedTargets, toLoad, toNotLoad);
   }

   protected static <E extends Enum<E> & IMixins> void loadLateMixins(Class<E> mixinsEnum, Set<String> loadedMods, List<String> toLoad, List<String> toNotLoad) {
      List<AbstractBuilder> builders = getEnabledBuildersForPhase(mixinsEnum, IBaseTransformer.Phase.LATE, toNotLoad);
      Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, IBaseTransformer.Phase.LATE, Collections.emptySet(), loadedMods);
      loadClasses(builders, loadedTargets, toLoad, toNotLoad);
   }

   private static <E extends Enum<E> & IMixins> List<AbstractBuilder> getEnabledBuildersForPhase(
      Class<E> mixinsEnum, IBaseTransformer.Phase loadingPhase, List<String> toNotLoad
   ) {
      E[] constants = (E[])mixinsEnum.getEnumConstants();
      List<AbstractBuilder> list = new ArrayList<>(constants.length + 1);

      for (E mixin : constants) {
         MixinBuilder builder = mixin.getBuilder();
         validateBuilder(builder, mixin, loadingPhase != null);
         if (builder.phase == loadingPhase) {
            if (builder.applyIf.get()) {
               list.add(builder);
            } else {
               builder.addAllClassesTo(toNotLoad);
            }
         }
      }

      return list;
   }

   private static void validateBuilder(MixinBuilder builder, Enum<?> mixin, boolean requirePhase) {
      if (builder == null) {
         throw new NullPointerException("Builder is null for IMixins : " + mixin.name());
      } else {
         int count = 0;
         if (builder.commonClasses != null) {
            count += builder.commonClasses.size();
         }

         if (builder.clientClasses != null) {
            count += builder.clientClasses.size();
         }

         if (builder.serverClasses != null) {
            count += builder.serverClasses.size();
         }

         if (count == 0) {
            throw new IllegalArgumentException("No mixin class registered for IMixins : " + mixin.name());
         } else if (requirePhase && builder.phase == null) {
            throw new IllegalArgumentException("No Phase specified for IMixins : " + mixin.name());
         }
      }
   }
}
