package com.llamalad7.mixinextras.expression.impl.flow.expansion;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.postprocessing.FlowPostProcessor;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class UnaryComparisonExpander extends InsnExpander {
   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      AbstractInsnNode insn = node.getInsn();
      int cstOpcode = this.getCstOpcode(insn);
      if (cstOpcode != -1) {
         JumpInsnNode jump = (JumpInsnNode)insn;
         int jumpOpcode = this.getExpandedJumpOpcode(insn);
         if (this.isComplexComparison(node.getInput(0))) {
            node.getInput(0).decorate("complexComparisonJump", node);
            sink.markAsSynthetic(node);
         } else {
            AbstractInsnNode cstInsn = new InsnNode(cstOpcode);
            FlowValue cst = new FlowValue(ExpressionASMUtils.getNewType(cstInsn), cstInsn);
            this.registerComponent(cst, UnaryComparisonExpander.Component.CST, jump);
            node.setInsn(new JumpInsnNode(jumpOpcode, jump.label));
            node.setParents(node.getInput(0), cst);
            this.registerComponent(node, UnaryComparisonExpander.Component.JUMP, jump);
            sink.registerFlow(cst);
         }
      }
   }

   @Override
   public void expand(Target target, InjectionNodes.InjectionNode node, InsnExpander.Expansion expansion) {
      if (node.isReplaced()) {
         AbstractInsnNode next = node.getCurrentTarget().getNext();
         if (!(next instanceof JumpInsnNode)) {
            throw new IllegalStateException("Could not find jump for expanded @ModifyConstant comparison! Please inform LlamaLad7!");
         } else {
            JumpInsnNode jump = (JumpInsnNode)next;
            expansion.registerInsn(UnaryComparisonExpander.Component.CST, node.getCurrentTarget());
            expansion.registerInsn(UnaryComparisonExpander.Component.JUMP, jump);
         }
      } else {
         AbstractInsnNode insn = node.getCurrentTarget();
         int cstOpcode = this.getCstOpcode(insn);
         if (cstOpcode != -1) {
            JumpInsnNode jump = (JumpInsnNode)insn;
            int jumpOpcode = this.getExpandedJumpOpcode(insn);
            target.method.maxStack++;
            this.expandInsn(
               target,
               node,
               expansion.registerInsn(UnaryComparisonExpander.Component.CST, new InsnNode(cstOpcode)),
               expansion.registerInsn(UnaryComparisonExpander.Component.JUMP, new JumpInsnNode(jumpOpcode, jump.label))
            );
         }
      }
   }

   private int getCstOpcode(AbstractInsnNode insn) {
      if (153 <= insn.getOpcode() && insn.getOpcode() <= 158) {
         return 3;
      } else {
         return insn.getOpcode() != 198 && insn.getOpcode() != 199 ? -1 : 1;
      }
   }

   private int getExpandedJumpOpcode(AbstractInsnNode insn) {
      if (153 <= insn.getOpcode() && insn.getOpcode() <= 158) {
         return insn.getOpcode() + 6;
      } else {
         return insn.getOpcode() != 198 && insn.getOpcode() != 199 ? -1 : insn.getOpcode() - 33;
      }
   }

   private boolean isComplexComparison(FlowValue node) {
      if (node.isComplex()) {
         return false;
      } else {
         AbstractInsnNode insn = node.getInsn();
         return 148 <= insn.getOpcode() && insn.getOpcode() <= 152;
      }
   }

   private static enum Component implements InsnExpander.InsnComponent {
      CST,
      JUMP;
   }
}
