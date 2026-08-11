package com.llamalad7.mixinextras.expression.impl.flow;

import java.util.function.Function;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public class ComputedFlowValue extends FlowValue {
   private final int size;
   private final Function<FlowValue[], Type> computer;

   public ComputedFlowValue(int size, Function<FlowValue[], Type> computer, AbstractInsnNode insn, FlowValue... parents) {
      super(null, insn, parents);
      this.size = size;
      this.computer = computer;
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public Type getType() {
      return this.computer.apply(this.parents);
   }
}
