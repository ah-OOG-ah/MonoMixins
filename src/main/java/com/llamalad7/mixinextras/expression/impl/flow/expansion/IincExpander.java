package com.llamalad7.mixinextras.expression.impl.flow.expansion;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.postprocessing.FlowPostProcessor;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class IincExpander extends InsnExpander {
   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      if (node.getInsn().getOpcode() == 132) {
         IincInsnNode iinc = (IincInsnNode)node.getInsn();
         FlowValue load = new FlowValue(Type.INT_TYPE, new VarInsnNode(21, iinc.var));
         this.registerComponent(load, IincExpander.Component.LOAD, iinc);
         FlowValue cst = new FlowValue(Type.INT_TYPE, ExpressionASMUtils.pushInt(iinc.incr));
         this.registerComponent(cst, IincExpander.Component.CST, iinc);
         FlowValue add = new FlowValue(Type.INT_TYPE, new InsnNode(96), load, cst);
         this.registerComponent(add, IincExpander.Component.ADD, iinc);
         node.setInsn(new VarInsnNode(54, iinc.var));
         node.setParents(add);
         this.registerComponent(node, IincExpander.Component.STORE, iinc);
         sink.registerFlow(load, cst, add);
      }
   }

   @Override
   public void expand(Target target, InjectionNodes.InjectionNode node, InsnExpander.Expansion expansion) {
      IincInsnNode iinc = (IincInsnNode)node.getCurrentTarget();
      target.method.maxStack += 2;
      this.expandInsn(
         target,
         node,
         expansion.registerInsn(IincExpander.Component.LOAD, new VarInsnNode(21, iinc.var)),
         expansion.registerInsn(IincExpander.Component.CST, ExpressionASMUtils.pushInt(iinc.incr)),
         expansion.registerInsn(IincExpander.Component.ADD, new InsnNode(96)),
         expansion.registerInsn(IincExpander.Component.STORE, new VarInsnNode(54, iinc.var))
      );
   }

   private static enum Component implements InsnExpander.InsnComponent {
      LOAD,
      CST,
      ADD,
      STORE;
   }
}
