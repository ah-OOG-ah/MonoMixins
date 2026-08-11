package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;

public class NewArrayPostProcessor implements FlowPostProcessor {
   private final Comparator<Pair<FlowValue, Integer>> insnIndexComparator;

   public NewArrayPostProcessor(MethodNode method) {
      this.insnIndexComparator = Comparator.comparingInt(o -> method.instructions.indexOf(o.getLeft().getInsn()));
   }

   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      AbstractInsnNode insn = node.getInsn();
      if (insn.getOpcode() == 189 || insn.getOpcode() == 188) {
         List<FlowValue> stores = this.getCreationStores(node);
         if (stores == null || stores.isEmpty()) {
            return;
         }

         sink.markAsSynthetic(node.getInput(0));

         for (FlowValue store : stores) {
            sink.markAsSynthetic(store);
            sink.markAsSynthetic(store.getInput(1));
         }

         node.decorate("mixinextras_persistent_arrayCreationInfo", new ArrayCreationInfo(stores.get(stores.size() - 1)));
         node.setParents(stores.stream().map(it -> it.getInput(2)).toArray(FlowValue[]::new));
      }
   }

   private List<FlowValue> getCreationStores(FlowValue array) {
      Integer size = this.getIntConstant(array.getInput(0));
      if (size == null) {
         return null;
      } else {
         List<FlowValue> sortedNext = array.getNext()
            .stream()
            .filter(it -> !it.getLeft().isComplex())
            .sorted(this.insnIndexComparator)
            .map(Pair::getLeft)
            .collect(Collectors.toList());
         if (sortedNext.size() < size) {
            return null;
         } else {
            List<FlowValue> stores = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
               FlowValue store = sortedNext.get(i);
               if (!this.isStore(array, store, i)) {
                  return null;
               }

               stores.add(store);
            }

            return stores;
         }
      }
   }

   private Integer getIntConstant(FlowValue node) {
      if (node.isComplex()) {
         return null;
      } else {
         Object cst = ExpressionASMUtils.getConstant(node.getInsn());
         return !(cst instanceof Integer) ? null : (Integer)cst;
      }
   }

   private boolean isStore(FlowValue array, FlowValue store, int index) {
      int opcode = store.getInsn().getOpcode();
      if (opcode >= 79 && opcode <= 86) {
         return store.getInput(0) != array ? false : Objects.equals(index, this.getIntConstant(store.getInput(1)));
      } else {
         return false;
      }
   }
}
