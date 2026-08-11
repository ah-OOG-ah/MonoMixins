package com.llamalad7.mixinextras.expression.impl.pool;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.expansion.InsnExpander;
import com.llamalad7.mixinextras.utils.InjectorUtils;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.modify.InvalidImplicitDiscriminatorException;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.util.Annotations;

public class LocalDef implements MemberDefinition {
   private final LocalVariableDiscriminator discriminator;
   private final InjectionInfo info;
   private final Type targetLocalType;
   private final boolean isArgsOnly;
   private final Target target;

   LocalDef(AnnotationNode local, InjectionInfo info, Target target) {
      this.discriminator = LocalVariableDiscriminator.parse(local);
      this.info = info;
      this.targetLocalType = Annotations.getValue(local, "type", Type.VOID_TYPE);
      this.isArgsOnly = Annotations.getValue(local, "argsOnly", Boolean.valueOf(false));
      this.target = target;
   }

   @Override
   public boolean matches(FlowValue node) {
      AbstractInsnNode virtualInsn = node.getInsn();
      if (!(virtualInsn instanceof VarInsnNode)) {
         return false;
      } else {
         VarInsnNode virtualVarInsn = (VarInsnNode)virtualInsn;
         AbstractInsnNode actualInsn = InsnExpander.getRepresentative(node);
         if (virtualVarInsn.getOpcode() >= 54 && virtualVarInsn.getOpcode() <= 58) {
            actualInsn = actualInsn.getNext();
         }

         LocalVariableDiscriminator.Context context = InjectorUtils.getOrCreateLocalContext(
            this.target, this.target.addInjectionNode(actualInsn), this.info, this.targetLocalType, this.isArgsOnly
         );
         if (this.discriminator.printLVT()) {
            InjectorUtils.printLocals(this.target, actualInsn, context, this.discriminator, this.targetLocalType, this.isArgsOnly);
            this.info.addCallbackInvocation(this.info.getMethod());
         }

         int index;
         try {
            index = this.discriminator.findLocal(context);
         } catch (InvalidImplicitDiscriminatorException var8) {
            return false;
         }

         return virtualVarInsn.var == index;
      }
   }
}
