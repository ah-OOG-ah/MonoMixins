package com.gtnewhorizon.gtnhmixins.builders;

import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

public class TargetModBuilder implements ITargetMod {
   private String coreModClass;
   private String modId;
   private String targetClass;
   private Predicate<ClassNode> classNodeTest;
   private boolean foundTargetClass;

   public TargetModBuilder setCoreModClass(String coreModClass) {
      this.coreModClass = coreModClass;
      return this;
   }

   public TargetModBuilder setModId(String modId) {
      this.modId = modId;
      return this;
   }

   public TargetModBuilder setTargetClass(String targetClass) {
      this.targetClass = targetClass;
      return this;
   }

   public TargetModBuilder setClassNodeTest(Predicate<ClassNode> classNodeTest) {
      this.classNodeTest = classNodeTest;
      return this;
   }

   public TargetModBuilder testModID(String modId) {
      this.classNodeTest = ITargetMod.Predicates.testModAnnotation(ITargetMod.Predicates.equals(modId), null, null);
      return this;
   }

   public TargetModBuilder testModVersion(String modId, String modVersion) {
      this.classNodeTest = ITargetMod.Predicates.testModAnnotation(ITargetMod.Predicates.equals(modId), null, ITargetMod.Predicates.equals(modVersion));
      return this;
   }

   public TargetModBuilder testModVersion(String modId, Predicate<String> modVersionTest) {
      this.classNodeTest = ITargetMod.Predicates.testModAnnotation(ITargetMod.Predicates.equals(modId), null, modVersionTest);
      return this;
   }

   public TargetModBuilder testModAnnotation(Predicate<String> modIdTest, Predicate<String> modNameTest, Predicate<String> modVersionTest) {
      this.classNodeTest = ITargetMod.Predicates.testModAnnotation(modIdTest, modNameTest, modVersionTest);
      return this;
   }

   protected static void validateBuilder(TargetModBuilder builder, ITargetMod target, IBaseTransformer.Phase phaseIn) {
      if (builder == null) {
         throw new NullPointerException("TargetModBuilder is null for ITargetMod " + target);
      } else if (builder.modId == null && builder.coreModClass == null && builder.targetClass == null && builder.classNodeTest == null) {
         throw new IllegalArgumentException("No information at all provided by ITargetMod " + target);
      } else {
         if (phaseIn == IBaseTransformer.Phase.EARLY) {
            if (builder.coreModClass == null && builder.targetClass == null && builder.classNodeTest == null) {
               throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by early mixins");
            }
         } else if (phaseIn == IBaseTransformer.Phase.LATE) {
            if (builder.modId == null && builder.targetClass == null && builder.classNodeTest == null) {
               throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target + " used by late mixins");
            }
         } else if (builder.targetClass == null && builder.classNodeTest == null) {
            throw new IllegalArgumentException("Not enough information provided by ITargetMod " + target);
         }

         if (builder.classNodeTest != null && builder.targetClass == null) {
            throw new IllegalArgumentException("ITargetMod " + target + " uses a ClassNode test but doesn't specify the target class");
         }
      }
   }

   protected boolean isTargetPresent(Set<String> loadedCoreMods, Set<String> loadedMods) {
      if (!loadedCoreMods.isEmpty() && this.coreModClass != null && loadedCoreMods.contains(this.coreModClass)) {
         return true;
      } else if (!loadedMods.isEmpty() && this.modId != null && loadedMods.contains(this.modId)) {
         return true;
      } else {
         if (this.targetClass != null) {
            if (this.foundTargetClass) {
               return true;
            }

            try {
               ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(this.targetClass, false);
               if (this.classNodeTest == null) {
                  this.foundTargetClass = true;
                  return true;
               }

               boolean test = this.classNodeTest.test(classNode);
               if (test) {
                  this.foundTargetClass = true;
                  return true;
               }
            } catch (IOException | ClassNotFoundException var5) {
            }
         }

         return false;
      }
   }

   @Nonnull
   @Override
   public TargetModBuilder getBuilder() {
      return this;
   }
}
