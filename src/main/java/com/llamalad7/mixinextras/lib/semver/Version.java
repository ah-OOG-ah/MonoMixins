package com.llamalad7.mixinextras.lib.semver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class Version implements Serializable, Comparable<Version> {
   public static final Comparator<Version> INCREMENT_ORDER = Version::compareToIgnoreBuildMetadata;
   public static final Comparator<Version> PRECEDENCE_ORDER = INCREMENT_ORDER.reversed();
   private final long major;
   private final long minor;
   private final long patch;
   private final String[] preReleaseIds;
   private final String[] buildIds;
   @Deprecated
   public static final Comparator<Version> BUILD_AWARE_ORDER = Version::compareTo;

   Version(long major, long minor, long patch, String[] preReleaseIds, String[] buildIds) {
      this.major = Version.Validators.nonNegative(major, "major");
      this.minor = Version.Validators.nonNegative(minor, "minor");
      this.patch = Version.Validators.nonNegative(patch, "patch");
      this.preReleaseIds = (String[])Version.Validators.nonNull(preReleaseIds, "preReleaseIds").clone();
      this.buildIds = (String[])Version.Validators.nonNull(buildIds, "buildIds").clone();
   }

   public static Version parse(String version) {
      return parse(version, true);
   }

   public static Version parse(String version, boolean strictly) {
      return VersionParser.parseValidSemVer(Version.Validators.nonNull(version, "version"), strictly);
   }

   public static Optional<Version> tryParse(String version) {
      return tryParse(version, true);
   }

   public static Optional<Version> tryParse(String version, boolean strictly) {
      try {
         return Optional.of(parse(version, strictly));
      } catch (RuntimeException var3) {
         return Optional.empty();
      }
   }

   public Optional<String> preReleaseVersion() {
      return Optional.ofNullable(joinIdentifiers(this.preReleaseIds));
   }

   public Optional<String> buildMetadata() {
      return Optional.ofNullable(joinIdentifiers(this.buildIds));
   }

   public boolean isHigherThan(Version other) {
      return this.compareToIgnoreBuildMetadata(other) > 0;
   }

   public int compareTo(Version other) {
      int result = this.compareToIgnoreBuildMetadata(other);
      if (result != 0) {
         return result;
      } else {
         result = compareIdentifierArrays(this.buildIds, other.buildIds);
         if (this.buildIds.length == 0 || other.buildIds.length == 0) {
            result = -1 * result;
         }

         return result;
      }
   }

   public int compareToIgnoreBuildMetadata(Version other) {
      Version.Validators.nonNull(other, "other");
      long result = this.major - other.major;
      if (result == 0L) {
         result = this.minor - other.minor;
         if (result == 0L) {
            result = this.patch - other.patch;
            if (result == 0L) {
               return compareIdentifierArrays(this.preReleaseIds, other.preReleaseIds);
            }
         }
      }

      return result < 0L ? -1 : 1;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof Version) ? false : this.compareTo((Version)other) == 0;
      }
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 97 * hash + Long.hashCode(this.major);
      hash = 97 * hash + Long.hashCode(this.minor);
      hash = 97 * hash + Long.hashCode(this.patch);
      hash = 97 * hash + Arrays.hashCode((Object[])this.preReleaseIds);
      return 97 * hash + Arrays.hashCode((Object[])this.buildIds);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.major);
      sb.append(".");
      sb.append(this.minor);
      sb.append(".");
      sb.append(this.patch);
      this.preReleaseVersion().ifPresent(r -> sb.append("-").append(r));
      this.buildMetadata().ifPresent(b -> sb.append("+").append(b));
      return sb.toString();
   }

   private static String joinIdentifiers(String... ids) {
      return ids.length == 0 ? null : String.join(".", ids);
   }

   private static int compareIdentifierArrays(String[] thisIds, String[] otherIds) {
      if (thisIds.length == 0 && otherIds.length == 0) {
         return 0;
      } else if (thisIds.length != 0 && otherIds.length != 0) {
         int result = 0;
         int minLength = Math.min(thisIds.length, otherIds.length);

         for (int i = 0; i < minLength; i++) {
            result = compareIdentifiers(thisIds[i], otherIds[i]);
            if (result != 0) {
               break;
            }
         }

         if (result == 0) {
            result = thisIds.length - otherIds.length;
         }

         return result;
      } else {
         return thisIds.length == 0 ? 1 : -1;
      }
   }

   private static int compareIdentifiers(String thisId, String otherId) {
      return isNumeric(thisId) && isNumeric(otherId) ? Long.valueOf(thisId).compareTo(Long.valueOf(otherId)) : thisId.compareTo(otherId);
   }

   private static boolean isNumeric(String id) {
      return id.startsWith("0") ? false : id.chars().allMatch(Character::isDigit);
   }

   static class Validators {
      static long nonNegative(long arg, String name) {
         if (arg < 0L) {
            throw new IllegalArgumentException(name + " must not be negative");
         } else {
            return arg;
         }
      }

      static <T> T nonNull(T arg, String name) {
         return nonNullOrThrow(arg, name + " must not be null");
      }

      private static <T> T nonNullOrThrow(T arg, String msg) {
         if (arg == null) {
            throw new IllegalArgumentException(msg);
         } else {
            return arg;
         }
      }
   }
}
