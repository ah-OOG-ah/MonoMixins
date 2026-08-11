package org.spongepowered.libraries.com.google.common.util.concurrent;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Optional;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.collect.ImmutableCollection;
import org.spongepowered.libraries.com.google.common.collect.ImmutableList;
import org.spongepowered.libraries.com.google.common.collect.Lists;

@GwtCompatible(emulated = true)
abstract class CollectionFuture<V, C> extends AggregateFuture<V, C> {
   abstract class CollectionFutureRunningState extends AggregateFuture<V, C>.RunningState {
      private List<Optional<V>> values;

      CollectionFutureRunningState(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed) {
         super(futures, allMustSucceed, true);
         this.values = (List<Optional<V>>)(futures.isEmpty() ? ImmutableList.of() : Lists.newArrayListWithCapacity(futures.size()));

         for (int i = 0; i < futures.size(); i++) {
            this.values.add(null);
         }
      }

      @Override
      final void collectOneValue(boolean allMustSucceed, int index, @Nullable V returnValue) {
         List<Optional<V>> localValues = this.values;
         if (localValues != null) {
            localValues.set(index, Optional.fromNullable(returnValue));
         } else {
            Preconditions.checkState(allMustSucceed || CollectionFuture.this.isCancelled(), "Future was done before all dependencies completed");
         }
      }

      @Override
      final void handleAllCompleted() {
         List<Optional<V>> localValues = this.values;
         if (localValues != null) {
            CollectionFuture.this.set((C)this.combine(localValues));
         } else {
            Preconditions.checkState(CollectionFuture.this.isDone());
         }
      }

      @Override
      void releaseResourcesAfterFailure() {
         super.releaseResourcesAfterFailure();
         this.values = null;
      }

      abstract C combine(List<Optional<V>> var1);
   }

   static final class ListFuture<V> extends CollectionFuture<V, List<V>> {
      ListFuture(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed) {
         this.init(new CollectionFuture.ListFuture.ListFutureRunningState(futures, allMustSucceed));
      }

      private final class ListFutureRunningState extends CollectionFuture<V, List<V>>.CollectionFutureRunningState {
         ListFutureRunningState(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed) {
            super(futures, allMustSucceed);
         }

         public List<V> combine(List<Optional<V>> values) {
            List<V> result = Lists.newArrayListWithCapacity(values.size());

            for (Optional<V> element : values) {
               result.add(element != null ? element.orNull() : null);
            }

            return Collections.unmodifiableList(result);
         }
      }
   }
}
