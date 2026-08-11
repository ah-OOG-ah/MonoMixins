package com.gtnewhorizon.gtnhmixins;

import java.util.List;
import java.util.Set;

public interface IEarlyMixinLoader {
   String getMixinConfig();

   List<String> getMixins(Set<String> var1);
}
