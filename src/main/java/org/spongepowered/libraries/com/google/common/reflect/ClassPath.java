package org.spongepowered.libraries.com.google.common.reflect;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.spongepowered.libraries.com.google.common.annotations.Beta;
import org.spongepowered.libraries.com.google.common.annotations.VisibleForTesting;
import org.spongepowered.libraries.com.google.common.base.CharMatcher;
import org.spongepowered.libraries.com.google.common.base.Preconditions;
import org.spongepowered.libraries.com.google.common.base.Predicate;
import org.spongepowered.libraries.com.google.common.base.Splitter;
import org.spongepowered.libraries.com.google.common.collect.FluentIterable;
import org.spongepowered.libraries.com.google.common.collect.ImmutableMap;
import org.spongepowered.libraries.com.google.common.collect.ImmutableSet;
import org.spongepowered.libraries.com.google.common.collect.Maps;
import org.spongepowered.libraries.com.google.common.collect.MultimapBuilder;
import org.spongepowered.libraries.com.google.common.collect.SetMultimap;
import org.spongepowered.libraries.com.google.common.collect.Sets;
import org.spongepowered.libraries.com.google.common.io.ByteSource;
import org.spongepowered.libraries.com.google.common.io.CharSource;
import org.spongepowered.libraries.com.google.common.io.Resources;

@Beta
public final class ClassPath {
   private static final Logger logger = Logger.getLogger(ClassPath.class.getName());
   private static final Predicate<ClassPath.ClassInfo> IS_TOP_LEVEL = new Predicate<ClassPath.ClassInfo>() {
      public boolean apply(ClassPath.ClassInfo info) {
         return info.className.indexOf(36) == -1;
      }
   };
   private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ").omitEmptyStrings();
   private static final String CLASS_FILE_NAME_EXTENSION = ".class";
   private final ImmutableSet<ClassPath.ResourceInfo> resources;

   private ClassPath(ImmutableSet<ClassPath.ResourceInfo> resources) {
      this.resources = resources;
   }

   public static ClassPath from(ClassLoader classloader) throws IOException {
      ClassPath.DefaultScanner scanner = new ClassPath.DefaultScanner();
      scanner.scan(classloader);
      return new ClassPath(scanner.getResources());
   }

   public ImmutableSet<ClassPath.ResourceInfo> getResources() {
      return this.resources;
   }

   public ImmutableSet<ClassPath.ClassInfo> getAllClasses() {
      return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).toSet();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses() {
      return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).filter(IS_TOP_LEVEL).toSet();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses(String packageName) {
      Preconditions.checkNotNull(packageName);
      ImmutableSet.Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();

      for (ClassPath.ClassInfo classInfo : this.getTopLevelClasses()) {
         if (classInfo.getPackageName().equals(packageName)) {
            builder.add(classInfo);
         }
      }

      return builder.build();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClassesRecursive(String packageName) {
      Preconditions.checkNotNull(packageName);
      String packagePrefix = packageName + '.';
      ImmutableSet.Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();

      for (ClassPath.ClassInfo classInfo : this.getTopLevelClasses()) {
         if (classInfo.getName().startsWith(packagePrefix)) {
            builder.add(classInfo);
         }
      }

      return builder.build();
   }

   @VisibleForTesting
   static String getClassName(String filename) {
      int classNameEnd = filename.length() - ".class".length();
      return filename.substring(0, classNameEnd).replace('/', '.');
   }

   @Beta
   public static final class ClassInfo extends ClassPath.ResourceInfo {
      private final String className;

      ClassInfo(String resourceName, ClassLoader loader) {
         super(resourceName, loader);
         this.className = ClassPath.getClassName(resourceName);
      }

      public String getPackageName() {
         return Reflection.getPackageName(this.className);
      }

      public String getSimpleName() {
         int lastDollarSign = this.className.lastIndexOf(36);
         if (lastDollarSign != -1) {
            String innerClassName = this.className.substring(lastDollarSign + 1);
            return CharMatcher.digit().trimLeadingFrom(innerClassName);
         } else {
            String packageName = this.getPackageName();
            return packageName.isEmpty() ? this.className : this.className.substring(packageName.length() + 1);
         }
      }

      public String getName() {
         return this.className;
      }

      public Class<?> load() {
         try {
            return this.loader.loadClass(this.className);
         } catch (ClassNotFoundException var2) {
            throw new IllegalStateException(var2);
         }
      }

      @Override
      public String toString() {
         return this.className;
      }
   }

   @VisibleForTesting
   static final class DefaultScanner extends ClassPath.Scanner {
      private final SetMultimap<ClassLoader, String> resources = MultimapBuilder.hashKeys().linkedHashSetValues().build();

      ImmutableSet<ClassPath.ResourceInfo> getResources() {
         ImmutableSet.Builder<ClassPath.ResourceInfo> builder = ImmutableSet.builder();

         for (Entry<ClassLoader, String> entry : this.resources.entries()) {
            builder.add(ClassPath.ResourceInfo.of(entry.getValue(), entry.getKey()));
         }

         return builder.build();
      }

      @Override
      protected void scanJarFile(ClassLoader classloader, JarFile file) {
         Enumeration<JarEntry> entries = file.entries();

         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && !entry.getName().equals("META-INF/MANIFEST.MF")) {
               this.resources.get(classloader).add(entry.getName());
            }
         }
      }

      @Override
      protected void scanDirectory(ClassLoader classloader, File directory) throws IOException {
         this.scanDirectory(directory, classloader, "");
      }

      private void scanDirectory(File directory, ClassLoader classloader, String packagePrefix) throws IOException {
         File[] files = directory.listFiles();
         if (files == null) {
            ClassPath.logger.warning("Cannot read directory " + directory);
         } else {
            for (File f : files) {
               String name = f.getName();
               if (f.isDirectory()) {
                  this.scanDirectory(f, classloader, packagePrefix + name + "/");
               } else {
                  String resourceName = packagePrefix + name;
                  if (!resourceName.equals("META-INF/MANIFEST.MF")) {
                     this.resources.get(classloader).add(resourceName);
                  }
               }
            }
         }
      }
   }

   @Beta
   public static class ResourceInfo {
      private final String resourceName;
      final ClassLoader loader;

      static ClassPath.ResourceInfo of(String resourceName, ClassLoader loader) {
         return (ClassPath.ResourceInfo)(resourceName.endsWith(".class")
            ? new ClassPath.ClassInfo(resourceName, loader)
            : new ClassPath.ResourceInfo(resourceName, loader));
      }

      ResourceInfo(String resourceName, ClassLoader loader) {
         this.resourceName = Preconditions.checkNotNull(resourceName);
         this.loader = Preconditions.checkNotNull(loader);
      }

      public final URL url() {
         URL url = this.loader.getResource(this.resourceName);
         if (url == null) {
            throw new NoSuchElementException(this.resourceName);
         } else {
            return url;
         }
      }

      public final ByteSource asByteSource() {
         return Resources.asByteSource(this.url());
      }

      public final CharSource asCharSource(Charset charset) {
         return Resources.asCharSource(this.url(), charset);
      }

      public final String getResourceName() {
         return this.resourceName;
      }

      @Override
      public int hashCode() {
         return this.resourceName.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof ClassPath.ResourceInfo)) {
            return false;
         } else {
            ClassPath.ResourceInfo that = (ClassPath.ResourceInfo)obj;
            return this.resourceName.equals(that.resourceName) && this.loader == that.loader;
         }
      }

      @Override
      public String toString() {
         return this.resourceName;
      }
   }

   abstract static class Scanner {
      private final Set<File> scannedUris = Sets.newHashSet();

      public final void scan(ClassLoader classloader) throws IOException {
         for (Entry<File, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            this.scan(entry.getKey(), entry.getValue());
         }
      }

      protected abstract void scanDirectory(ClassLoader var1, File var2) throws IOException;

      protected abstract void scanJarFile(ClassLoader var1, JarFile var2) throws IOException;

      @VisibleForTesting
      final void scan(File file, ClassLoader classloader) throws IOException {
         if (this.scannedUris.add(file.getCanonicalFile())) {
            this.scanFrom(file, classloader);
         }
      }

      private void scanFrom(File file, ClassLoader classloader) throws IOException {
         try {
            if (!file.exists()) {
               return;
            }
         } catch (SecurityException var4) {
            ClassPath.logger.warning("Cannot access " + file + ": " + var4);
            return;
         }

         if (file.isDirectory()) {
            this.scanDirectory(classloader, file);
         } else {
            this.scanJar(file, classloader);
         }
      }

      private void scanJar(File file, ClassLoader classloader) throws IOException {
         JarFile jarFile;
         try {
            jarFile = new JarFile(file);
         } catch (IOException var13) {
            return;
         }

         try {
            for (File path : getClassPathFromManifest(file, jarFile.getManifest())) {
               this.scan(path, classloader);
            }

            this.scanJarFile(classloader, jarFile);
         } finally {
            try {
               jarFile.close();
            } catch (IOException var12) {
            }
         }
      }

      @VisibleForTesting
      static ImmutableSet<File> getClassPathFromManifest(File jarFile, @Nullable Manifest manifest) {
         if (manifest == null) {
            return ImmutableSet.of();
         } else {
            ImmutableSet.Builder<File> builder = ImmutableSet.builder();
            String classpathAttribute = manifest.getMainAttributes().getValue(Name.CLASS_PATH.toString());
            if (classpathAttribute != null) {
               for (String path : ClassPath.CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
                  URL url;
                  try {
                     url = getClassPathEntry(jarFile, path);
                  } catch (MalformedURLException var8) {
                     ClassPath.logger.warning("Invalid Class-Path entry: " + path);
                     continue;
                  }

                  if (url.getProtocol().equals("file")) {
                     builder.add(new File(url.getFile()));
                  }
               }
            }

            return builder.build();
         }
      }

      @VisibleForTesting
      static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
         LinkedHashMap<File, ClassLoader> entries = Maps.newLinkedHashMap();
         ClassLoader parent = classloader.getParent();
         if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
         }

         if (classloader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader)classloader;

            for (URL entry : urlClassLoader.getURLs()) {
               if (entry.getProtocol().equals("file")) {
                  File file = new File(entry.getFile());
                  if (!entries.containsKey(file)) {
                     entries.put(file, classloader);
                  }
               }
            }
         }

         return ImmutableMap.copyOf(entries);
      }

      @VisibleForTesting
      static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
         return new URL(jarFile.toURI().toURL(), path);
      }
   }
}
