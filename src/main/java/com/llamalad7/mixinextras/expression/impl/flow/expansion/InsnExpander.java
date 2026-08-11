package com.llamalad7.mixinextras.expression.impl.flow.expansion;

import com.llamalad7.mixinextras.expression.impl.ExpressionService;
import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;
import com.llamalad7.mixinextras.expression.impl.flow.postprocessing.FlowPostProcessor;
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;

public abstract class InsnExpander implements FlowPostProcessor {
   private static final String INSN_COMPONENT = "expandedInsnComponent";
   private static final String EXPANSION = "expansion";
   private final Map<AbstractInsnNode, InsnExpander.Expansion> expansions = new IdentityHashMap<>();

   @Override
   public abstract void process(FlowValue var1, FlowPostProcessor.OutputSink var2);

   public abstract void expand(Target var1, InjectionNodes.InjectionNode var2, InsnExpander.Expansion var3);

   protected final void registerComponent(FlowValue node, InsnExpander.InsnComponent component, AbstractInsnNode compound) {
      node.decorate("expandedInsnComponent", component);
      this.expansions.computeIfAbsent(compound, x$0 -> new InsnExpander.Expansion(x$0));
      node.decorate("expansion", this.expansions.get(compound));
   }

   protected final void expandInsn(Target target, InjectionNodes.InjectionNode node, AbstractInsnNode... insns) {
      InsnList insnList = new InsnList();

      for (AbstractInsnNode insn : insns) {
         insnList.add(insn);
      }

      AbstractInsnNode insn = node.getCurrentTarget();
      target.insns.insert(insn, insnList);
      target.replaceNode(insn, dummyInsn());
   }

   protected static InsnNode dummyInsn() {
      return new InsnNode(0);
   }

   public static InsnExpander.Expansion prepareExpansion(FlowValue node, Target target, InjectionInfo info, ExpressionContext ctx) {
      if (!hasExpansion(node)) {
         return null;
      } else {
         checkSupportsExpansion(info, ctx.type);
         InsnExpander.Expansion expansion = node.getDecoration("expansion");
         AbstractInsnNode compoundInsn = expansion.compound;
         InjectionNodes.InjectionNode compoundNode = target.addInjectionNode(compoundInsn);
         if (!compoundNode.hasDecoration("mixinextras_expansionInfo")) {
            compoundNode.decorate("mixinextras_expansionInfo", expansion);
         }

         expansion.registerInterest(info, node.getDecoration("expandedInsnComponent"));
         return expansion;
      }
   }

   public static InjectionNodes.InjectionNode doExpansion(InjectionNodes.InjectionNode node, Target target, InjectionInfo info) {
      InsnExpander.Expansion expansion = node.getDecoration("mixinextras_expansionInfo");
      if (expansion == null) {
         return node;
      } else {
         expansion.doExpansion(target, node);
         return target.addInjectionNode(expansion.getTargetInsn(info));
      }
   }

   private static void checkSupportsExpansion(InjectionInfo info, ExpressionContext.Type type) {
      switch (type) {
         case SLICE:
         case INJECT:
         case MODIFY_VARIABLE:
            return;
         case MODIFY_EXPRESSION_VALUE:
         case WRAP_OPERATION:
            return;
         default:
            throw ExpressionService.getInstance()
               .makeInvalidInjectionException(info, String.format("Expression context type %s does not support compound instructions!", type));
      }
   }

   public static AbstractInsnNode getRepresentative(FlowValue node) {
      InsnExpander.Expansion expansion = node.getDecoration("expansion");
      return expansion != null ? expansion.compound : node.getInsn();
   }

   public static boolean hasExpansion(FlowValue node) {
      return node.hasDecoration("expansion");
   }

   public static void addExpansionStep(FlowValue node, Consumer<InjectionNodes.InjectionNode> step) {
      InsnExpander.Expansion expansion = node.getDecoration("expansion");
      expansion.addExpansionStep(node.getDecoration("expandedInsnComponent"), step);
   }

   public class Expansion {
      private final Map<InjectionInfo, InsnExpander.InsnComponent> interests = new IdentityHashMap<>();
      private final Map<InsnExpander.InsnComponent, List<Consumer<InjectionNodes.InjectionNode>>> expansionSteps = new HashMap<>();
      private final Map<InsnExpander.InsnComponent, AbstractInsnNode> expandedInsns = new HashMap<>();
      private boolean expanded = false;
      public final AbstractInsnNode compound;

      public Expansion(AbstractInsnNode compound) {
         this.compound = compound;
      }

      public void registerInterest(InjectionInfo info, InsnExpander.InsnComponent component) {
         if (this.interests.put(info, component) != null) {
            throw new UnsupportedOperationException("The same injector should not target multiple parts of a compound instruction!");
         }
      }

      public void decorate(InjectionInfo info, String key, Object value) {
         this.addExpansionStep(this.interests.get(info), node -> node.decorate(key, value));
      }

      public void decorateInjectorSpecific(InjectionInfo info, String key, Object value) {
         this.addExpansionStep(this.interests.get(info), node -> ExpressionService.getInstance().decorateInjectorSpecific(node, info, key, value));
      }

      public Set<InsnExpander.InsnComponent> registeredInterests() {
         return new HashSet<>(this.interests.values());
      }

      void addExpansionStep(InsnExpander.InsnComponent component, Consumer<InjectionNodes.InjectionNode> step) {
         this.expansionSteps.computeIfAbsent(component, k -> new ArrayList<>()).add(step);
      }

      void doExpansion(Target target, InjectionNodes.InjectionNode node) {
         if (!this.expanded) {
            this.expanded = true;
            InsnExpander.this.expand(target, node, this);

            for (Entry<InsnExpander.InsnComponent, List<Consumer<InjectionNodes.InjectionNode>>> steps : this.expansionSteps.entrySet()) {
               InjectionNodes.InjectionNode newNode = target.addInjectionNode(this.expandedInsns.get(steps.getKey()));

               for (Consumer<InjectionNodes.InjectionNode> step : steps.getValue()) {
                  step.accept(newNode);
               }
            }

            this.expansionSteps.clear();
         }
      }

      AbstractInsnNode getTargetInsn(InjectionInfo info) {
         return this.expandedInsns.get(this.interests.get(info));
      }

      protected AbstractInsnNode registerInsn(InsnExpander.InsnComponent component, AbstractInsnNode insn) {
         this.expandedInsns.put(component, insn);
         return insn;
      }
   }

   public interface InsnComponent {
   }
}
