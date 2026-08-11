package com.llamalad7.mixinextras.injector.wrapmethod;

import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;
import com.llamalad7.mixinextras.sugar.impl.ShareInfo;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.OperationUtils;
import com.llamalad7.mixinextras.utils.UniquenessHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.TypeReference;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TypeAnnotationNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.util.Bytecode;

public abstract class WrapMethodStage {
   protected abstract MethodNode getVanillaMethod();

   public abstract MethodNode apply(ClassNode var1, LinkedHashSet<ShareInfo> var2);

   protected static MethodNode move(ClassNode targetClass, MethodNode original) {
      MethodNode newMethod = new MethodNode(
         original.access, UniquenessHelper.getUniqueMethodName(targetClass, original.name + "$mixinextras$wrapped"), original.desc, null, null
      );
      Bytecode.setVisibility(newMethod, Bytecode.Visibility.PRIVATE);
      newMethod.instructions = original.instructions;
      newMethod.instructions.resetLabels();
      original.instructions = new InsnList();
      newMethod.tryCatchBlocks = original.tryCatchBlocks;
      original.tryCatchBlocks = null;
      newMethod.localVariables = original.localVariables;
      original.localVariables = null;
      stripLocalVariableReferences(original.visibleTypeAnnotations);
      stripLocalVariableReferences(original.invisibleTypeAnnotations);
      original.visibleLocalVariableAnnotations = null;
      original.invisibleLocalVariableAnnotations = null;
      targetClass.methods.add(newMethod);
      return newMethod;
   }

   private static void stripLocalVariableReferences(List<TypeAnnotationNode> nodes) {
      if (nodes != null) {
         nodes.removeIf(it -> new TypeReference(it.typeRef).getSort() == 64);
      }
   }

   public static class Vanilla extends WrapMethodStage {
      private final MethodNode original;

      public Vanilla(MethodNode original) {
         this.original = original;
      }

      @Override
      protected MethodNode getVanillaMethod() {
         return this.original;
      }

      @Override
      public MethodNode apply(ClassNode targetClass, LinkedHashSet<ShareInfo> gatheredShares) {
         this.stripShareInitializers(gatheredShares);
         int shareStartIndex = Bytecode.getFirstNonArgLocalIndex(this.original);
         this.changeDesc(gatheredShares);
         this.fixLocals(shareStartIndex, new ArrayList<>(gatheredShares));
         return this.original;
      }

      private void stripShareInitializers(LinkedHashSet<ShareInfo> gatheredShares) {
         for (ShareInfo share : gatheredShares) {
            share.stripInitializerFrom(this.original);
         }
      }

      private void changeDesc(LinkedHashSet<ShareInfo> gatheredShares) {
         Type[] shareParams = gatheredShares.stream().map(it -> it.getShareType().getImplType()).toArray(Type[]::new);
         Type[] params = ArrayUtils.addAll(Type.getArgumentTypes(this.original.desc), shareParams);
         Type returnType = Type.getReturnType(this.original.desc);
         this.original.desc = Type.getMethodDescriptor(returnType, params);
      }

      private void fixLocals(int shareStartIndex, List<ShareInfo> allShares) {
         if (!allShares.isEmpty()) {
            Map<Integer, Integer> oldToNewShares = IntStream.range(0, allShares.size())
               .boxed()
               .collect(Collectors.toMap(i -> allShares.get(i).getLvtIndex(), i -> shareStartIndex + i));

            for (ShareInfo share : allShares) {
               share.setLvtIndex(oldToNewShares.get(share.getLvtIndex()));
            }

            IntUnaryOperator changeIndex = index -> {
               Integer newShare = oldToNewShares.get(index);
               if (newShare != null) {
                  return newShare;
               } else {
                  return index < shareStartIndex ? index : index + allShares.size();
               }
            };

            for (AbstractInsnNode insn : this.original.instructions.toArray()) {
               if (insn instanceof VarInsnNode) {
                  VarInsnNode varNode = (VarInsnNode)insn;
                  varNode.var = changeIndex.applyAsInt(varNode.var);
               } else if (insn instanceof IincInsnNode) {
                  IincInsnNode incNode = (IincInsnNode)insn;
                  incNode.var = changeIndex.applyAsInt(incNode.var);
               }
            }

            if (this.original.localVariables != null) {
               for (LocalVariableNode local : this.original.localVariables) {
                  local.index = changeIndex.applyAsInt(local.index);
               }
            }
         }
      }
   }

   public static class Wrapper extends WrapMethodStage {
      private final WrapMethodStage inner;
      private final MethodNode handler;
      private final Type operationType;
      private final List<ShareInfo> shares;
      private final boolean isStatic;

      public Wrapper(WrapMethodStage inner, MethodNode handler, Type operationType, List<ShareInfo> shares) {
         this.inner = inner;
         this.handler = handler;
         this.operationType = operationType;
         this.shares = shares;
         this.isStatic = Bytecode.isStatic(handler);
      }

      @Override
      protected MethodNode getVanillaMethod() {
         return this.inner.getVanillaMethod();
      }

      @Override
      public MethodNode apply(ClassNode targetClass, LinkedHashSet<ShareInfo> gatheredShares) {
         LinkedHashSet<ShareInfo> newShares = new LinkedHashSet<>(gatheredShares);
         List<ShareInfo> sharesToAllocate = new ArrayList<>();

         for (ShareInfo share : this.shares) {
            if (newShares.add(share)) {
               sharesToAllocate.add(share);
            }
         }

         MethodNode vanilla = this.getVanillaMethod();
         Type[] operationArgs = Type.getArgumentTypes(vanilla.desc);
         Type returnType = Type.getReturnType(vanilla.desc);
         MethodNode wrapper = this.inner.apply(targetClass, newShares);
         MethodNode inner = move(targetClass, wrapper);
         fixDesc(wrapper, sharesToAllocate.size());
         InsnList insns = new InsnList();
         allocateShares(sharesToAllocate, insns);
         if (!this.isStatic) {
            insns.add(new VarInsnNode(25, 0));
         }

         Bytecode.loadArgs(operationArgs, insns, this.isStatic ? 0 : 1);
         if (!this.isStatic) {
            insns.add(new VarInsnNode(25, 0));
         }

         loadShares(newShares, insns);
         Type[] trailing = newShares.stream().map(it -> it.getShareType().getImplType()).toArray(Type[]::new);
         OperationUtils.makeOperation(
            operationArgs, returnType, insns, !this.isStatic, trailing, targetClass, this.operationType, inner.name, (paramArrayIndex, loadArgs) -> {
               InsnList call = new InsnList();
               loadArgs.accept(call);
               call.add(ASMUtils.getInvokeInstruction(targetClass, inner));
               return call;
            }
         );
         loadShares(this.shares, insns);
         insns.add(ASMUtils.getInvokeInstruction(targetClass, this.handler));
         this.coerceReturnType(insns, returnType);
         insns.add(new InsnNode(returnType.getOpcode(172)));
         wrapper.instructions.add(insns);
         return wrapper;
      }

      private static void fixDesc(MethodNode wrapper, int shareCount) {
         Type[] argTypes = Type.getArgumentTypes(wrapper.desc);
         argTypes = ArrayUtils.subarray(argTypes, 0, argTypes.length - shareCount);
         wrapper.desc = Type.getMethodDescriptor(Type.getReturnType(wrapper.desc), argTypes);
      }

      private static void allocateShares(List<ShareInfo> sharesToAllocate, InsnList insns) {
         for (ShareInfo share : sharesToAllocate) {
            insns.add(share.initialize());
         }
      }

      private static void loadShares(Collection<ShareInfo> shares, InsnList insns) {
         for (ShareInfo share : shares) {
            insns.add(share.load());
         }
      }

      private void coerceReturnType(InsnList insns, Type expectedReturnType) {
         Type handlerReturnType = Type.getReturnType(this.handler.desc);
         if (expectedReturnType.getSort() >= 9 && !expectedReturnType.equals(handlerReturnType)) {
            insns.add(new TypeInsnNode(192, expectedReturnType.getInternalName()));
         }
      }
   }
}
