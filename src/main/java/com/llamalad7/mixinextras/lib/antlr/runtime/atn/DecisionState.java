package com.llamalad7.mixinextras.lib.antlr.runtime.atn;

public abstract class DecisionState extends ATNState {
   public int decision = -1;
   public boolean nonGreedy;
}
