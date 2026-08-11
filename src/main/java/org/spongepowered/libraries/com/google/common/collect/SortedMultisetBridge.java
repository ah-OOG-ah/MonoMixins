package org.spongepowered.libraries.com.google.common.collect;

import java.util.SortedSet;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@GwtIncompatible
interface SortedMultisetBridge<E> extends Multiset<E> {
   SortedSet<E> elementSet();
}
