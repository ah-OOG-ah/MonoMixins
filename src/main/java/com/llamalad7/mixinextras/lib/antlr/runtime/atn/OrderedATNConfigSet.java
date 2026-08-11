package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

import com.llamalad7.mixinextras.lib.antlr.runtime.misc.ObjectEqualityComparator;

public class OrderedATNConfigSet extends ATNConfigSet {
   public OrderedATNConfigSet() {
      this.configLookup = new OrderedATNConfigSet.LexerConfigHashSet();
   }

   public static class LexerConfigHashSet extends ATNConfigSet.AbstractConfigHashSet {
      public LexerConfigHashSet() {
         super(ObjectEqualityComparator.INSTANCE);
      }
   }
}
