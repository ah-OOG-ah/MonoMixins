package com.llamalad7.mixinextras.transformer;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public interface MixinTransformer {
   void transform(IMixinInfo var1, ClassNode var2);
}
