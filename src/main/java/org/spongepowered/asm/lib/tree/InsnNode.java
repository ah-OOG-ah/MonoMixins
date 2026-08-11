package org.spongepowered.asm.lib.tree;

import java.util.Map;
import org.spongepowered.asm.lib.MethodVisitor;

public class InsnNode extends AbstractInsnNode {
   public InsnNode(int opcode) {
      super(opcode);
   }

   @Override
   public int getType() {
      return 0;
   }

   @Override
   public void accept(MethodVisitor methodVisitor) {
      methodVisitor.visitInsn(this.opcode);
      this.acceptAnnotations(methodVisitor);
   }

   @Override
   public AbstractInsnNode clone(Map<LabelNode, LabelNode> clonedLabels) {
      return new InsnNode(this.opcode).cloneAnnotations(this);
   }
}
