package org.spongepowered.asm.mixin.injection.points;

import java.util.ArrayList;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelector;
import org.spongepowered.asm.mixin.injection.selectors.ITargetSelectorConstructor;
import org.spongepowered.asm.mixin.injection.selectors.TargetSelector;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionPointException;
import org.spongepowered.include.com.google.common.base.Strings;

@InjectionPoint.AtCode("NEW")
public class BeforeNew extends InjectionPoint {
   private final String target;
   private final String desc;
   private final int ordinal;

   public BeforeNew(InjectionPointData data) {
      super(data);
      this.ordinal = data.getOrdinal();
      String target = Strings.emptyToNull(data.get("class", data.get("target", "")).replace('.', '/'));
      ITargetSelector member = TargetSelector.parseAndValidate(target, data.getContext());
      if (!(member instanceof ITargetSelectorConstructor)) {
         throw new InvalidInjectionPointException(data.getMixin(), "Failed parsing @At(\"NEW\") target descriptor \"%s\" on %s", target, data.getDescription());
      } else {
         ITargetSelectorConstructor targetSelector = (ITargetSelectorConstructor)member;
         this.target = targetSelector.toCtorType();
         this.desc = targetSelector.toCtorDesc();
      }
   }

   public boolean hasDescriptor() {
      return this.desc != null;
   }

   public String getDescriptor() {
      return this.desc;
   }

   @Override
   public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
      boolean found = false;
      int ordinal = 0;
      Collection<TypeInsnNode> newNodes = new ArrayList<>();
      Collection<AbstractInsnNode> candidates = this.desc != null ? newNodes : nodes;

      for (AbstractInsnNode insn : insns) {
         if (insn instanceof TypeInsnNode && insn.getOpcode() == 187 && this.matchesOwner((TypeInsnNode)insn)) {
            if (this.ordinal == -1 || this.ordinal == ordinal) {
               candidates.add(insn);
               found = this.desc == null;
            }

            ordinal++;
         }
      }

      if (this.desc != null) {
         for (TypeInsnNode newNode : newNodes) {
            if (findInitNodeFor(insns, newNode, this.desc) != null) {
               nodes.add(newNode);
               found = true;
            }
         }
      }

      return found;
   }

   public static MethodInsnNode findInitNodeFor(InsnList insns, TypeInsnNode newNode, String desc) {
      int indexOf = insns.indexOf(newNode);
      int depth = 0;

      for (AbstractInsnNode insn : insns) {
         if (insn instanceof MethodInsnNode && insn.getOpcode() == 183) {
            MethodInsnNode methodNode = (MethodInsnNode)insn;
            if ("<init>".equals(methodNode.name)) {
               if (--depth == 0) {
                  return !methodNode.owner.equals(newNode.desc) || desc != null && !methodNode.desc.equals(desc) ? null : methodNode;
               }
            }
         } else if (insn instanceof TypeInsnNode && insn.getOpcode() == 187) {
            depth++;
         }
      }

      return null;
   }

   private boolean matchesOwner(TypeInsnNode insn) {
      return this.target == null || this.target.equals(insn.desc);
   }
}
