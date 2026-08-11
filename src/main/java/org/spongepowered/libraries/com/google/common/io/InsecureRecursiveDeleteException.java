package org.spongepowered.libraries.com.google.common.io;

import java.nio.file.FileSystemException;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.GwtIncompatible;

@Beta
@AndroidIncompatible
@GwtIncompatible
public final class InsecureRecursiveDeleteException extends FileSystemException {
   public InsecureRecursiveDeleteException(@Nullable String file) {
      super(file, null, "unable to guarantee security of recursive delete");
   }
}
