package com.llamalad7.mixinextras.expression.impl.utils;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.utils.InsnReference;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class ComplexComparisonInfo extends ComparisonInfo {
   private final InsnReference jumpInsn;
   private final int jumpOpcode;

   public ComplexComparisonInfo(int comparison, FlowValue node, Type input, FlowValue jump, boolean jumpOnTrue) {
      super(comparison, node, input, jumpOnTrue);
      this.jumpInsn = new InsnReference(jump);
      this.jumpOpcode = jump.getInsn().getOpcode();
   }

   @Override
   public int copyJump(InsnList insns) {
      insns.add(new InsnNode(this.comparison));
      return this.jumpOpcode;
   }

   @Override
   public JumpInsnNode getJumpInsn(Target target) {
      return (JumpInsnNode)this.jumpInsn.getNode(target).getCurrentTarget();
   }

   @Override
   public void cleanup(Target target) {
      target.replaceNode(this.getJumpInsn(target), new InsnNode(0));
   }
}
