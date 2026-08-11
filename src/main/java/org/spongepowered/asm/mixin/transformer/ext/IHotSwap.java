package org.spongepowered.asm.mixin.transformer.ext;

import org.spongepowered.asm.lib.tree.ClassNode;

public interface IHotSwap {
   void registerMixinClass(String var1);

   void registerTargetClass(String var1, ClassNode var2);
}
