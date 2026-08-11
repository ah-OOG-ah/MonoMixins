package com.llamalad7.mixinextras.expression.impl.flow;

import com.llamalad7.mixinextras.expression.impl.utils.ExpressionASMUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.analysis.Analyzer;
import org.spongepowered.asm.lib.tree.analysis.AnalyzerException;
import org.spongepowered.asm.lib.tree.analysis.BasicValue;
import org.spongepowered.asm.lib.tree.analysis.Interpreter;
import org.spongepowered.asm.util.asm.ASM;

class LocalsCalculator extends Interpreter<BasicValue> {
   private final Map<VarInsnNode, Object> results = new IdentityHashMap<>();
   private final MethodNode methodNode;
   private final FlowContext context;

   public static Map<VarInsnNode, Type> getLocalTypes(ClassNode classNode, MethodNode methodNode, FlowContext ctx) {
      LocalsCalculator calculator = new LocalsCalculator(methodNode, ctx);

      try {
         new Analyzer<>(calculator).analyze(classNode.name, methodNode);
      } catch (AnalyzerException var6) {
         throw new RuntimeException(String.format("Failed to calculate locals for %s::%s%s: ", classNode.name, methodNode.name, methodNode.desc), var6);
      }

      for (Entry<VarInsnNode, Object> entry : calculator.results.entrySet()) {
         if (entry.getValue() instanceof Set) {
            entry.setValue(((Set)entry.getValue()).stream().reduce((type1, type2) -> ExpressionASMUtils.getCommonSupertype(ctx, type1, type2)).get());
         }
      }

      return calculator.results;
   }

   private LocalsCalculator(MethodNode methodNode, FlowContext ctx) {
      super(ASM.API_VERSION);
      this.methodNode = methodNode;
      this.context = ctx;
   }

   public BasicValue newValue(Type type) {
      if (type == Type.VOID_TYPE) {
         return null;
      } else {
         if (type == null) {
            type = ExpressionASMUtils.BOTTOM_TYPE;
         }

         return new BasicValue(type);
      }
   }

   public BasicValue newOperation(AbstractInsnNode insn) {
      return new BasicValue(ExpressionASMUtils.getNewType(insn));
   }

   public BasicValue copyOperation(AbstractInsnNode insn, BasicValue value) {
      if (insn.getOpcode() >= 21 && insn.getOpcode() <= 25) {
         VarInsnNode varNode = (VarInsnNode)insn;
         this.recordType(varNode, value.getType());
      }

      return value;
   }

   public BasicValue unaryOperation(AbstractInsnNode insn, BasicValue value) {
      return new BasicValue(ExpressionASMUtils.getUnaryType(insn));
   }

   public BasicValue binaryOperation(AbstractInsnNode insn, BasicValue value1, BasicValue value2) {
      return new BasicValue(ExpressionASMUtils.getBinaryType(insn, value1.getType()));
   }

   public BasicValue ternaryOperation(AbstractInsnNode insn, BasicValue value1, BasicValue value2, BasicValue value3) {
      return null;
   }

   public BasicValue naryOperation(AbstractInsnNode insn, List<? extends BasicValue> values) {
      return new BasicValue(ExpressionASMUtils.getNaryType(insn));
   }

   public void returnOperation(AbstractInsnNode insn, BasicValue value, BasicValue expected) {
   }

   public BasicValue merge(BasicValue value1, BasicValue value2) {
      return value1.equals(value2) ? value1 : new BasicValue(ExpressionASMUtils.getCommonSupertype(this.context, value1.getType(), value2.getType()));
   }

   private void recordType(VarInsnNode insn, Type type) {
      Object cached = this.results.get(insn);
      if (!(cached instanceof Type)) {
         if (cached instanceof Set) {
            ((Set)cached).add(type);
         }

         LocalVariableNode local = this.getLocalVariableAt(insn);
         this.results.put(insn, local != null ? Type.getType(local.desc) : new HashSet<>(Collections.singleton(type)));
      }
   }

   private LocalVariableNode getLocalVariableAt(VarInsnNode varInsn) {
      int pos = this.methodNode.instructions.indexOf(varInsn);
      int var = varInsn.var;
      if (this.methodNode.localVariables != null && !this.methodNode.localVariables.isEmpty()) {
         LocalVariableNode localVariableNode = null;

         for (LocalVariableNode local : this.methodNode.localVariables) {
            if (local.index == var && local.desc != null && this.isOpcodeInRange(this.methodNode.instructions, local, pos)) {
               localVariableNode = local;
            }
         }

         return localVariableNode;
      } else {
         return null;
      }
   }

   private boolean isOpcodeInRange(InsnList insns, LocalVariableNode local, int pos) {
      return insns.indexOf(local.start) <= pos && insns.indexOf(local.end) > pos;
   }
}
