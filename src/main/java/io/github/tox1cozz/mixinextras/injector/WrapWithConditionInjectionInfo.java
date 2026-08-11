package io.github.tox1cozz.mixinextras.injector;

import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@InjectionInfo.AnnotationType(WrapWithCondition.class)
@InjectionInfo.HandlerPrefix("wrapWithCondition")
public class WrapWithConditionInjectionInfo extends InjectionInfo {
   static final String POPPED_OPERATION_DECORATOR = "mixinextras_operationIsImmediatelyPopped";

   public WrapWithConditionInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
      super(mixin, method, annotation);
   }

   @Override
   protected Injector parseInjector(AnnotationNode injectAnnotation) {
      return new WrapWithConditionInjector(this);
   }

   @Override
   public void prepare() {
      super.prepare();

      for (List<InjectionNodes.InjectionNode> nodeList : this.targetNodes.values()) {
         for (InjectionNodes.InjectionNode node : nodeList) {
            AbstractInsnNode currentTarget = node.getCurrentTarget();
            if (currentTarget instanceof MethodInsnNode) {
               Type returnType = Type.getReturnType(((MethodInsnNode)currentTarget).desc);
               if (this.isTypePoppedByInstruction(returnType, currentTarget.getNext())) {
                  node.decorate("mixinextras_operationIsImmediatelyPopped", true);
               }
            }
         }
      }
   }

   private boolean isTypePoppedByInstruction(Type type, AbstractInsnNode insn) {
      switch (type.getSize()) {
         case 1:
            return insn.getOpcode() == 87;
         case 2:
            return insn.getOpcode() == 88;
         default:
            return false;
      }
   }
}
