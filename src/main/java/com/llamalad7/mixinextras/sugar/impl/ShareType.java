package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefClassGenerator;
import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefUtils;
import com.llamalad7.mixinextras.utils.ASMUtils;
import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class ShareType {
   private final Type innerType;

   public ShareType(Type innerType) {
      this.innerType = innerType;
   }

   public Type getInnerType() {
      return this.innerType;
   }

   public Type getImplType() {
      return Type.getObjectType(LocalRefClassGenerator.getForType(this.innerType));
   }

   public InsnList initialize(int lvtIndex) {
      InsnList init = new InsnList();
      LocalRefUtils.generateNew(init, this.innerType);
      init.add(new VarInsnNode(58, lvtIndex));
      init.add(new VarInsnNode(25, lvtIndex));
      init.add(new InsnNode(ASMUtils.getDummyOpcodeForType(this.innerType)));
      LocalRefUtils.generateInitialization(init, this.innerType);
      return init;
   }

   public void addToLvt(Target target, int lvtIndex) {
      LabelNode start = new LabelNode();
      target.insns.insert(start);
      LabelNode end = new LabelNode();
      target.insns.add(end);
      Type implType = Type.getObjectType(LocalRefClassGenerator.getForType(this.innerType));
      target.addLocalVariable(lvtIndex, "sharedRef" + lvtIndex, implType.getDescriptor());
      List<LocalVariableNode> lvt = target.method.localVariables;
      LocalVariableNode newVar = lvt.get(lvt.size() - 1);
      newVar.start = start;
      newVar.end = end;
   }
}
