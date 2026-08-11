package org.spongepowered.include.com.google.common.collect;

public abstract class ForwardingObject {
   protected ForwardingObject() {
   }

   protected abstract Object delegate();

   @Override
   public String toString() {
      return this.delegate().toString();
   }
}
