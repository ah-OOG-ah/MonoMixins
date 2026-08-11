package org.spongepowered.tools.obfuscation.mirror;

import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.Bytecode;

public class MethodHandleASM extends MethodHandle {
   private final MethodNode method;

   public MethodHandleASM(TypeHandle owner, MethodNode method) {
      super(owner, method.name, method.desc);
      this.method = method;
   }

   @Override
   public String getJavaSignature() {
      return TypeUtils.getJavaSignature(this.method.desc);
   }

   @Override
   public Bytecode.Visibility getVisibility() {
      return Bytecode.getVisibility(this.method);
   }
}
