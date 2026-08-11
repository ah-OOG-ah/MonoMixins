package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.injector.StackExtension;
import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefUtils;
import com.llamalad7.mixinextras.utils.TargetDecorations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.util.Annotations;

public class ShareInfo {
   private int lvtIndex;
   private final ShareType shareType;
   private final Collection<AbstractInsnNode> initialization = new ArrayList<>();

   private ShareInfo(int lvtIndex, Type innerType) {
      this.lvtIndex = lvtIndex;
      this.shareType = new ShareType(innerType);
   }

   public int getLvtIndex() {
      return this.lvtIndex;
   }

   public void setLvtIndex(int lvtIndex) {
      this.lvtIndex = lvtIndex;
   }

   public ShareType getShareType() {
      return this.shareType;
   }

   public void addToLvt(Target target) {
      this.shareType.addToLvt(target, this.lvtIndex);
   }

   public InsnList initialize() {
      InsnList init = this.shareType.initialize(this.lvtIndex);
      this.initialization.addAll(Arrays.asList(init.toArray()));
      return init;
   }

   public AbstractInsnNode load() {
      return new VarInsnNode(25, this.lvtIndex);
   }

   public void stripInitializerFrom(MethodNode method) {
      this.initialization.forEach(method.instructions::remove);
   }

   public static ShareInfo getOrCreate(Target target, AnnotationNode shareAnnotation, Type paramType, IMixinInfo mixin, StackExtension stack) {
      if (SugarApplicator.isSugar(shareAnnotation.desc) && shareAnnotation.desc.endsWith("Share;")) {
         Type innerType = getInnerType(paramType);
         Map<ShareInfo.ShareId, ShareInfo> infos = TargetDecorations.getOrPut(target, "ShareSugar_Infos", HashMap::new);
         ShareInfo.ShareId id = getId(shareAnnotation, mixin);
         ShareInfo shareInfo = infos.get(id);
         if (shareInfo == null) {
            shareInfo = new ShareInfo(target.allocateLocal(), innerType);
            infos.put(id, shareInfo);
            shareInfo.addToLvt(target);
            target.insns.insert(shareInfo.initialize());
            if (stack != null) {
               stack.ensureAtLeast(innerType.getSize() + 2);
            }
         } else if (!innerType.equals(shareInfo.shareType.getInnerType())) {
            throw new SugarApplicationException(
               String.format("Share id %s in %s was requested for different types %s and %s!", id, target, innerType, shareInfo.shareType.getInnerType())
            );
         }

         return shareInfo;
      } else {
         return null;
      }
   }

   private static Type getInnerType(Type paramType) {
      Type innerType = LocalRefUtils.getTargetType(paramType, Type.getType(Object.class));
      if (innerType == paramType) {
         throw new SugarApplicationException("@Share parameter must be some variation of LocalRef.");
      } else {
         return innerType;
      }
   }

   private static ShareInfo.ShareId getId(AnnotationNode shareAnnotation, IMixinInfo mixin) {
      return new ShareInfo.ShareId(Annotations.getValue(shareAnnotation, "namespace", mixin.getClassName()), Annotations.getValue(shareAnnotation));
   }

   private static class ShareId {
      private final String namespace;
      private final String id;

      private ShareId(String namespace, String id) {
         this.namespace = namespace;
         this.id = id;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ShareInfo.ShareId shareId = (ShareInfo.ShareId)o;
            return Objects.equals(this.namespace, shareId.namespace) && Objects.equals(this.id, shareId.id);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.namespace, this.id);
      }

      @Override
      public String toString() {
         return this.namespace + ':' + this.id;
      }
   }
}
