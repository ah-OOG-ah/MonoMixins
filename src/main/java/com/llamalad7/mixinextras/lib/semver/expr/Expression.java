package com.llamalad7.mixinextras.lib.semver.expr;

import com.llamalad7.mixinextras.lib.semver.Version;
import java.util.function.Predicate;

public interface Expression extends Predicate<Version> {
   boolean interpret(Version var1);

   default boolean test(Version version) {
      return this.interpret(version);
   }
}
