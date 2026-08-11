package com.llamalad7.mixinextras.versions;

import java.util.Collection;
import java.util.stream.Collectors;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.refmap.IMixinContext;

public class MixinVersionImpl_v0_8 extends MixinVersion {
   @Override
   public RuntimeException makeInvalidInjectionException(InjectionInfo info, String message) {
      return new InvalidInjectionException(info, message);
   }

   @Override
   public IMixinContext getMixin(InjectionInfo info) {
      return info.getContext();
   }

   @Override
   public LocalVariableDiscriminator.Context makeLvtContext(InjectionInfo info, Type returnType, boolean argsOnly, Target target, AbstractInsnNode node) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: Constructor org/spongepowered/asm/mixin/injection/modify/LocalVariableDiscriminator$Context.<init>(Lorg/spongepowered/asm/lib/Type;ZLorg/spongepowered/asm/mixin/injection/struct/Target;Lorg/spongepowered/asm/lib/tree/AbstractInsnNode;)V not found
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil.getSyntheticParametersMask(ExprUtil.java:49)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:959)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent.toJava(NewExprent.java:461)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1014)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent.toJava(ExitExprent.java:86)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:891)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:91)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1326)
      //
      // Bytecode:
      // 0: new org/spongepowered/asm/mixin/injection/modify/LocalVariableDiscriminator$Context
      // 3: dup
      // 4: aload 2
      // 5: iload 3
      // 6: aload 4
      // 8: aload 5
      // a: invokespecial org/spongepowered/asm/mixin/injection/modify/LocalVariableDiscriminator$Context.<init> (Lorg/spongepowered/asm/lib/Type;ZLorg/spongepowered/asm/mixin/injection/struct/Target;Lorg/spongepowered/asm/lib/tree/AbstractInsnNode;)V
      // d: areturn
   }

   @Override
   public void preInject(InjectionInfo info) {
      throw new AssertionError("Cannot preInject until 0.8.3");
   }

   @Override
   public AnnotationNode getAnnotation(InjectionInfo info) {
      return info.getAnnotation();
   }

   @Override
   public int getOrder(InjectionInfo info) {
      throw new AssertionError("Cannot getOrder until 0.8.7");
   }

   @Override
   public Collection<Target> getTargets(InjectionInfo info) {
      IMixinContext mixin = MixinVersion.getInstance().getMixin(info);
      return info.getTargets().stream().<Target>map(mixin::getTargetMethod).collect(Collectors.toList());
   }

   @Override
   public MemberInfo parseMemberInfo(String targetSelector, InjectionInfo info) {
      return MemberInfo.parse(targetSelector, info.getContext().getReferenceMapper(), info.getContext().getClassRef());
   }
}
