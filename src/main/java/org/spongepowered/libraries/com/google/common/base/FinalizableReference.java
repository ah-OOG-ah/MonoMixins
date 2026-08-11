package org.spongepowered.libraries.com.google.common.base;

import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@GwtIncompatible
public interface FinalizableReference {
   void finalizeReferent();
}
