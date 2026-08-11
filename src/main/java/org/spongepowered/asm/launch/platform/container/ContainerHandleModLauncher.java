package org.spongepowered.asm.launch.platform.container;

import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import org.spongepowered.asm.service.MixinService;

public class ContainerHandleModLauncher extends ContainerHandleVirtual {
   public ContainerHandleModLauncher(String name) {
      super(name);
   }

   public void addResource(String name, Path path) {
      this.add(new ContainerHandleModLauncher.Resource(name, path));
   }

   public void addResource(Entry<String, Path> entry) {
      this.add(new ContainerHandleModLauncher.Resource(entry.getKey(), entry.getValue()));
   }

   public void addResource(Object resource) {
      if (resource instanceof Entry) {
         this.addResource((Entry<String, Path>)resource);
      } else {
         MixinService.getService().getLogger("mixin").error("Unrecognised resource type {} passed to {}", resource.getClass(), this);
      }
   }

   public void addResources(List<?> resources) {
      for (Object resource : resources) {
         this.addResource(resource);
      }
   }

   @Override
   public String toString() {
      return String.format("ModLauncher Root Container(%s:%x)", this.getName(), this.hashCode());
   }

   class Resource extends ContainerHandleURI {
      private String name;
      private Path path;

      public Resource(String name, Path path) {
         super(path.toUri());
         this.name = name;
         this.path = path;
      }

      @Override
      public String getId() {
         String name = this.name;
         int lastDotPos = name.lastIndexOf(46);
         if (lastDotPos > 0) {
            name = name.substring(0, lastDotPos);
         }

         return name;
      }

      @Override
      public String getDescription() {
         return this.path.toAbsolutePath().toString();
      }

      @Override
      public String toString() {
         return String.format("ContainerHandleModLauncher.Resource(%s:%s)", this.name, this.path);
      }
   }
}
