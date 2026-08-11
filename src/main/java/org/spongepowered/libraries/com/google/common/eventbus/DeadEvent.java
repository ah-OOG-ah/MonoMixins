package org.spongepowered.libraries.com.google.common.eventbus;

import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.base.MoreObjects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
public class DeadEvent {
   private final Object source;
   private final Object event;

   public DeadEvent(Object source, Object event) {
      this.source = Preconditions.checkNotNull(source);
      this.event = Preconditions.checkNotNull(event);
   }

   public Object getSource() {
      return this.source;
   }

   public Object getEvent() {
      return this.event;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("source", this.source).add("event", this.event).toString();
   }
}
