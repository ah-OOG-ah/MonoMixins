package org.spongepowered.asm.launch;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService.Phase;
import java.util.EnumSet;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.ClassNode;

public interface IClassProcessor {
   EnumSet<Phase> handlesClass(Type var1, boolean var2, String var3);

   boolean processClass(Phase var1, ClassNode var2, Type var3, String var4);

   boolean generatesClass(Type var1);

   boolean generateClass(Type var1, ClassNode var2);
}
