package com.llamalad7.mixinextras.lib.antlr.runtime;

public class CommonTokenStream extends BufferedTokenStream {
   protected int channel = 0;

   public CommonTokenStream(TokenSource tokenSource) {
      super(tokenSource);
   }

   @Override
   protected int adjustSeekIndex(int i) {
      return this.nextTokenOnChannel(i, this.channel);
   }

   @Override
   protected Token LB(int k) {
      if (k != 0 && this.p - k >= 0) {
         int i = this.p;

         for (int n = 1; n <= k && i > 0; n++) {
            i = this.previousTokenOnChannel(i - 1, this.channel);
         }

         return i < 0 ? null : this.tokens.get(i);
      } else {
         return null;
      }
   }

   @Override
   public Token LT(int k) {
      this.lazyInit();
      if (k == 0) {
         return null;
      } else if (k < 0) {
         return this.LB(-k);
      } else {
         int i = this.p;

         for (int n = 1; n < k; n++) {
            if (this.sync(i + 1)) {
               i = this.nextTokenOnChannel(i + 1, this.channel);
            }
         }

         return this.tokens.get(i);
      }
   }
}
