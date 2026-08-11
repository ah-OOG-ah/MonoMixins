package com.gtnewhorizon.gtnhmixins;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public interface ILateMixinLoader {
   String getMixinConfig();

   @Nonnull
   List<String> getMixins(Set<String> var1);
}
