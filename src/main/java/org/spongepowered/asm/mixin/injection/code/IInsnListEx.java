package org.spongepowered.asm.mixin.injection.code;

import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public interface IInsnListEx {
   String getTargetName();

   String getTargetDesc();

   String getTargetSignature();

   int getTargetAccess();

   boolean isTargetStatic();

   boolean isTargetConstructor();

   boolean isTargetStaticInitialiser();

   AbstractInsnNode getSpecialNode(IInsnListEx.SpecialNodeType var1);

   boolean hasDecoration(String var1);

   <V> V getDecoration(String var1);

   <V> V getDecoration(String var1, V var2);

   public static enum SpecialNodeType {
      DELEGATE_CTOR,
      INITIALISER_INJECTION_POINT,
      CTOR_BODY;
   }
}
