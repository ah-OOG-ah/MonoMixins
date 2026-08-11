package org.spongepowered.asm.lib.commons;

import org.spongepowered.asm.lib.Label;

public interface TableSwitchGenerator {
   void generateCase(int var1, Label var2);

   void generateDefault();
}
