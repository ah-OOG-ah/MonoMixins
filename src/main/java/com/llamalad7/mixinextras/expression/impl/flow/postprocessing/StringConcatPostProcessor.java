package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;

public class StringConcatPostProcessor implements FlowPostProcessor {
   private static final String STRING_BUILDER = Type.getInternalName(StringBuilder.class);

   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      FlowValue firstAppend = this.getFirstAppend(node);
      if (firstAppend != null) {
         List<FlowValue> appendCalls = new ArrayList<>();
         FlowValue currentAppend = firstAppend;

         while (true) {
            appendCalls.add(currentAppend);
            Collection<Pair<FlowValue, Integer>> next = currentAppend.getNext();
            if (next.size() != 1) {
               return;
            }

            Pair<FlowValue, Integer> child = next.iterator().next();
            if (!this.isAppendCall(child)) {
               if (this.isToStringCall(child)) {
                  FlowValue toStringCall = child.getLeft();
                  if (appendCalls.size() < 2) {
                     return;
                  }

                  this.decorateConcat(appendCalls, toStringCall);
                  return;
               }

               return;
            }

            currentAppend = child.getLeft();
         }
      }
   }

   private void decorateConcat(List<FlowValue> appendCalls, FlowValue toStringCall) {
      FlowValue initialComponent = appendCalls.get(0).getInput(1);

      for (int i = 1; i < appendCalls.size() - 1; i++) {
         appendCalls.get(i).decorate("stringConcatInfo", new StringConcatInfo(i == 1, true, initialComponent, toStringCall));
      }

      toStringCall.decorate("stringConcatInfo", new StringConcatInfo(appendCalls.size() == 2, false, initialComponent, toStringCall));
   }

   private FlowValue getFirstAppend(FlowValue node) {
      InstantiationInfo instantiation = node.getDecoration("instantiationInfo");
      if (instantiation == null || !instantiation.type.getInternalName().equals(STRING_BUILDER)) {
         return null;
      } else if (!this.isEmptyInit(instantiation.initCall)) {
         return null;
      } else if (node.getNext().size() != 1) {
         return null;
      } else {
         Pair<FlowValue, Integer> firstAppend = node.getNext().iterator().next();
         return this.isAppendCall(firstAppend) ? firstAppend.getLeft() : null;
      }
   }

   private boolean isEmptyInit(FlowValue call) {
      return ((MethodInsnNode)call.getInsn()).desc.equals("()V");
   }

   private boolean isAppendCall(Pair<FlowValue, Integer> child) {
      if (child.getRight() != 0) {
         return false;
      } else {
         AbstractInsnNode insn = child.getLeft().getInsn();
         if (insn.getOpcode() != 182) {
            return false;
         } else {
            MethodInsnNode call = (MethodInsnNode)insn;
            return call.owner.equals(STRING_BUILDER) && call.name.equals("append") && Type.getArgumentTypes(call.desc).length == 1;
         }
      }
   }

   private boolean isToStringCall(Pair<FlowValue, Integer> child) {
      if (child.getRight() != 0) {
         return false;
      } else {
         AbstractInsnNode insn = child.getLeft().getInsn();
         if (insn.getOpcode() != 182) {
            return false;
         } else {
            MethodInsnNode call = (MethodInsnNode)insn;
            return call.owner.equals(STRING_BUILDER) && call.name.equals("toString");
         }
      }
   }
}
