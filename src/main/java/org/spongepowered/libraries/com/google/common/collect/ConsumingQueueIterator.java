package org.spongepowered.libraries.com.google.common.collect;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible
class ConsumingQueueIterator<T> extends AbstractIterator<T> {
   private final Queue<T> queue;

   ConsumingQueueIterator(T... elements) {
      this.queue = new ArrayDeque<>(elements.length);
      Collections.addAll(this.queue, elements);
   }

   ConsumingQueueIterator(Queue<T> queue) {
      this.queue = Preconditions.checkNotNull(queue);
   }

   @Override
   public T computeNext() {
      return this.queue.isEmpty() ? this.endOfData() : this.queue.remove();
   }
}
