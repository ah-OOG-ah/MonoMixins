package org.spongepowered.libraries.com.google.common.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;
import org.spongepowered.libraries.com.google.common.base.Preconditions;

@Beta
@GwtIncompatible
public final class PatternFilenameFilter implements FilenameFilter {
   private final Pattern pattern;

   public PatternFilenameFilter(String patternStr) {
      this(Pattern.compile(patternStr));
   }

   public PatternFilenameFilter(Pattern pattern) {
      this.pattern = Preconditions.checkNotNull(pattern);
   }

   @Override
   public boolean accept(@Nullable File dir, String fileName) {
      return this.pattern.matcher(fileName).matches();
   }
}
