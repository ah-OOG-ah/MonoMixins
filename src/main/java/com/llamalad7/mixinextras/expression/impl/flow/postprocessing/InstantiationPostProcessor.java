package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import java.util.stream.IntStream;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.util.Bytecode;

public class InstantiationPostProcessor implements FlowPostProcessor {
   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      AbstractInsnNode insn = node.getInsn();
      if (insn.getOpcode() == 187) {
         Type newType = Type.getObjectType(((TypeInsnNode)insn).desc);
         FlowValue initCall = this.findInitCall(node);
         node.decorate("instantiationInfo", new InstantiationInfo(newType, initCall));
         sink.markAsSynthetic(initCall);
         node.setParents(IntStream.range(1, initCall.inputCount()).mapToObj(initCall::getInput).toArray(FlowValue[]::new));
      }
   }

   private FlowValue findInitCall(FlowValue newNode) {
      for (Pair<FlowValue, Integer> next : newNode.getNext()) {
         if (next.getRight() == 0) {
            FlowValue nextValue = next.getLeft();
            AbstractInsnNode nextInsn = nextValue.getInsn();
            if (nextInsn.getOpcode() == 183 && ((MethodInsnNode)nextInsn).name.equals("<init>") && nextValue.getInput(0) == newNode) {
               return nextValue;
            }
         }
      }

      throw new IllegalStateException("Could not find <init> call for " + Bytecode.describeNode(newNode.getInsn()));
   }
}
