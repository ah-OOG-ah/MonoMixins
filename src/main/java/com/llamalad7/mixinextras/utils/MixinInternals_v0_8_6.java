package com.llamalad7.mixinextras.utils;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.selectors.TargetSelectors;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

public class MixinInternals_v0_8_6 {
   private static final InternalField<InjectionInfo, TargetSelectors> INJECTION_INFO_SELECTED_TARGETS = InternalField.of(InjectionInfo.class, "targets");

   public static Collection<MethodNode> getTargets(InjectionInfo info) {
      Iterable<TargetSelectors.SelectedMethod> targets = INJECTION_INFO_SELECTED_TARGETS.get(info);
      return StreamSupport.stream(targets.spliterator(), false).map(TargetSelectors.SelectedMethod::getMethod).collect(Collectors.toList());
   }
}
