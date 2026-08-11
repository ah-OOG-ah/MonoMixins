package com.llamalad7.mixinextras.expression.impl.flow;

import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import java.util.HashSet;
import java.util.Set;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public class ComplexFlowValue extends FlowValue {
   private final int size;
   private final Set<FlowValue> sources;
   private final FlowContext context;

   public ComplexFlowValue(int size, Set<FlowValue> sources, FlowContext context) {
      super(null, null, (FlowValue[])null);
      this.size = size;
      this.sources = sources;
      this.context = context;
   }

   @Override
   public void addChild(FlowValue value, int index) {
   }

   @Override
   public void finish() {
   }

   @Override
   public AbstractInsnNode getInsn() {
      throw ComplexDataException.INSTANCE;
   }

   @Override
   public FlowValue getInput(int index) {
      throw ComplexDataException.INSTANCE;
   }

   @Override
   public int inputCount() {
      return 0;
   }

   @Override
   public void mergeInputs(FlowValue[] newInputs, FlowContext ctx) {
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public FlowValue mergeWith(FlowValue other, FlowContext ctx) {
      if (this == other) {
         return this;
      } else {
         Set<FlowValue> newSources = new HashSet<>(this.sources);
         if (other instanceof ComplexFlowValue) {
            newSources.addAll(((ComplexFlowValue)other).sources);
         } else {
            newSources.add(other);
         }

         return new ComplexFlowValue(this.size, newSources, ctx);
      }
   }

   @Override
   public Type getType() {
      return this.sources.stream().map(FlowValue::getType).reduce((type1, type2) -> ExpressionASMUtils.getCommonSupertype(this.context, type1, type2)).get();
   }
}
