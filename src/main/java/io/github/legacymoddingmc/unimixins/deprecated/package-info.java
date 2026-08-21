/// @deprecated Use [com.google] for Guava. See below for notes on this package.
@Deprecated
package io.github.legacymoddingmc.unimixins.deprecated;

/// MonoMixins aims to support anything UniMixins 0.3.1 supported at runtime, while updating the mod to be less...
/// *strange* at build time. This means emulating a number of libraries UniMixins used to shade.
///
/// However, MonoMixins does *not* guarantee build-time compat, and deliberately hides these deprecated libraries from
/// editors. Their classes are remapped into this package at runtime, or else placed here with altered file extensions
/// to prevent IDEs from decompiling them.
///
/// As of now, ASM is relocated to the upstream location, [org.objectweb.asm]. Guava is relocated here at runtime, to
/// [io.github.legacymoddingmc.unimixins.deprecated.com.google].
