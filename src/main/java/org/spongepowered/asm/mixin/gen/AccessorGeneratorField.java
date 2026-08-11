package org.spongepowered.asm.mixin.gen;

import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.util.Bytecode;

public abstract class AccessorGeneratorField extends AccessorGenerator {
   protected final FieldNode targetField;
   protected final Type targetType;

   public AccessorGeneratorField(AccessorInfo info) {
      super(info, Bytecode.isStatic(info.getTargetField()));
      this.targetField = info.getTargetField();
      this.targetType = info.getTargetFieldType();
      this.checkModifiers();
   }
}
