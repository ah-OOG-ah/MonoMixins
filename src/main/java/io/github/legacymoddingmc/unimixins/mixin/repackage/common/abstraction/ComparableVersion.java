package io.github.legacymoddingmc.unimixins.mixin.repackage.common.abstraction;

public class ComparableVersion {
   private final ComparableVersion.IComparableVersionImpl impl;

   public ComparableVersion(String version) {
      if (ComparableVersion.class.getResource("/cpw/mods/fml/common/versioning/ComparableVersion.class") != null) {
         this.impl = new ComparableVersion.ComparableVersionOld(version);
      } else {
         this.impl = new ComparableVersion.ComparableVersionNew(version);
      }
   }

   public int compareTo(ComparableVersion other) {
      return this.impl.compareTo(other.impl);
   }

   public static class ComparableVersionNew implements ComparableVersion.IComparableVersionImpl {
      private final net.minecraftforge.fml.common.versioning.ComparableVersion internal;

      public ComparableVersionNew(String version) {
         this.internal = new net.minecraftforge.fml.common.versioning.ComparableVersion(version);
      }

      @Override
      public int compareTo(ComparableVersion.IComparableVersionImpl other) {
         if (other instanceof ComparableVersion.ComparableVersionNew) {
            return this.internal.compareTo(((ComparableVersion.ComparableVersionNew)other).internal);
         } else {
            throw new IllegalArgumentException();
         }
      }
   }

   public static class ComparableVersionOld implements ComparableVersion.IComparableVersionImpl {
      private final cpw.mods.fml.common.versioning.ComparableVersion internal;

      public ComparableVersionOld(String version) {
         this.internal = new cpw.mods.fml.common.versioning.ComparableVersion(version);
      }

      @Override
      public int compareTo(ComparableVersion.IComparableVersionImpl other) {
         if (other instanceof ComparableVersion.ComparableVersionOld) {
            return this.internal.compareTo(((ComparableVersion.ComparableVersionOld)other).internal);
         } else {
            throw new IllegalArgumentException();
         }
      }
   }

   public interface IComparableVersionImpl {
      int compareTo(ComparableVersion.IComparableVersionImpl var1);
   }
}
