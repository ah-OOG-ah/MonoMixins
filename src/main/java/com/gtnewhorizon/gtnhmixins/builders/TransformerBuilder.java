package com.gtnewhorizon.gtnhmixins.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class TransformerBuilder extends AbstractBuilder {
   public TransformerBuilder() {
   }

   public TransformerBuilder(String description) {
   }

   public TransformerBuilder addCommonTransformers(@Nonnull String... transformers) {
      return (TransformerBuilder)super.addCommonClasses(transformers);
   }

   public TransformerBuilder addClientTransformers(@Nonnull String... transformers) {
      return (TransformerBuilder)super.addClientClasses(transformers);
   }

   public TransformerBuilder addServerTransformers(@Nonnull String... transformers) {
      return (TransformerBuilder)super.addServerClasses(transformers);
   }

   public TransformerBuilder addSidedTransformers(@Nonnull IBaseTransformer.Side side, @Nonnull String... transformers) {
      return (TransformerBuilder)super.addSidedClasses(side, transformers);
   }

   public TransformerBuilder setApplyIf(@Nonnull Supplier<Boolean> applyIf) {
      return (TransformerBuilder)super.setApplyIf(applyIf);
   }

   public TransformerBuilder addRequiredMod(@Nonnull ITargetMod mod) {
      return (TransformerBuilder)super.addRequiredMod(mod);
   }

   public TransformerBuilder addExcludedMod(@Nonnull ITargetMod mod) {
      return (TransformerBuilder)super.addExcludedMod(mod);
   }

   protected static <E extends Enum<E> & ITransformers> void loadTransformers(Class<E> transformerEnum, List<String> toLoad, List<String> toNotLoad) {
      List<AbstractBuilder> builders = getEnabledBuildersForPhase(transformerEnum, toNotLoad);
      Set<ITargetMod> loadedTargets = getLoadedTargetedMods(builders, null, Collections.emptySet(), Collections.emptySet());
      loadClasses(builders, loadedTargets, toLoad, toNotLoad);
   }

   private static <E extends Enum<E> & ITransformers> List<AbstractBuilder> getEnabledBuildersForPhase(Class<E> transformerEnum, List<String> toNotLoad) {
      E[] constants = (E[])transformerEnum.getEnumConstants();
      List<AbstractBuilder> list = new ArrayList<>(constants.length + 1);

      for (E transformer : constants) {
         TransformerBuilder builder = transformer.getBuilder();
         validateBuilder(builder, transformer);
         if (builder.applyIf.get()) {
            list.add(builder);
         } else {
            builder.addAllClassesTo(toNotLoad);
         }
      }

      return list;
   }

   private static void validateBuilder(TransformerBuilder builder, Enum<?> transformer) {
      if (builder == null) {
         throw new NullPointerException("Builder is null for ITransformer : " + transformer.name());
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
            throw new IllegalArgumentException("No transformer class registered for ITransformer : " + transformer.name());
         }
      }
   }
}
