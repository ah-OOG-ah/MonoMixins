package org.spongepowered.asm.mixin.injection;

import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.selectors.ISelectorContext;
import org.spongepowered.asm.util.IMessageSink;

public interface IInjectionPointContext extends ISelectorContext, IMessageSink {
   MethodNode getMethod();

   AnnotationNode getAnnotationNode();
}
