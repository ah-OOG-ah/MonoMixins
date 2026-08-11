package com.llamalad7.mixinextras.expression.impl.ast.identifiers;

import com.llamalad7.mixinextras.expression.impl.pool.IdentifierPool;
import org.spongepowered.asm.lib.Type;

public interface TypeIdentifier {
   boolean matches(IdentifierPool var1, Type var2);
}
