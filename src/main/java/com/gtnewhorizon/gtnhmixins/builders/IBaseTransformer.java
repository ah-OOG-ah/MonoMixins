package com.gtnewhorizon.gtnhmixins.builders;

public interface IBaseTransformer {
   public static enum Phase {
      EARLY,
      LATE;
   }

   public static enum Side {
      COMMON,
      CLIENT,
      SERVER;
   }
}
