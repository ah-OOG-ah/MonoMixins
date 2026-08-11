package com.llamalad7.mixinextras.expression.impl.pool;

import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.selectors.MatchResult;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;

class MethodDef implements SimpleMemberDefinition {
   private final MemberInfo memberInfo;

   public MethodDef(String method, InjectionInfo info) {
      this.memberInfo = CompatibilityHelper.parseMemberInfo(method, info);
   }

   @Override
   public boolean matches(AbstractInsnNode insn) {
      if (!(insn instanceof MethodInsnNode)) {
         return false;
      } else {
         MethodInsnNode methodNode = (MethodInsnNode)insn;
         return this.memberInfo.matches(methodNode.owner, methodNode.name, methodNode.desc) == MatchResult.EXACT_MATCH;
      }
   }

   @Override
   public boolean matches(Handle handle) {
      switch (handle.getTag()) {
         case 5:
         case 6:
         case 7:
         case 9:
            return this.memberInfo.matches(handle.getOwner(), handle.getName(), handle.getDesc()) == MatchResult.EXACT_MATCH;
         case 8:
         default:
            return false;
      }
   }
}
