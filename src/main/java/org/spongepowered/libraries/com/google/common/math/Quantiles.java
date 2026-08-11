package org.spongepowered.libraries.com.google.common.math;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.primitives.Doubles;
import org.spongepowered.libraries.com.google.common.primitives.Ints;

@Beta
@GwtIncompatible
public final class Quantiles {
   public static Quantiles.ScaleAndIndex median() {
      return scale(2).index(1);
   }

   public static Quantiles.Scale quartiles() {
      return scale(4);
   }

   public static Quantiles.Scale percentiles() {
      return scale(100);
   }

   public static Quantiles.Scale scale(int scale) {
      return new Quantiles.Scale(scale);
   }

   private static boolean containsNaN(double... dataset) {
      for (double value : dataset) {
         if (Double.isNaN(value)) {
            return true;
         }
      }

      return false;
   }

   private static double interpolate(double lower, double upper, double remainder, double scale) {
      if (lower == Double.NEGATIVE_INFINITY) {
         return upper == Double.POSITIVE_INFINITY ? Double.NaN : Double.NEGATIVE_INFINITY;
      } else {
         return upper == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : lower + (upper - lower) * remainder / scale;
      }
   }

   private static void checkIndex(int index, int scale) {
      if (index < 0 || index > scale) {
         throw new IllegalArgumentException("Quantile indexes must be between 0 and the scale, which is " + scale);
      }
   }

   private static double[] longsToDoubles(long[] longs) {
      int len = longs.length;
      double[] doubles = new double[len];

      for (int i = 0; i < len; i++) {
         doubles[i] = longs[i];
      }

      return doubles;
   }

   private static double[] intsToDoubles(int[] ints) {
      int len = ints.length;
      double[] doubles = new double[len];

      for (int i = 0; i < len; i++) {
         doubles[i] = ints[i];
      }

      return doubles;
   }

   private static void selectInPlace(int required, double[] array, int from, int to) {
      if (required == from) {
         int min = from;

         for (int index = from + 1; index <= to; index++) {
            if (array[min] > array[index]) {
               min = index;
            }
         }

         if (min != from) {
            swap(array, min, from);
         }
      } else {
         while (to > from) {
            int partitionPoint = partition(array, from, to);
            if (partitionPoint >= required) {
               to = partitionPoint - 1;
            }

            if (partitionPoint <= required) {
               from = partitionPoint + 1;
            }
         }
      }
   }

   private static int partition(double[] array, int from, int to) {
      movePivotToStartOfSlice(array, from, to);
      double pivot = array[from];
      int partitionPoint = to;

      for (int i = to; i > from; i--) {
         if (array[i] > pivot) {
            swap(array, partitionPoint, i);
            partitionPoint--;
         }
      }

      swap(array, from, partitionPoint);
      return partitionPoint;
   }

   private static void movePivotToStartOfSlice(double[] array, int from, int to) {
      int mid = from + to >>> 1;
      boolean toLessThanMid = array[to] < array[mid];
      boolean midLessThanFrom = array[mid] < array[from];
      boolean toLessThanFrom = array[to] < array[from];
      if (toLessThanMid == midLessThanFrom) {
         swap(array, mid, from);
      } else if (toLessThanMid != toLessThanFrom) {
         swap(array, from, to);
      }
   }

   private static void selectAllInPlace(int[] allRequired, int requiredFrom, int requiredTo, double[] array, int from, int to) {
      int requiredChosen = chooseNextSelection(allRequired, requiredFrom, requiredTo, from, to);
      int required = allRequired[requiredChosen];
      selectInPlace(required, array, from, to);
      int requiredBelow = requiredChosen - 1;

      while (requiredBelow >= requiredFrom && allRequired[requiredBelow] == required) {
         requiredBelow--;
      }

      if (requiredBelow >= requiredFrom) {
         selectAllInPlace(allRequired, requiredFrom, requiredBelow, array, from, required - 1);
      }

      int requiredAbove = requiredChosen + 1;

      while (requiredAbove <= requiredTo && allRequired[requiredAbove] == required) {
         requiredAbove++;
      }

      if (requiredAbove <= requiredTo) {
         selectAllInPlace(allRequired, requiredAbove, requiredTo, array, required + 1, to);
      }
   }

   private static int chooseNextSelection(int[] allRequired, int requiredFrom, int requiredTo, int from, int to) {
      if (requiredFrom == requiredTo) {
         return requiredFrom;
      } else {
         int centerFloor = from + to >>> 1;
         int low = requiredFrom;
         int high = requiredTo;

         while (high > low + 1) {
            int mid = low + high >>> 1;
            if (allRequired[mid] > centerFloor) {
               high = mid;
            } else {
               if (allRequired[mid] >= centerFloor) {
                  return mid;
               }

               low = mid;
            }
         }

         return from + to - allRequired[low] - allRequired[high] > 0 ? high : low;
      }
   }

   private static void swap(double[] array, int i, int j) {
      double temp = array[i];
      array[i] = array[j];
      array[j] = temp;
   }

   public static final class Scale {
      private final int scale;

      private Scale(int scale) {
         Preconditions.checkArgument(scale > 0, "Quantile scale must be positive");
         this.scale = scale;
      }

      public Quantiles.ScaleAndIndex index(int index) {
         return new Quantiles.ScaleAndIndex(this.scale, index);
      }

      public Quantiles.ScaleAndIndexes indexes(int... indexes) {
         return new Quantiles.ScaleAndIndexes(this.scale, (int[])indexes.clone());
      }

      public Quantiles.ScaleAndIndexes indexes(Collection<Integer> indexes) {
         return new Quantiles.ScaleAndIndexes(this.scale, Ints.toArray(indexes));
      }
   }

   public static final class ScaleAndIndex {
      private final int scale;
      private final int index;

      private ScaleAndIndex(int scale, int index) {
         Quantiles.checkIndex(index, scale);
         this.scale = scale;
         this.index = index;
      }

      public double compute(Collection<? extends Number> dataset) {
         return this.computeInPlace(Doubles.toArray(dataset));
      }

      public double compute(double... dataset) {
         return this.computeInPlace((double[])dataset.clone());
      }

      public double compute(long... dataset) {
         return this.computeInPlace(Quantiles.longsToDoubles(dataset));
      }

      public double compute(int... dataset) {
         return this.computeInPlace(Quantiles.intsToDoubles(dataset));
      }

      public double computeInPlace(double... dataset) {
         Preconditions.checkArgument(dataset.length > 0, "Cannot calculate quantiles of an empty dataset");
         if (Quantiles.containsNaN(dataset)) {
            return Double.NaN;
         } else {
            long numerator = (long)this.index * (dataset.length - 1);
            int quotient = (int)LongMath.divide(numerator, this.scale, RoundingMode.DOWN);
            int remainder = (int)(numerator - (long)quotient * this.scale);
            Quantiles.selectInPlace(quotient, dataset, 0, dataset.length - 1);
            if (remainder == 0) {
               return dataset[quotient];
            } else {
               Quantiles.selectInPlace(quotient + 1, dataset, quotient + 1, dataset.length - 1);
               return Quantiles.interpolate(dataset[quotient], dataset[quotient + 1], (double)remainder, (double)this.scale);
            }
         }
      }
   }

   public static final class ScaleAndIndexes {
      private final int scale;
      private final int[] indexes;

      private ScaleAndIndexes(int scale, int[] indexes) {
         for (int index : indexes) {
            Quantiles.checkIndex(index, scale);
         }

         this.scale = scale;
         this.indexes = indexes;
      }

      public Map<Integer, Double> compute(Collection<? extends Number> dataset) {
         return this.computeInPlace(Doubles.toArray(dataset));
      }

      public Map<Integer, Double> compute(double... dataset) {
         return this.computeInPlace((double[])dataset.clone());
      }

      public Map<Integer, Double> compute(long... dataset) {
         return this.computeInPlace(Quantiles.longsToDoubles(dataset));
      }

      public Map<Integer, Double> compute(int... dataset) {
         return this.computeInPlace(Quantiles.intsToDoubles(dataset));
      }

      public Map<Integer, Double> computeInPlace(double... dataset) {
         Preconditions.checkArgument(dataset.length > 0, "Cannot calculate quantiles of an empty dataset");
         if (!Quantiles.containsNaN(dataset)) {
            int[] quotients = new int[this.indexes.length];
            int[] remainders = new int[this.indexes.length];
            int[] requiredSelections = new int[this.indexes.length * 2];
            int requiredSelectionsCount = 0;

            for (int i = 0; i < this.indexes.length; i++) {
               long numerator = (long)this.indexes[i] * (dataset.length - 1);
               int quotient = (int)LongMath.divide(numerator, this.scale, RoundingMode.DOWN);
               int remainder = (int)(numerator - (long)quotient * this.scale);
               quotients[i] = quotient;
               remainders[i] = remainder;
               requiredSelections[requiredSelectionsCount] = quotient;
               requiredSelectionsCount++;
               if (remainder != 0) {
                  requiredSelections[requiredSelectionsCount] = quotient + 1;
                  requiredSelectionsCount++;
               }
            }

            Arrays.sort(requiredSelections, 0, requiredSelectionsCount);
            Quantiles.selectAllInPlace(requiredSelections, 0, requiredSelectionsCount - 1, dataset, 0, dataset.length - 1);
            Map<Integer, Double> ret = new HashMap<>();

            for (int ix = 0; ix < this.indexes.length; ix++) {
               int quotient = quotients[ix];
               int remainder = remainders[ix];
               if (remainder == 0) {
                  ret.put(this.indexes[ix], dataset[quotient]);
               } else {
                  ret.put(this.indexes[ix], Quantiles.interpolate(dataset[quotient], dataset[quotient + 1], (double)remainder, (double)this.scale));
               }
            }

            return Collections.unmodifiableMap(ret);
         } else {
            Map<Integer, Double> nanMap = new HashMap<>();

            for (int index : this.indexes) {
               nanMap.put(index, Double.NaN);
            }

            return Collections.unmodifiableMap(nanMap);
         }
      }
   }
}
