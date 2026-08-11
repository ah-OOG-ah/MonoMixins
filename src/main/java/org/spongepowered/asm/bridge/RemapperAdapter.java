package org.spongepowered.asm.bridge;

import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.ObfuscationUtil;

public abstract class RemapperAdapter implements IRemapper, ObfuscationUtil.IClassRemapper {
   protected final ILogger logger = MixinService.getService().getLogger("mixin");
   protected final Remapper remapper;
   protected boolean supportsNullArguments = true;

   public RemapperAdapter(Remapper remapper) {
      this.remapper = remapper;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }

   @Override
   public String mapMethodName(String owner, String name, String desc) {
      this.logger.debug("{} is remapping method {}{} for {}", this, name, desc, owner);
      if (this.supportsNullArguments || owner != null && name != null && desc != null) {
         String newName = this.remapper.mapMethodName(owner, name, desc);
         if (!newName.equals(name)) {
            return newName;
         } else {
            String obfOwner = this.unmap(owner);
            String obfDesc = this.unmapDesc(desc);
            this.logger.debug("{} is remapping obfuscated method {}{} for {}", this, name, obfDesc, obfOwner);
            return this.remapper.mapMethodName(obfOwner, name, obfDesc);
         }
      } else {
         return name;
      }
   }

   @Override
   public String mapFieldName(String owner, String name, String desc) {
      this.logger.debug("{} is remapping field {}{} for {}", this, name, desc, owner);
      if (this.supportsNullArguments || owner != null && name != null && desc != null) {
         String newName = this.remapper.mapFieldName(owner, name, desc);
         if (!newName.equals(name)) {
            return newName;
         } else {
            String obfOwner = this.unmap(owner);
            String obfDesc = this.unmapDesc(desc);
            this.logger.debug("{} is remapping obfuscated field {}{} for {}", this, name, obfDesc, obfOwner);
            return this.remapper.mapFieldName(obfOwner, name, obfDesc);
         }
      } else {
         return name;
      }
   }

   @Override
   public String map(String typeName) {
      this.logger.debug("{} is remapping class {}", this, typeName);
      return typeName == null ? typeName : this.remapper.map(typeName);
   }

   @Override
   public String unmap(String typeName) {
      return typeName == null ? typeName : typeName;
   }

   @Override
   public String mapDesc(String desc) {
      return desc == null ? desc : this.remapper.mapDesc(desc);
   }

   @Override
   public String unmapDesc(String desc) {
      if (desc == null) {
         return desc;
      } else {
         String newDesc = ObfuscationUtil.unmapDescriptor(desc, this);
         return newDesc != null ? newDesc : desc;
      }
   }
}
