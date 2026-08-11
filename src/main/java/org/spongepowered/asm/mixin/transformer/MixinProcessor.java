package org.spongepowered.asm.mixin.transformer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelectorDynamic;
import org.spongepowered.asm.mixin.throwables.ClassAlreadyLoadedException;
import org.spongepowered.asm.mixin.throwables.MixinApplyError;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.mixin.throwables.MixinPrepareError;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.mixin.transformer.throwables.ReEntrantTransformerError;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.ReEntranceLock;
import org.spongepowered.asm.util.perf.Profiler;

class MixinProcessor {
   static final ILogger logger = MixinService.getService().getLogger("mixin");
   private final IMixinService service = MixinService.getService();
   private final List<MixinConfig> configs = new ArrayList<>();
   private final List<MixinConfig> pendingConfigs = new ArrayList<>();
   private final ReEntranceLock lock;
   private final String sessionId = UUID.randomUUID().toString();
   private final Extensions extensions;
   private final IHotSwap hotSwapper;
   private final MixinCoprocessors coprocessors = new MixinCoprocessors();
   private final Profiler profiler;
   private final IMixinAuditTrail auditTrail;
   private MixinEnvironment currentEnvironment;
   private Level verboseLoggingLevel = Level.DEBUG;
   private boolean errorState = false;
   private int transformedCount = 0;

   MixinProcessor(MixinEnvironment environment, Extensions extensions, IHotSwap hotSwapper, MixinCoprocessorNestHost nestHostCoprocessor) {
      this.lock = this.service.getReEntranceLock();
      this.extensions = extensions;
      this.hotSwapper = hotSwapper;
      this.coprocessors.add(new MixinCoprocessorPassthrough());
      this.coprocessors.add(new MixinCoprocessorAccessor(this.sessionId));
      this.coprocessors.add(nestHostCoprocessor);
      this.profiler = Profiler.getProfiler("mixin");
      this.auditTrail = this.service.getAuditTrail();
   }

   public void audit(MixinEnvironment environment) {
      Set<String> unhandled = new HashSet<>();

      for (MixinConfig config : this.configs) {
         unhandled.addAll(config.getUnhandledTargets());
      }

      ILogger auditLogger = MixinService.getService().getLogger("mixin.audit");

      for (String target : unhandled) {
         try {
            auditLogger.info("Force-loading class {}", target);
            this.service.getClassProvider().findClass(target, false);
         } catch (ClassNotFoundException var9) {
            auditLogger.error("Could not force-load " + target, var9);
         }
      }

      for (MixinConfig config : this.configs) {
         for (String target : config.getUnhandledTargets()) {
            ClassAlreadyLoadedException ex = new ClassAlreadyLoadedException(target + " was already classloaded");
            auditLogger.error("Could not force-load " + target, ex);
         }
      }

      if (environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER)) {
         Profiler.printAuditSummary();
      }
   }

   synchronized boolean applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode) {
      if (name != null && !this.errorState) {
         Profiler.Section mixinTimer = this.profiler.begin("mixin");

         boolean locked;
         try {
            locked = this.lockAndSelect(environment, name);
         } catch (Throwable var19) {
            mixinTimer.end();
            throw var19;
         }

         boolean transformed = false;

         try {
            MixinCoprocessor.ProcessResult result = this.coprocessors.process(name, targetClassNode);
            transformed |= result.isTransformed();
            if (result.isPassthrough()) {
               for (MixinCoprocessor coprocessor : this.coprocessors) {
                  transformed |= coprocessor.postProcess(name, targetClassNode);
               }

               if (this.auditTrail != null) {
                  this.auditTrail.onPostProcess(name);
               }

               this.extensions.export(environment, name, false, targetClassNode);
               return transformed;
            }

            MixinConfig packageOwnedByConfig = null;

            for (MixinConfig config : this.configs) {
               if (config.packageMatch(name)) {
                  int packageLen = packageOwnedByConfig != null ? packageOwnedByConfig.getMixinPackage().length() : 0;
                  if (config.getMixinPackage().length() > packageLen) {
                     packageOwnedByConfig = config;
                  }
               }
            }

            if (packageOwnedByConfig != null) {
               ClassInfo targetInfo = ClassInfo.fromClassNode(targetClassNode);
               if (!targetInfo.hasSuperClass(InjectionPoint.class) && !targetInfo.hasSuperClass(ITargetSelectorDynamic.class)) {
                  throw new IllegalClassLoadError(this.getInvalidClassError(name, targetClassNode, packageOwnedByConfig));
               }

               return transformed;
            }

            SortedSet<MixinInfo> mixins = null;

            for (MixinConfig configx : this.configs) {
               if (configx.hasMixinsFor(name)) {
                  if (mixins == null) {
                     mixins = new TreeSet<>();
                  }

                  mixins.addAll(configx.getMixinsFor(name));
               }
            }

            if (mixins != null) {
               if (locked) {
                  ReEntrantTransformerError error = new ReEntrantTransformerError("Re-entrance error.");
                  logger.warn("Re-entrance detected, this will cause serious problems.", error);
                  throw error;
               }

               if (this.hotSwapper != null) {
                  this.hotSwapper.registerTargetClass(name, targetClassNode);
               }

               try {
                  TargetClassContext context = new TargetClassContext(environment, this.extensions, this.sessionId, name, targetClassNode, mixins);
                  context.applyMixins();
                  transformed |= this.coprocessors.postProcess(name, targetClassNode);
                  if (context.isExported()) {
                     this.extensions.export(environment, context.getClassName(), context.isExportForced(), context.getClassNode());
                  }

                  for (InvalidMixinException suppressed : context.getSuppressedExceptions()) {
                     this.handleMixinApplyError(context.getClassName(), suppressed, environment);
                  }

                  this.transformedCount++;
                  transformed = true;
               } catch (InvalidMixinException var20) {
                  this.dumpClassOnFailure(name, targetClassNode, environment);
                  this.handleMixinApplyError(name, var20, environment);
               }
            } else if (this.coprocessors.postProcess(name, targetClassNode)) {
               transformed = true;
               this.extensions.export(environment, name, false, targetClassNode);
            }
         } catch (MixinTransformerError var21) {
            throw var21;
         } catch (Throwable var22) {
            this.dumpClassOnFailure(name, targetClassNode, environment);
            throw new MixinTransformerError("An unexpected critical error was encountered", var22);
         } finally {
            this.lock.pop();
            mixinTimer.end();
         }

         return transformed;
      } else {
         return false;
      }
   }

   synchronized boolean couldTransformClass(MixinEnvironment environment, String name) {
      if (environment != MixinEnvironment.getCurrentEnvironment()) {
         throw new MixinException("Current environment must match the supplied environment");
      } else if (name != null && !this.errorState) {
         this.lockAndSelect(environment, name);

         boolean var13;
         try {
            if (this.coprocessors.processingCouldTransform(name)) {
               return true;
            }

            for (MixinConfig config : this.configs) {
               if (config.packageMatch(name)) {
                  return true;
               }
            }

            Iterator var9 = this.configs.iterator();

            MixinConfig configx;
            do {
               if (!var9.hasNext()) {
                  return false;
               }

               configx = (MixinConfig)var9.next();
            } while (!configx.hasMixinsFor(name));

            var13 = true;
         } finally {
            this.lock.pop();
         }

         return var13;
      } else {
         return false;
      }
   }

   private boolean lockAndSelect(MixinEnvironment environment, String name) {
      boolean locked = this.lock.push().check();
      if (locked) {
         for (MixinConfig config : this.pendingConfigs) {
            if (config.hasPendingMixinsFor(name)) {
               ReEntrantTransformerError error = new ReEntrantTransformerError("Re-entrance error.");
               logger.warn("Re-entrance detected during prepare phase, this will cause serious problems.", error);
               throw error;
            }
         }
      } else {
         try {
            this.checkSelect(environment);
         } catch (Exception var7) {
            this.lock.pop();
            throw new MixinException(var7);
         }
      }

      return locked;
   }

   private String getInvalidClassError(String name, ClassNode targetClassNode, MixinConfig ownedByConfig) {
      if (ownedByConfig.getClasses().contains(name)) {
         return String.format("Illegal classload request for %s. Mixin is defined in %s and cannot be referenced directly", name, ownedByConfig);
      } else {
         AnnotationNode mixin = Annotations.getInvisible(targetClassNode, Mixin.class);
         if (mixin != null) {
            MixinInfo.Variant variant = MixinInfo.getVariant(targetClassNode);
            if (variant == MixinInfo.Variant.ACCESSOR) {
               return String.format(
                  "Illegal classload request for accessor mixin %s. The mixin is missing from %s which owns package %s* and the mixin has not been applied.",
                  name,
                  ownedByConfig,
                  ownedByConfig.getMixinPackage()
               );
            }
         }

         return String.format(
            "%s is in a defined mixin package %s* owned by %s and cannot be referenced directly", name, ownedByConfig.getMixinPackage(), ownedByConfig
         );
      }
   }

   public List<String> reload(String mixinClass, ClassNode classNode) {
      if (this.lock.getDepth() > 0) {
         throw new MixinApplyError("Cannot reload mixin if re-entrant lock entered");
      } else {
         List<String> targets = new ArrayList<>();

         for (MixinConfig config : this.configs) {
            targets.addAll(config.reloadMixin(mixinClass, classNode));
         }

         return targets;
      }
   }

   private void checkSelect(MixinEnvironment environment) {
      if (this.currentEnvironment != environment) {
         this.select(environment);
      } else {
         int unvisitedCount = Mixins.getUnvisitedCount();
         if (unvisitedCount > 0 && this.transformedCount == 0) {
            this.select(environment);
         }
      }
   }

   private void select(MixinEnvironment environment) {
      this.verboseLoggingLevel = environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE) ? Level.INFO : Level.DEBUG;
      if (this.transformedCount > 0) {
         logger.log(this.verboseLoggingLevel, "Ending {}, applied {} mixins", this.currentEnvironment, this.transformedCount);
      }

      String action = this.currentEnvironment == environment ? "Checking for additional" : "Preparing";
      logger.log(this.verboseLoggingLevel, "{} mixins for {}", action, environment);
      Profiler.setActive(true);
      this.profiler.mark(environment.getPhase().toString() + ":prepare");
      Profiler.Section prepareTimer = this.profiler.begin("prepare");
      this.selectConfigs(environment);
      this.extensions.select(environment);
      int totalMixins = this.prepareConfigs(environment, this.extensions);
      this.currentEnvironment = environment;
      this.transformedCount = 0;
      prepareTimer.end();
      long elapsedMs = prepareTimer.getTime();
      double elapsedTime = prepareTimer.getSeconds();
      if (elapsedTime > 0.25) {
         long loadTime = this.profiler.get("class.load").getTime();
         long transformTime = this.profiler.get("class.transform").getTime();
         long pluginTime = this.profiler.get("mixin.plugin").getTime();
         String elapsed = new DecimalFormat("###0.000").format(elapsedTime);
         String perMixinTime = new DecimalFormat("###0.0").format((double)elapsedMs / totalMixins);
         logger.log(
            this.verboseLoggingLevel,
            "Prepared {} mixins in {} sec ({}ms avg) ({}ms load, {}ms transform, {}ms plugin)",
            totalMixins,
            elapsed,
            perMixinTime,
            loadTime,
            transformTime,
            pluginTime
         );
      }

      this.profiler.mark(environment.getPhase().toString() + ":apply");
      Profiler.setActive(environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER));
   }

   private void selectConfigs(MixinEnvironment environment) {
      Iterator<Config> iter = Mixins.getConfigs().iterator();

      while (iter.hasNext()) {
         Config handle = iter.next();

         try {
            MixinConfig config = handle.get();
            if (config.select(environment)) {
               iter.remove();
               logger.log(this.verboseLoggingLevel, "Selecting config {}", config);
               config.onSelect();
               this.pendingConfigs.add(config);
            }
         } catch (Exception var5) {
            logger.warn(String.format("Failed to select mixin config: %s", handle), var5);
         }
      }

      Collections.sort(this.pendingConfigs);
   }

   private int prepareConfigs(MixinEnvironment environment, Extensions extensions) {
      int totalMixins = 0;
      final IHotSwap hotSwapper = this.hotSwapper;

      for (MixinConfig config : this.pendingConfigs) {
         for (MixinCoprocessor coprocessor : this.coprocessors) {
            config.addListener(coprocessor);
         }

         config.addListener(MixinInheritanceTracker.INSTANCE);
         if (hotSwapper != null) {
            config.addListener(new MixinConfig.IListener() {
               @Override
               public void onPrepare(MixinInfo mixin) {
                  hotSwapper.registerMixinClass(mixin.getClassName());
               }

               @Override
               public void onInit(MixinInfo mixin) {
               }
            });
         }
      }

      for (MixinConfig config : this.pendingConfigs) {
         try {
            logger.log(this.verboseLoggingLevel, "Preparing {} ({})", config, config.getDeclaredMixinCount());
            config.prepare(extensions);
            totalMixins += config.getMixinCount();
         } catch (InvalidMixinException var13) {
            this.handleMixinPrepareError(config, var13, environment);
         } catch (Exception var14) {
            String message = var14.getMessage();
            logger.error(
               "Error encountered whilst initialising mixin config '" + config.getName() + "' from mod '" + FabricUtil.getModId(config) + "': " + message,
               var14
            );
         }
      }

      for (MixinConfig config : this.pendingConfigs) {
         IMixinConfigPlugin plugin = config.getPlugin();
         if (plugin != null) {
            Set<String> otherTargets = new HashSet<>();

            for (MixinConfig otherConfig : this.pendingConfigs) {
               if (!otherConfig.equals(config)) {
                  otherTargets.addAll(otherConfig.getTargets());
               }
            }

            plugin.acceptTargets(config.getTargetsSet(), Collections.unmodifiableSet(otherTargets));
         }
      }

      for (MixinConfig configx : this.pendingConfigs) {
         try {
            configx.postInitialise(this.extensions);
         } catch (InvalidMixinException var11) {
            this.handleMixinPrepareError(configx, var11, environment);
         } catch (Exception var12) {
            String message = var12.getMessage();
            logger.error(
               "Error encountered during mixin config postInit step '" + configx.getName() + "' from mod '" + FabricUtil.getModId(configx) + "': " + message,
               var12
            );
         }
      }

      this.configs.addAll(this.pendingConfigs);
      Collections.sort(this.configs);
      this.pendingConfigs.clear();
      return totalMixins;
   }

   private void handleMixinPrepareError(MixinConfig config, InvalidMixinException ex, MixinEnvironment environment) throws MixinPrepareError {
      this.handleMixinError(config.getName(), ex, environment, MixinProcessor.ErrorPhase.PREPARE);
   }

   private void handleMixinApplyError(String targetClass, InvalidMixinException ex, MixinEnvironment environment) throws MixinApplyError {
      this.handleMixinError(targetClass, ex, environment, MixinProcessor.ErrorPhase.APPLY);
   }

   private void handleMixinError(String context, InvalidMixinException ex, MixinEnvironment environment, MixinProcessor.ErrorPhase errorPhase) throws Error {
      this.errorState = true;
      IMixinInfo mixin = ex.getMixin();
      if (mixin == null) {
         logger.error("InvalidMixinException has no mixin!", ex);
         throw ex;
      } else {
         IMixinConfig config = mixin.getConfig();
         MixinEnvironment.Phase phase = mixin.getPhase();
         IMixinErrorHandler.ErrorAction action = config.isRequired() ? IMixinErrorHandler.ErrorAction.ERROR : IMixinErrorHandler.ErrorAction.WARN;
         if (environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
            new PrettyPrinter()
               .wrapTo(160)
               .add("Invalid Mixin")
               .centre()
               .hr('-')
               .kvWidth(10)
               .kv("Action", errorPhase.name())
               .kv("Mixin", mixin.getClassName())
               .kv("Config", config.getName())
               .kv("ModId", FabricUtil.getModId(config))
               .kv("Phase", phase)
               .hr('-')
               .add("    %s", ex.getClass().getName())
               .hr('-')
               .addWrapped("    %s", ex.getMessage())
               .hr('-')
               .add(ex, 8)
               .log(action.logLevel);
         }

         for (IMixinErrorHandler handler : this.getErrorHandlers(mixin.getPhase())) {
            IMixinErrorHandler.ErrorAction newAction = errorPhase.onError(handler, context, ex, mixin, action);
            if (newAction != null) {
               action = newAction;
            }
         }

         logger.log(action.logLevel, errorPhase.getLogMessage(context, ex, mixin), ex);
         this.errorState = false;
         if (action == IMixinErrorHandler.ErrorAction.ERROR) {
            throw new MixinApplyError(errorPhase.getErrorMessage(mixin, config, phase), ex);
         }
      }
   }

   private List<IMixinErrorHandler> getErrorHandlers(MixinEnvironment.Phase phase) {
      List<IMixinErrorHandler> handlers = new ArrayList<>();

      for (String handlerClassName : Mixins.getErrorHandlerClasses()) {
         try {
            logger.info("Instancing error handler class {}", handlerClassName);
            Class<?> handlerClass = this.service.getClassProvider().findClass(handlerClassName, true);
            IMixinErrorHandler handler = (IMixinErrorHandler)handlerClass.getDeclaredConstructor().newInstance();
            if (handler != null) {
               handlers.add(handler);
            }
         } catch (Throwable var7) {
         }
      }

      return handlers;
   }

   private void dumpClassOnFailure(String className, ClassNode classNode, MixinEnvironment env) {
      if (env.getOption(MixinEnvironment.Option.DUMP_TARGET_ON_FAILURE)) {
         ExtensionClassExporter exporter = this.extensions.getExtension(ExtensionClassExporter.class);
         exporter.dumpClass(className.replace('.', '/') + ".target", classNode);
      }
   }

   static enum ErrorPhase {
      PREPARE {
         @Override
         IMixinErrorHandler.ErrorAction onError(
            IMixinErrorHandler handler, String context, InvalidMixinException ex, IMixinInfo mixin, IMixinErrorHandler.ErrorAction action
         ) {
            try {
               return handler.onPrepareError(mixin.getConfig(), ex, mixin, action);
            } catch (AbstractMethodError var7) {
               return action;
            }
         }

         @Override
         protected String getContext(IMixinInfo mixin, String context) {
            return String.format("preparing %s in %s", mixin.getName(), context);
         }
      },
      APPLY {
         @Override
         IMixinErrorHandler.ErrorAction onError(
            IMixinErrorHandler handler, String context, InvalidMixinException ex, IMixinInfo mixin, IMixinErrorHandler.ErrorAction action
         ) {
            try {
               return handler.onApplyError(context, ex, mixin, action);
            } catch (AbstractMethodError var7) {
               return action;
            }
         }

         @Override
         protected String getContext(IMixinInfo mixin, String context) {
            return String.format("%s -> %s", mixin, context);
         }
      };

      private final String text = this.name().toLowerCase(Locale.ROOT);

      private ErrorPhase() {
      }

      abstract IMixinErrorHandler.ErrorAction onError(
         IMixinErrorHandler var1, String var2, InvalidMixinException var3, IMixinInfo var4, IMixinErrorHandler.ErrorAction var5
      );

      protected abstract String getContext(IMixinInfo var1, String var2);

      public String getLogMessage(String context, InvalidMixinException ex, IMixinInfo mixin) {
         return String.format(
            "Mixin %s for mod %s failed %s: %s %s",
            this.text,
            FabricUtil.getModId(mixin.getConfig()),
            this.getContext(mixin, context),
            ex.getClass().getName(),
            ex.getMessage()
         );
      }

      public String getErrorMessage(IMixinInfo mixin, IMixinConfig config, MixinEnvironment.Phase phase) {
         return String.format("Mixin [%s] from phase [%s] in config [%s] FAILED during %s", mixin, phase, config, this.name());
      }
   }
}
