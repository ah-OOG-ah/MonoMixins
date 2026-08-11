package org.spongepowered.libraries.com.google.common.eventbus;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.Queues;

abstract class Dispatcher {
   static Dispatcher perThreadDispatchQueue() {
      return new Dispatcher.PerThreadQueuedDispatcher();
   }

   static Dispatcher legacyAsync() {
      return new Dispatcher.LegacyAsyncDispatcher();
   }

   static Dispatcher immediate() {
      return Dispatcher.ImmediateDispatcher.INSTANCE;
   }

   abstract void dispatch(Object var1, Iterator<Subscriber> var2);

   private static final class ImmediateDispatcher extends Dispatcher {
      private static final Dispatcher.ImmediateDispatcher INSTANCE = new Dispatcher.ImmediateDispatcher();

      @Override
      void dispatch(Object event, Iterator<Subscriber> subscribers) {
         Preconditions.checkNotNull(event);

         while (subscribers.hasNext()) {
            subscribers.next().dispatchEvent(event);
         }
      }
   }

   private static final class LegacyAsyncDispatcher extends Dispatcher {
      private final ConcurrentLinkedQueue<Dispatcher.LegacyAsyncDispatcher.EventWithSubscriber> queue = Queues.newConcurrentLinkedQueue();

      private LegacyAsyncDispatcher() {
      }

      @Override
      void dispatch(Object event, Iterator<Subscriber> subscribers) {
         Preconditions.checkNotNull(event);

         while (subscribers.hasNext()) {
            this.queue.add(new Dispatcher.LegacyAsyncDispatcher.EventWithSubscriber(event, subscribers.next()));
         }

         Dispatcher.LegacyAsyncDispatcher.EventWithSubscriber e;
         while ((e = this.queue.poll()) != null) {
            e.subscriber.dispatchEvent(e.event);
         }
      }

      private static final class EventWithSubscriber {
         private final Object event;
         private final Subscriber subscriber;

         private EventWithSubscriber(Object event, Subscriber subscriber) {
            this.event = event;
            this.subscriber = subscriber;
         }
      }
   }

   private static final class PerThreadQueuedDispatcher extends Dispatcher {
      private final ThreadLocal<Queue<Dispatcher.PerThreadQueuedDispatcher.Event>> queue = new ThreadLocal<Queue<Dispatcher.PerThreadQueuedDispatcher.Event>>() {
         protected Queue<Dispatcher.PerThreadQueuedDispatcher.Event> initialValue() {
            return Queues.newArrayDeque();
         }
      };
      private final ThreadLocal<Boolean> dispatching = new ThreadLocal<Boolean>() {
         protected Boolean initialValue() {
            return false;
         }
      };

      private PerThreadQueuedDispatcher() {
      }

      @Override
      void dispatch(Object event, Iterator<Subscriber> subscribers) {
         Preconditions.checkNotNull(event);
         Preconditions.checkNotNull(subscribers);
         Queue<Dispatcher.PerThreadQueuedDispatcher.Event> queueForThread = this.queue.get();
         queueForThread.offer(new Dispatcher.PerThreadQueuedDispatcher.Event(event, subscribers));
         if (!this.dispatching.get()) {
            this.dispatching.set(true);

            Dispatcher.PerThreadQueuedDispatcher.Event nextEvent;
            try {
               while ((nextEvent = queueForThread.poll()) != null) {
                  while (nextEvent.subscribers.hasNext()) {
                     nextEvent.subscribers.next().dispatchEvent(nextEvent.event);
                  }
               }
            } finally {
               this.dispatching.remove();
               this.queue.remove();
            }
         }
      }

      private static final class Event {
         private final Object event;
         private final Iterator<Subscriber> subscribers;

         private Event(Object event, Iterator<Subscriber> subscribers) {
            this.event = event;
            this.subscribers = subscribers;
         }
      }
   }
}
