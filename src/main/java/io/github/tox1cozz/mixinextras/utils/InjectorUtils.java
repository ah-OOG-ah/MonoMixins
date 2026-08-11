package io.github.tox1cozz.mixinextras.utils;

import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;

public class InjectorUtils {
   public static boolean isVirtualRedirect(InjectionNodes.InjectionNode node) {
      return node.isReplaced() && node.hasDecoration("redirector") && node.getCurrentTarget().getOpcode() != 184;
   }
}
