package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public interface IMixins extends IBaseTransformer {
   @Nonnull
   MixinBuilder getBuilder();

   static <E extends Enum<E> & IMixins> List<String> getMixins(Class<E> mixinsEnum) {
      List<String> toLoad = new ArrayList<>();
      List<String> toNotLoad = new ArrayList<>();
      MixinBuilder.loadMixins(mixinsEnum, toLoad, toNotLoad);
      GTNHMixins.log("Not loading the following mixins: {}", toNotLoad);

      for (String mixin : toLoad) {
         GTNHMixins.log("Loading {}", mixin);
      }

      return toLoad;
   }

   static <E extends Enum<E> & IMixins> List<String> getEarlyMixins(Class<E> mixinsEnum, Set<String> loadedCoreMods) {
      List<String> toLoad = new ArrayList<>();
      List<String> toNotLoad = new ArrayList<>();
      MixinBuilder.loadEarlyMixins(mixinsEnum, loadedCoreMods, toLoad, toNotLoad);
      GTNHMixins.log("Not loading the following EARLY mixins: {}", toNotLoad);
      return toLoad;
   }

   static <E extends Enum<E> & IMixins> List<String> getLateMixins(Class<E> mixinsEnum, Set<String> loadedMods) {
      List<String> toLoad = new ArrayList<>();
      List<String> toNotLoad = new ArrayList<>();
      MixinBuilder.loadLateMixins(mixinsEnum, loadedMods, toLoad, toNotLoad);
      GTNHMixins.log("Not loading the following LATE mixins: {}", toNotLoad.toString());
      return toLoad;
   }
}
