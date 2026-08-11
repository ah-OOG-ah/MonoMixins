package org.spongepowered.asm.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterConsole;
import org.spongepowered.include.com.google.common.base.Joiner;
import org.spongepowered.include.com.google.common.collect.ObjectArrays;

public final class MixinService {
   private static MixinService.LogBuffer logBuffer = new MixinService.LogBuffer();
   private static MixinService instance;
   private ServiceLoader<IMixinServiceBootstrap> bootstrapServiceLoader;
   private final Set<String> bootedServices = new HashSet<>();
   private ServiceLoader<IMixinService> serviceLoader;
   private IMixinService service = null;
   private IGlobalPropertyService propertyService;

   private MixinService() {
      this.runBootServices();
   }

   private void runBootServices() {
      String serviceCls = System.getProperty("mixin.bootstrapService");
      if (serviceCls != null) {
         try {
            IMixinServiceBootstrap bootService = (IMixinServiceBootstrap)Class.forName(serviceCls).getConstructor().newInstance();
            bootService.bootstrap();
            this.bootedServices.add(bootService.getServiceClassName());
         } catch (ReflectiveOperationException var4) {
            throw new RuntimeException(var4);
         }
      } else {
         this.bootstrapServiceLoader = ServiceLoader.load(IMixinServiceBootstrap.class, this.getClass().getClassLoader());

         for (IMixinServiceBootstrap bootService : this.bootstrapServiceLoader) {
            try {
               bootService.bootstrap();
               this.bootedServices.add(bootService.getServiceClassName());
            } catch (ServiceInitialisationException var5) {
               logBuffer.debug("Mixin bootstrap service {} is not available: {}", var5.getStackTrace()[0].getClassName(), var5.getMessage());
            } catch (Throwable var6) {
               logBuffer.debug("Catching {}:{} initialising service", var6.getClass().getName(), var6.getMessage(), var6);
            }
         }
      }
   }

   private static MixinService getInstance() {
      if (instance == null) {
         instance = new MixinService();
      }

      return instance;
   }

   public static void boot() {
      getInstance();
   }

   public static IMixinService getService() {
      return getInstance().getServiceInstance();
   }

   private synchronized IMixinService getServiceInstance() {
      if (this.service == null) {
         try {
            this.service = this.initService();
            ILogger serviceLogger = this.service.getLogger("mixin");
            logBuffer.flush(serviceLogger);
         } catch (Error var3) {
            ILogger defaultLogger = getDefaultLogger();
            logBuffer.flush(defaultLogger);
            defaultLogger.error(var3.getMessage(), var3);
            throw var3;
         }
      }

      return this.service;
   }

   private IMixinService initService() {
      String serviceCls = System.getProperty("mixin.service");
      if (serviceCls != null) {
         try {
            IMixinService service = (IMixinService)Class.forName(serviceCls).getConstructor().newInstance();
            if (!service.isValid()) {
               throw new RuntimeException("invalid service " + serviceCls + " configured via system property");
            } else {
               return service;
            }
         } catch (ReflectiveOperationException var8) {
            throw new RuntimeException(var8);
         }
      } else {
         this.serviceLoader = ServiceLoader.load(IMixinService.class, this.getClass().getClassLoader());
         Iterator<IMixinService> iter = this.serviceLoader.iterator();
         List<String> badServices = new ArrayList<>();
         int brokenServiceCount = 0;

         while (iter.hasNext()) {
            try {
               IMixinService service = iter.next();
               if (this.bootedServices.contains(service.getClass().getName())) {
                  logBuffer.debug("MixinService [{}] was successfully booted in {}", service.getName(), this.getClass().getClassLoader());
               }

               if (service.isValid()) {
                  return service;
               }

               logBuffer.debug("MixinService [{}] is not valid", service.getName());
               badServices.add(String.format("INVALID[%s]", service.getName()));
            } catch (ServiceConfigurationError var9) {
               brokenServiceCount++;
            } catch (Throwable var10) {
               String faultingClassName = var10.getStackTrace()[0].getClassName();
               logBuffer.debug("MixinService [{}] failed initialisation: {}", faultingClassName, var10.getMessage());
               int pos = faultingClassName.lastIndexOf(46);
               badServices.add(String.format("ERROR[%s]", pos < 0 ? faultingClassName : faultingClassName.substring(pos + 1)));
            }
         }

         String brokenServiceNote = brokenServiceCount == 0 ? "" : " and " + brokenServiceCount + " other invalid services.";
         throw new ServiceNotAvailableError("No mixin host service is available. Services: " + Joiner.on(", ").join(badServices) + brokenServiceNote);
      }
   }

   public static IGlobalPropertyService getGlobalPropertyService() {
      return getInstance().getGlobalPropertyServiceInstance();
   }

   private IGlobalPropertyService getGlobalPropertyServiceInstance() {
      if (this.propertyService == null) {
         this.propertyService = this.initPropertyService();
      }

      return this.propertyService;
   }

   private IGlobalPropertyService initPropertyService() {
      for (IGlobalPropertyService service : ServiceLoader.load(IGlobalPropertyService.class, this.getClass().getClassLoader())) {
         try {
            return service;
         } catch (ServiceConfigurationError var4) {
         } catch (Throwable var5) {
         }
      }

      throw new ServiceNotAvailableError("No mixin global property service is available");
   }

   private static <T> T getDefaultLogger() {
      return (T)new LoggerAdapterConsole("mixin").setDebugStream(System.err);
   }

   static class LogBuffer {
      private final List<MixinService.LogBuffer.LogEntry> buffer = new ArrayList<>();
      private ILogger logger;

      synchronized void debug(String message, Object... params) {
         if (this.logger != null) {
            this.logger.debug(message, params);
         } else {
            this.buffer.add(new MixinService.LogBuffer.LogEntry(message, params, null));
         }
      }

      synchronized void flush(ILogger logger) {
         for (MixinService.LogBuffer.LogEntry buffered : this.buffer) {
            if (buffered.t != null) {
               logger.debug(buffered.message, ObjectArrays.concat(buffered.params, buffered.t));
            } else {
               logger.debug(buffered.message, buffered.params);
            }
         }

         this.buffer.clear();
         this.logger = logger;
      }

      public static class LogEntry {
         public String message;
         public Object[] params;
         public Throwable t;

         public LogEntry(String message, Object[] params, Throwable t) {
            this.message = message;
            this.params = params;
            this.t = t;
         }
      }
   }
}
