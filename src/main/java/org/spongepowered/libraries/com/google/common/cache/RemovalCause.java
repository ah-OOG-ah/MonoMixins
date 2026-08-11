package org.spongepowered.libraries.com.google.common.cache;

import org.spongepowered.libraries.com.google.common.annotations.GwtCompatible;

@GwtCompatible
public enum RemovalCause {
   EXPLICIT {
      @Override
      boolean wasEvicted() {
         return false;
      }
   },
   REPLACED {
      @Override
      boolean wasEvicted() {
         return false;
      }
   },
   COLLECTED {
      @Override
      boolean wasEvicted() {
         return true;
      }
   },
   EXPIRED {
      @Override
      boolean wasEvicted() {
         return true;
      }
   },
   SIZE {
      @Override
      boolean wasEvicted() {
         return true;
      }
   };

   private RemovalCause() {
   }

   abstract boolean wasEvicted();
}
