package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public interface ITransformers extends IBaseTransformer {
   @Nonnull
   TransformerBuilder getBuilder();

   static <E extends Enum<E> & ITransformers> String[] getTransformers(Class<E> transformerEnum) {
      List<String> toLoad = new ArrayList<>();
      List<String> toNotLoad = new ArrayList<>();
      TransformerBuilder.loadTransformers(transformerEnum, toLoad, toNotLoad);
      GTNHMixins.log("Not loading the following transformers: {}", toNotLoad);

      for (String transformer : toLoad) {
         GTNHMixins.log("Loading ITransformer {}", transformer);
      }

      return toLoad.toArray(new String[0]);
   }
}
