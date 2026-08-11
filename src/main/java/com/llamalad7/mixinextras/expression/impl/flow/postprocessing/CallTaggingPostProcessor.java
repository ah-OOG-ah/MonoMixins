package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.util.Bytecode;

public class CallTaggingPostProcessor implements FlowPostProcessor {
   private final Type currentType;
   private final boolean isStatic;

   public CallTaggingPostProcessor(ClassNode classNode, MethodNode methodNode) {
      this.currentType = Type.getObjectType(classNode.name);
      this.isStatic = Bytecode.isStatic(methodNode);
   }

   @Override
   public void process(FlowValue node, FlowPostProcessor.OutputSink sink) {
      MethodCallType type = this.getType(node);
      if (type != null) {
         node.decorate("methodCallType", type);
         if (type == MethodCallType.SUPER) {
            sink.markAsSynthetic(node.getInput(0));
            node.removeParent(0);
         }
      }
   }

   private MethodCallType getType(FlowValue node) {
      if (!(node.getInsn() instanceof MethodInsnNode)) {
         return null;
      } else {
         MethodInsnNode call = (MethodInsnNode)node.getInsn();
         switch (call.getOpcode()) {
            case 182:
            case 185:
               return MethodCallType.NORMAL;
            case 183:
               if (call.name.equals("<init>")) {
                  return null;
               } else if (call.owner.equals(this.currentType.getInternalName())) {
                  return MethodCallType.NORMAL;
               } else if (this.isLoadThis(node.getInput(0))) {
                  return MethodCallType.SUPER;
               }
            default:
               return null;
            case 184:
               return MethodCallType.STATIC;
         }
      }
   }

   private boolean isLoadThis(FlowValue node) {
      if (!this.isStatic && !node.isComplex() && node.getInsn().getOpcode() == 25) {
         VarInsnNode load = (VarInsnNode)node.getInsn();
         return load.var == 0;
      } else {
         return false;
      }
   }
}
