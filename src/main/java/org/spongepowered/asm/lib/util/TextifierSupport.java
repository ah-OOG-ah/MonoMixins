package org.spongepowered.asm.lib.util;

import java.util.Map;
import org.spongepowered.asm.lib.Label;

public interface TextifierSupport {
   void textify(StringBuilder var1, Map<Label, String> var2);
}
