package org.spongepowered.asm.service.mojang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.platform.MainAttributes;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.asm.transformers.MixinClassReader;
import org.spongepowered.asm.util.Files;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.include.com.google.common.collect.ImmutableList;
import org.spongepowered.include.com.google.common.collect.Sets;
import org.spongepowered.include.com.google.common.io.ByteStreams;
import org.spongepowered.include.com.google.common.io.Closeables;

public class MixinServiceLaunchWrapper extends MixinServiceAbstract implements IClassBytecodeProvider, IClassProvider, ITransformerProvider {
   public static final GlobalProperties.Keys BLACKBOARD_KEY_TWEAKCLASSES = GlobalProperties.Keys.of("TweakClasses");
   public static final GlobalProperties.Keys BLACKBOARD_KEY_TWEAKS = GlobalProperties.Keys.of("Tweaks");
   private static final Set<String> excludeTransformers = Sets.newHashSet(
      "net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer",
      "cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer",
      "net.minecraftforge.fml.common.asm.transformers.TerminalTransformer",
      "cpw.mods.fml.common.asm.transformers.TerminalTransformer"
   );
   private static final Logger logger = LogManager.getLogger();
   private LaunchClassLoaderUtil classLoaderUtil;
   private List<ILegacyClassTransformer> delegatedTransformers;
   private IClassNameTransformer nameTransformer;

   @Override
   public String getName() {
      return "LaunchWrapper";
   }

   @Override
   public boolean isValid() {
      try {
         LaunchClassLoader ex = Launch.classLoader;
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   @Override
   public void prepare() {
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.service.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.launch.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.logging.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.lib.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.lib.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.mixin.");
      Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.util.");
   }

   @Override
   public MixinEnvironment.Phase getInitialPhase() {
      String command = System.getProperty("sun.java.command");
      if (command != null && command.contains("GradleStart")) {
         System.setProperty("mixin.env.remapRefMap", "true");
      }

      return findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") > 132 ? MixinEnvironment.Phase.DEFAULT : MixinEnvironment.Phase.PREINIT;
   }

   @Override
   public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
      return MixinEnvironment.CompatibilityLevel.JAVA_8;
   }

   @Override
   protected ILogger createLogger(String name) {
      return new LoggerAdapterLog4j2(name);
   }

   @Override
   public void init() {
      if (findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") < 4) {
         logger.error("MixinBootstrap.doInit() called during a tweak constructor!");
      }

      List<String> tweakClasses = GlobalProperties.get(BLACKBOARD_KEY_TWEAKCLASSES);
      if (tweakClasses != null) {
         tweakClasses.add("org.spongepowered.asm.mixin.EnvironmentStateTweaker");
      }

      super.init();
   }

   @Override
   public Collection<String> getPlatformAgents() {
      return ImmutableList.of(
         "org.spongepowered.asm.launch.platform.MixinPlatformAgentFMLLegacy", "org.spongepowered.asm.launch.platform.MixinPlatformAgentLiteLoaderLegacy"
      );
   }

   @Override
   public IContainerHandle getPrimaryContainer() {
      URI uri = null;

      try {
         uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
         if (uri != null) {
            return new ContainerHandleURI(uri);
         }
      } catch (URISyntaxException var3) {
         var3.printStackTrace();
      }

      return new ContainerHandleVirtual(this.getName());
   }

   @Override
   public Collection<IContainerHandle> getMixinContainers() {
      ImmutableList.Builder<IContainerHandle> list = ImmutableList.builder();
      this.getContainersFromClassPath(list);
      this.getContainersFromAgents(list);
      return list.build();
   }

   private void getContainersFromClassPath(ImmutableList.Builder<IContainerHandle> list) {
      URL[] sources = this.getClassPath();
      if (sources != null) {
         for (URL url : sources) {
            try {
               URI uri = url.toURI();
               logger.debug("Scanning {} for mixin tweaker", new Object[]{uri});
               if ("file".equals(uri.getScheme()) && Files.toFile(uri).exists()) {
                  MainAttributes attributes = MainAttributes.of(uri);
                  String tweaker = attributes.get("TweakClass");
                  if ("org.spongepowered.asm.launch.MixinTweaker".equals(tweaker)) {
                     list.add(new ContainerHandleURI(uri));
                  }
               }
            } catch (Exception var10) {
               var10.printStackTrace();
            }
         }
      }
   }

   @Override
   public IClassProvider getClassProvider() {
      return this;
   }

   @Override
   public IClassBytecodeProvider getBytecodeProvider() {
      return this;
   }

   @Override
   public ITransformerProvider getTransformerProvider() {
      return this;
   }

   @Override
   public IClassTracker getClassTracker() {
      return this.getClassLoaderUtil();
   }

   @Override
   public IMixinAuditTrail getAuditTrail() {
      return null;
   }

   @Override
   public Class<?> findClass(String name) throws ClassNotFoundException {
      return Launch.classLoader.findClass(name);
   }

   @Override
   public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
      return Class.forName(name, initialize, Launch.classLoader);
   }

   @Override
   public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
      return Class.forName(name, initialize, Launch.class.getClassLoader());
   }

   @Override
   public void beginPhase() {
      Launch.classLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
      this.delegatedTransformers = null;
   }

   @Override
   public void checkEnv(Object bootSource) {
      if (bootSource.getClass().getClassLoader() != Launch.class.getClassLoader()) {
         throw new MixinException("Attempted to init the mixin environment in the wrong classloader");
      }
   }

   @Override
   public InputStream getResourceAsStream(String name) {
      return Launch.classLoader.getResourceAsStream(name);
   }

   @Deprecated
   @Override
   public URL[] getClassPath() {
      return Launch.classLoader.getSources().toArray(new URL[0]);
   }

   @Override
   public Collection<ITransformer> getTransformers() {
      List<IClassTransformer> transformers = Launch.classLoader.getTransformers();
      List<ITransformer> wrapped = new ArrayList<>(transformers.size());

      for (IClassTransformer transformer : transformers) {
         if (transformer instanceof ITransformer) {
            wrapped.add((ITransformer)transformer);
         } else {
            wrapped.add(new LegacyTransformerHandle(transformer));
         }

         if (transformer instanceof IClassNameTransformer) {
            logger.debug("Found name transformer: {}", new Object[]{transformer.getClass().getName()});
            this.nameTransformer = (IClassNameTransformer)transformer;
         }
      }

      return wrapped;
   }

   public List<ITransformer> getDelegatedTransformers() {
      return Collections.unmodifiableList(this.getDelegatedLegacyTransformers());
   }

   private List<ILegacyClassTransformer> getDelegatedLegacyTransformers() {
      if (this.delegatedTransformers == null) {
         this.buildTransformerDelegationList();
      }

      return this.delegatedTransformers;
   }

   private void buildTransformerDelegationList() {
      logger.debug("Rebuilding transformer delegation list:");
      this.delegatedTransformers = new ArrayList<>();

      for (ITransformer transformer : this.getTransformers()) {
         if (transformer instanceof ILegacyClassTransformer) {
            ILegacyClassTransformer legacyTransformer = (ILegacyClassTransformer)transformer;
            String transformerName = legacyTransformer.getName();
            boolean include = true;

            for (String excludeClass : excludeTransformers) {
               if (transformerName.contains(excludeClass)) {
                  include = false;
                  break;
               }
            }

            if (include && !legacyTransformer.isDelegationExcluded()) {
               logger.debug("  Adding:    {}", new Object[]{transformerName});
               this.delegatedTransformers.add(legacyTransformer);
            } else {
               logger.debug("  Excluding: {}", new Object[]{transformerName});
            }
         }
      }

      logger.debug("Transformer delegation list created with {} entries", new Object[]{this.delegatedTransformers.size()});
   }

   @Override
   public void addTransformerExclusion(String name) {
      excludeTransformers.add(name);
      this.delegatedTransformers = null;
   }

   @Deprecated
   public byte[] getClassBytes(String name, String transformedName) throws IOException {
      byte[] classBytes = Launch.classLoader.getClassBytes(name);
      if (classBytes != null) {
         return classBytes;
      } else {
         URLClassLoader appClassLoader;
         if (Launch.class.getClassLoader() instanceof URLClassLoader) {
            appClassLoader = (URLClassLoader)Launch.class.getClassLoader();
         } else {
            appClassLoader = new URLClassLoader(new URL[0], Launch.class.getClassLoader());
         }

         InputStream classStream = null;

         Object var7;
         try {
            String resourcePath = transformedName.replace('.', '/').concat(".class");
            classStream = appClassLoader.getResourceAsStream(resourcePath);
            return ByteStreams.toByteArray(classStream);
         } catch (Exception var11) {
            var7 = null;
         } finally {
            Closeables.closeQuietly(classStream);
         }

         return (byte[])var7;
      }
   }

   @Deprecated
   public byte[] getClassBytes(String className, boolean runTransformers) throws ClassNotFoundException, IOException {
      String transformedName = className.replace('/', '.');
      String name = this.unmapClassName(transformedName);
      Profiler profiler = Profiler.getProfiler("mixin");
      Profiler.Section loadTime = profiler.begin(1, "class.load");
      byte[] classBytes = this.getClassBytes(name, transformedName);
      loadTime.end();
      if (runTransformers) {
         Profiler.Section transformTime = profiler.begin(1, "class.transform");
         classBytes = this.applyTransformers(name, transformedName, classBytes, profiler);
         transformTime.end();
      }

      if (classBytes == null) {
         throw new ClassNotFoundException(String.format("The specified class '%s' was not found", transformedName));
      } else {
         return classBytes;
      }
   }

   private byte[] applyTransformers(String name, String transformedName, byte[] basicClass, Profiler profiler) {
      if (this.getClassLoaderUtil().isClassExcluded(name, transformedName)) {
         return basicClass;
      } else {
         for (ILegacyClassTransformer transformer : this.getDelegatedLegacyTransformers()) {
            this.lock.clear();
            int pos = transformer.getName().lastIndexOf(46);
            String simpleName = transformer.getName().substring(pos + 1);
            Profiler.Section transformTime = profiler.begin(2, simpleName.toLowerCase(Locale.ROOT));
            transformTime.setInfo(transformer.getName());
            basicClass = transformer.transformClassBytes(name, transformedName, basicClass);
            transformTime.end();
            if (this.lock.isSet()) {
               this.addTransformerExclusion(transformer.getName());
               this.lock.clear();
               logger.info("A re-entrant transformer '{}' was detected and will no longer process meta class data", new Object[]{transformer.getName()});
            }
         }

         return basicClass;
      }
   }

   private String unmapClassName(String className) {
      if (this.nameTransformer == null) {
         this.findNameTransformer();
      }

      return this.nameTransformer != null ? this.nameTransformer.unmapClassName(className) : className;
   }

   private void findNameTransformer() {
      for (IClassTransformer transformer : Launch.classLoader.getTransformers()) {
         if (transformer instanceof IClassNameTransformer) {
            logger.debug("Found name transformer: {}", new Object[]{transformer.getClass().getName()});
            this.nameTransformer = (IClassNameTransformer)transformer;
         }
      }
   }

   @Override
   public ClassNode getClassNode(String className) throws ClassNotFoundException, IOException {
      return this.getClassNode(className, this.getClassBytes(className, true), 8);
   }

   @Override
   public ClassNode getClassNode(String className, boolean runTransformers) throws ClassNotFoundException, IOException {
      return this.getClassNode(className, this.getClassBytes(className, runTransformers), 8);
   }

   @Override
   public ClassNode getClassNode(String className, boolean runTransformers, int flags) throws ClassNotFoundException, IOException {
      return this.getClassNode(className, this.getClassBytes(className, runTransformers), flags);
   }

   private ClassNode getClassNode(String className, byte[] classBytes, int flags) {
      ClassNode classNode = new ClassNode();
      ClassReader classReader = new MixinClassReader(classBytes, className);
      classReader.accept(classNode, flags);
      return classNode;
   }

   private static int findInStackTrace(String className, String methodName) {
      Thread currentThread = Thread.currentThread();
      if (!"main".equals(currentThread.getName())) {
         return 0;
      } else {
         StackTraceElement[] stackTrace = currentThread.getStackTrace();

         for (StackTraceElement s : stackTrace) {
            if (className.equals(s.getClassName()) && methodName.equals(s.getMethodName())) {
               return s.getLineNumber();
            }
         }

         return 0;
      }
   }

   private LaunchClassLoaderUtil getClassLoaderUtil() {
      if (this.classLoaderUtil == null) {
         this.classLoaderUtil = new LaunchClassLoaderUtil(Launch.classLoader);
      }

      return this.classLoaderUtil;
   }
}
