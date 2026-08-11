package org.spongepowered.libraries.com.google.common.collect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.MoreObjects;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
public abstract class ImmutableTable<R, C, V> extends AbstractTable<R, C, V> implements Serializable {
   @Beta
   public static <T, R, C, V> Collector<T, ?, ImmutableTable<R, C, V>> toImmutableTable(
      Function<? super T, ? extends R> rowFunction, Function<? super T, ? extends C> columnFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(rowFunction);
      Preconditions.checkNotNull(columnFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         () -> new ImmutableTable.Builder(),
         (builder, t) -> builder.put((R)rowFunction.apply(t), (C)columnFunction.apply(t), (V)valueFunction.apply(t)),
         (b1, b2) -> b1.combine(b2),
         b -> b.build()
      );
   }

   public static <T, R, C, V> Collector<T, ?, ImmutableTable<R, C, V>> toImmutableTable(
      Function<? super T, ? extends R> rowFunction,
      Function<? super T, ? extends C> columnFunction,
      Function<? super T, ? extends V> valueFunction,
      BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(rowFunction);
      Preconditions.checkNotNull(columnFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collector.of(
         () -> new ImmutableTable.CollectorState(),
         (state, input) -> state.put((R)rowFunction.apply(input), (C)columnFunction.apply(input), (V)valueFunction.apply(input), mergeFunction),
         (s1, s2) -> s1.combine(s2, mergeFunction),
         state -> state.toTable()
      );
   }

   public static <R, C, V> ImmutableTable<R, C, V> of() {
      return (ImmutableTable<R, C, V>)SparseImmutableTable.EMPTY;
   }

   public static <R, C, V> ImmutableTable<R, C, V> of(R rowKey, C columnKey, V value) {
      return new SingletonImmutableTable<>(rowKey, columnKey, value);
   }

   public static <R, C, V> ImmutableTable<R, C, V> copyOf(Table<? extends R, ? extends C, ? extends V> table) {
      return table instanceof ImmutableTable ? (ImmutableTable)table : copyOf(table.cellSet());
   }

   private static <R, C, V> ImmutableTable<R, C, V> copyOf(Iterable<? extends Table.Cell<? extends R, ? extends C, ? extends V>> cells) {
      ImmutableTable.Builder<R, C, V> builder = builder();

      for (Table.Cell<? extends R, ? extends C, ? extends V> cell : cells) {
         builder.put(cell);
      }

      return builder.build();
   }

   public static <R, C, V> ImmutableTable.Builder<R, C, V> builder() {
      return new ImmutableTable.Builder<>();
   }

   static <R, C, V> Table.Cell<R, C, V> cellOf(R rowKey, C columnKey, V value) {
      return Tables.immutableCell(Preconditions.checkNotNull(rowKey), Preconditions.checkNotNull(columnKey), Preconditions.checkNotNull(value));
   }

   ImmutableTable() {
   }

   public ImmutableSet<Table.Cell<R, C, V>> cellSet() {
      return (ImmutableSet<Table.Cell<R, C, V>>)super.cellSet();
   }

   abstract ImmutableSet<Table.Cell<R, C, V>> createCellSet();

   final UnmodifiableIterator<Table.Cell<R, C, V>> cellIterator() {
      throw new AssertionError("should never be called");
   }

   @Override
   final Spliterator<Table.Cell<R, C, V>> cellSpliterator() {
      throw new AssertionError("should never be called");
   }

   public ImmutableCollection<V> values() {
      return (ImmutableCollection<V>)super.values();
   }

   abstract ImmutableCollection<V> createValues();

   @Override
   final Iterator<V> valuesIterator() {
      throw new AssertionError("should never be called");
   }

   public ImmutableMap<R, V> column(C columnKey) {
      Preconditions.checkNotNull(columnKey);
      return MoreObjects.firstNonNull((ImmutableMap<R, V>)this.columnMap().get(columnKey), ImmutableMap.of());
   }

   public ImmutableSet<C> columnKeySet() {
      return this.columnMap().keySet();
   }

   public abstract ImmutableMap<C, Map<R, V>> columnMap();

   public ImmutableMap<C, V> row(R rowKey) {
      Preconditions.checkNotNull(rowKey);
      return MoreObjects.firstNonNull((ImmutableMap<C, V>)this.rowMap().get(rowKey), ImmutableMap.of());
   }

   public ImmutableSet<R> rowKeySet() {
      return this.rowMap().keySet();
   }

   public abstract ImmutableMap<R, Map<C, V>> rowMap();

   @Override
   public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
      return this.get(rowKey, columnKey) != null;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.values().contains(value);
   }

   @Deprecated
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final V put(R rowKey, C columnKey, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public final void putAll(Table<? extends R, ? extends C, ? extends V> table) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @Override
   public final V remove(Object rowKey, Object columnKey) {
      throw new UnsupportedOperationException();
   }

   abstract ImmutableTable.SerializedForm createSerializedForm();

   final Object writeReplace() {
      return this.createSerializedForm();
   }

   public static final class Builder<R, C, V> {
      private final List<Table.Cell<R, C, V>> cells = Lists.newArrayList();
      private Comparator<? super R> rowComparator;
      private Comparator<? super C> columnComparator;

      @CanIgnoreReturnValue
      public ImmutableTable.Builder<R, C, V> orderRowsBy(Comparator<? super R> rowComparator) {
         this.rowComparator = Preconditions.checkNotNull(rowComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableTable.Builder<R, C, V> orderColumnsBy(Comparator<? super C> columnComparator) {
         this.columnComparator = Preconditions.checkNotNull(columnComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableTable.Builder<R, C, V> put(R rowKey, C columnKey, V value) {
         this.cells.add(ImmutableTable.cellOf(rowKey, columnKey, value));
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableTable.Builder<R, C, V> put(Table.Cell<? extends R, ? extends C, ? extends V> cell) {
         if (cell instanceof Tables.ImmutableCell) {
            Preconditions.checkNotNull(cell.getRowKey());
            Preconditions.checkNotNull(cell.getColumnKey());
            Preconditions.checkNotNull(cell.getValue());
            this.cells.add((Table.Cell<R, C, V>)cell);
         } else {
            this.put((R)cell.getRowKey(), (C)cell.getColumnKey(), (V)cell.getValue());
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableTable.Builder<R, C, V> putAll(Table<? extends R, ? extends C, ? extends V> table) {
         for (Table.Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
            this.put(cell);
         }

         return this;
      }

      ImmutableTable.Builder<R, C, V> combine(ImmutableTable.Builder<R, C, V> other) {
         this.cells.addAll(other.cells);
         return this;
      }

      public ImmutableTable<R, C, V> build() {
         int size = this.cells.size();
         switch (size) {
            case 0:
               return ImmutableTable.of();
            case 1:
               return new SingletonImmutableTable<>(Iterables.getOnlyElement(this.cells));
            default:
               return RegularImmutableTable.forCells(this.cells, this.rowComparator, this.columnComparator);
         }
      }
   }

   private static final class CollectorState<R, C, V> {
      final List<ImmutableTable.MutableCell<R, C, V>> insertionOrder = new ArrayList<>();
      final Table<R, C, ImmutableTable.MutableCell<R, C, V>> table = HashBasedTable.create();

      private CollectorState() {
      }

      void put(R row, C column, V value, BinaryOperator<V> merger) {
         ImmutableTable.MutableCell<R, C, V> oldCell = this.table.get(row, column);
         if (oldCell == null) {
            ImmutableTable.MutableCell<R, C, V> cell = new ImmutableTable.MutableCell<>(row, column, value);
            this.insertionOrder.add(cell);
            this.table.put(row, column, cell);
         } else {
            oldCell.merge(value, merger);
         }
      }

      ImmutableTable.CollectorState<R, C, V> combine(ImmutableTable.CollectorState<R, C, V> other, BinaryOperator<V> merger) {
         for (ImmutableTable.MutableCell<R, C, V> cell : other.insertionOrder) {
            this.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue(), merger);
         }

         return this;
      }

      ImmutableTable<R, C, V> toTable() {
         return ImmutableTable.copyOf(this.insertionOrder);
      }
   }

   private static final class MutableCell<R, C, V> extends Tables.AbstractCell<R, C, V> {
      private final R row;
      private final C column;
      private V value;

      MutableCell(R row, C column, V value) {
         this.row = Preconditions.checkNotNull(row);
         this.column = Preconditions.checkNotNull(column);
         this.value = Preconditions.checkNotNull(value);
      }

      @Override
      public R getRowKey() {
         return this.row;
      }

      @Override
      public C getColumnKey() {
         return this.column;
      }

      @Override
      public V getValue() {
         return this.value;
      }

      void merge(V value, BinaryOperator<V> mergeFunction) {
         Preconditions.checkNotNull(value);
         this.value = Preconditions.checkNotNull(mergeFunction.apply(this.value, value));
      }
   }

   static final class SerializedForm implements Serializable {
      private final Object[] rowKeys;
      private final Object[] columnKeys;
      private final Object[] cellValues;
      private final int[] cellRowIndices;
      private final int[] cellColumnIndices;
      private static final long serialVersionUID = 0L;

      private SerializedForm(Object[] rowKeys, Object[] columnKeys, Object[] cellValues, int[] cellRowIndices, int[] cellColumnIndices) {
         this.rowKeys = rowKeys;
         this.columnKeys = columnKeys;
         this.cellValues = cellValues;
         this.cellRowIndices = cellRowIndices;
         this.cellColumnIndices = cellColumnIndices;
      }

      static ImmutableTable.SerializedForm create(ImmutableTable<?, ?, ?> table, int[] cellRowIndices, int[] cellColumnIndices) {
         return new ImmutableTable.SerializedForm(
            table.rowKeySet().toArray(), table.columnKeySet().toArray(), table.values().toArray(), cellRowIndices, cellColumnIndices
         );
      }

      Object readResolve() {
         if (this.cellValues.length == 0) {
            return ImmutableTable.of();
         } else if (this.cellValues.length == 1) {
            return ImmutableTable.of(this.rowKeys[0], this.columnKeys[0], this.cellValues[0]);
         } else {
            ImmutableList.Builder<Table.Cell<Object, Object, Object>> cellListBuilder = new ImmutableList.Builder<>(this.cellValues.length);

            for (int i = 0; i < this.cellValues.length; i++) {
               cellListBuilder.add(ImmutableTable.cellOf(this.rowKeys[this.cellRowIndices[i]], this.columnKeys[this.cellColumnIndices[i]], this.cellValues[i]));
            }

            return RegularImmutableTable.forOrderedComponents(cellListBuilder.build(), ImmutableSet.copyOf(this.rowKeys), ImmutableSet.copyOf(this.columnKeys));
         }
      }
   }
}
