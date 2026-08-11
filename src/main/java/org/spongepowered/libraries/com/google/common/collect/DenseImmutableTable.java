package org.spongepowered.libraries.com.google.common.collect;

import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@GwtCompatible
@Immutable
final class DenseImmutableTable<R, C, V> extends RegularImmutableTable<R, C, V> {
   private final ImmutableMap<R, Integer> rowKeyToIndex;
   private final ImmutableMap<C, Integer> columnKeyToIndex;
   private final ImmutableMap<R, Map<C, V>> rowMap;
   private final ImmutableMap<C, Map<R, V>> columnMap;
   private final int[] rowCounts;
   private final int[] columnCounts;
   private final V[][] values;
   private final int[] cellRowIndices;
   private final int[] cellColumnIndices;

   DenseImmutableTable(ImmutableList<Table.Cell<R, C, V>> cellList, ImmutableSet<R> rowSpace, ImmutableSet<C> columnSpace) {
      V[][] array = (V[][])(new Object[rowSpace.size()][columnSpace.size()]);
      this.values = array;
      this.rowKeyToIndex = Maps.indexMap(rowSpace);
      this.columnKeyToIndex = Maps.indexMap(columnSpace);
      this.rowCounts = new int[this.rowKeyToIndex.size()];
      this.columnCounts = new int[this.columnKeyToIndex.size()];
      int[] cellRowIndices = new int[cellList.size()];
      int[] cellColumnIndices = new int[cellList.size()];

      for (int i = 0; i < cellList.size(); i++) {
         Table.Cell<R, C, V> cell = cellList.get(i);
         R rowKey = cell.getRowKey();
         C columnKey = cell.getColumnKey();
         int rowIndex = this.rowKeyToIndex.get(rowKey);
         int columnIndex = this.columnKeyToIndex.get(columnKey);
         V existingValue = this.values[rowIndex][columnIndex];
         Preconditions.checkArgument(existingValue == null, "duplicate key: (%s, %s)", rowKey, columnKey);
         this.values[rowIndex][columnIndex] = cell.getValue();
         this.rowCounts[rowIndex]++;
         this.columnCounts[columnIndex]++;
         cellRowIndices[i] = rowIndex;
         cellColumnIndices[i] = columnIndex;
      }

      this.cellRowIndices = cellRowIndices;
      this.cellColumnIndices = cellColumnIndices;
      this.rowMap = new DenseImmutableTable.RowMap();
      this.columnMap = new DenseImmutableTable.ColumnMap();
   }

   @Override
   public ImmutableMap<C, Map<R, V>> columnMap() {
      return this.columnMap;
   }

   @Override
   public ImmutableMap<R, Map<C, V>> rowMap() {
      return this.rowMap;
   }

   @Override
   public V get(@Nullable Object rowKey, @Nullable Object columnKey) {
      Integer rowIndex = this.rowKeyToIndex.get(rowKey);
      Integer columnIndex = this.columnKeyToIndex.get(columnKey);
      return rowIndex != null && columnIndex != null ? this.values[rowIndex][columnIndex] : null;
   }

   @Override
   public int size() {
      return this.cellRowIndices.length;
   }

   @Override
   Table.Cell<R, C, V> getCell(int index) {
      int rowIndex = this.cellRowIndices[index];
      int columnIndex = this.cellColumnIndices[index];
      R rowKey = this.rowKeySet().asList().get(rowIndex);
      C columnKey = this.columnKeySet().asList().get(columnIndex);
      V value = this.values[rowIndex][columnIndex];
      return cellOf(rowKey, columnKey, value);
   }

   @Override
   V getValue(int index) {
      return this.values[this.cellRowIndices[index]][this.cellColumnIndices[index]];
   }

   @Override
   ImmutableTable.SerializedForm createSerializedForm() {
      return ImmutableTable.SerializedForm.create(this, this.cellRowIndices, this.cellColumnIndices);
   }

   private final class Column extends DenseImmutableTable.ImmutableArrayMap<R, V> {
      private final int columnIndex;

      Column(int columnIndex) {
         super(DenseImmutableTable.this.columnCounts[columnIndex]);
         this.columnIndex = columnIndex;
      }

      @Override
      ImmutableMap<R, Integer> keyToIndex() {
         return DenseImmutableTable.this.rowKeyToIndex;
      }

      @Override
      V getValue(int keyIndex) {
         return DenseImmutableTable.this.values[keyIndex][this.columnIndex];
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }

   private final class ColumnMap extends DenseImmutableTable.ImmutableArrayMap<C, Map<R, V>> {
      private ColumnMap() {
         super(DenseImmutableTable.this.columnCounts.length);
      }

      @Override
      ImmutableMap<C, Integer> keyToIndex() {
         return DenseImmutableTable.this.columnKeyToIndex;
      }

      Map<R, V> getValue(int keyIndex) {
         return DenseImmutableTable.this.new Column(keyIndex);
      }

      @Override
      boolean isPartialView() {
         return false;
      }
   }

   private abstract static class ImmutableArrayMap<K, V> extends ImmutableMap.IteratorBasedImmutableMap<K, V> {
      private final int size;

      ImmutableArrayMap(int size) {
         this.size = size;
      }

      abstract ImmutableMap<K, Integer> keyToIndex();

      private boolean isFull() {
         return this.size == this.keyToIndex().size();
      }

      K getKey(int index) {
         return this.keyToIndex().keySet().asList().get(index);
      }

      @Nullable
      abstract V getValue(int var1);

      @Override
      ImmutableSet<K> createKeySet() {
         return this.isFull() ? this.keyToIndex().keySet() : super.createKeySet();
      }

      @Override
      public int size() {
         return this.size;
      }

      @Override
      public V get(@Nullable Object key) {
         Integer keyIndex = this.keyToIndex().get(key);
         return keyIndex == null ? null : this.getValue(keyIndex);
      }

      @Override
      UnmodifiableIterator<Entry<K, V>> entryIterator() {
         return new AbstractIterator<Entry<K, V>>() {
            private int index = -1;
            private final int maxIndex = ImmutableArrayMap.this.keyToIndex().size();

            protected Entry<K, V> computeNext() {
               this.index++;

               while (this.index < this.maxIndex) {
                  V value = (V)ImmutableArrayMap.this.getValue(this.index);
                  if (value != null) {
                     return Maps.immutableEntry((K)ImmutableArrayMap.this.getKey(this.index), value);
                  }

                  this.index++;
               }

               return this.endOfData();
            }
         };
      }
   }

   private final class Row extends DenseImmutableTable.ImmutableArrayMap<C, V> {
      private final int rowIndex;

      Row(int rowIndex) {
         super(DenseImmutableTable.this.rowCounts[rowIndex]);
         this.rowIndex = rowIndex;
      }

      @Override
      ImmutableMap<C, Integer> keyToIndex() {
         return DenseImmutableTable.this.columnKeyToIndex;
      }

      @Override
      V getValue(int keyIndex) {
         return DenseImmutableTable.this.values[this.rowIndex][keyIndex];
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }

   private final class RowMap extends DenseImmutableTable.ImmutableArrayMap<R, Map<C, V>> {
      private RowMap() {
         super(DenseImmutableTable.this.rowCounts.length);
      }

      @Override
      ImmutableMap<R, Integer> keyToIndex() {
         return DenseImmutableTable.this.rowKeyToIndex;
      }

      Map<C, V> getValue(int keyIndex) {
         return DenseImmutableTable.this.new Row(keyIndex);
      }

      @Override
      boolean isPartialView() {
         return false;
      }
   }
}
