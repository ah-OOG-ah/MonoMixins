package io.github.tox1cozz.mixinbooterlegacy;

import java.util.List;

public interface ILateMixinLoader {
   List<String> getMixinConfigs();

   default boolean shouldMixinConfigQueue(String mixinConfig) {
      return true;
   }

   default void onMixinConfigQueued(String mixinConfig) {
   }
}
