package org.spongepowered.asm.mixin.injection.selectors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.InvokeDynamicInsnNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.util.Handles;
import org.spongepowered.include.com.google.common.base.Strings;

public abstract class ElementNode<TNode> {
   public boolean isField() {
      return false;
   }

   public abstract ElementNode.NodeType getType();

   public MethodNode getMethod() {
      return null;
   }

   public FieldNode getField() {
      return null;
   }

   public AbstractInsnNode getInsn() {
      return null;
   }

   public abstract String getOwner();

   public abstract String getName();

   public String getSyntheticName() {
      return this.getName();
   }

   public abstract String getDesc();

   public String getDelegateDesc() {
      return this.getDesc();
   }

   public String getImplDesc() {
      return this.getDesc();
   }

   public abstract String getSignature();

   public abstract TNode get();

   @Override
   public String toString() {
      String desc = Strings.nullToEmpty(this.getDesc());
      if (!desc.isEmpty() && this.isField()) {
         desc = ":" + desc;
      }

      String owner = Strings.nullToEmpty(this.getOwner());
      if (!owner.isEmpty()) {
         owner = "L" + owner + ";";
      }

      return String.format("%s%s%s", owner, Strings.nullToEmpty(this.getName()), desc);
   }

   public static ElementNode<MethodNode> of(ClassNode owner, MethodNode method) {
      return new ElementNode.ElementNodeMethod(owner, method);
   }

   public static ElementNode<FieldNode> of(ClassNode owner, FieldNode field) {
      return new ElementNode.ElementNodeField(owner, field);
   }

   public static <TNode> ElementNode<TNode> of(ClassNode owner, TNode node) {
      if (node instanceof ElementNode) {
         return (ElementNode<TNode>)node;
      } else if (node instanceof MethodNode) {
         return new ElementNode.ElementNodeMethod(owner, (MethodNode)node);
      } else if (node instanceof FieldNode) {
         return new ElementNode.ElementNodeField(owner, (FieldNode)node);
      } else if (node instanceof MethodInsnNode) {
         return new ElementNode.ElementNodeMethodInsn((MethodInsnNode)node);
      } else if (node instanceof InvokeDynamicInsnNode) {
         return new ElementNode.ElementNodeInvokeDynamicInsn((InvokeDynamicInsnNode)node);
      } else if (node instanceof FieldInsnNode) {
         return new ElementNode.ElementNodeFieldInsn((FieldInsnNode)node);
      } else {
         throw new IllegalArgumentException("Could not create ElementNode for unknown node type: " + node.getClass().getName());
      }
   }

   public static <TNode extends AbstractInsnNode> ElementNode<TNode> of(TNode node) {
      if (node instanceof MethodInsnNode) {
         return new ElementNode.ElementNodeMethodInsn((MethodInsnNode)node);
      } else if (node instanceof InvokeDynamicInsnNode) {
         return new ElementNode.ElementNodeInvokeDynamicInsn((InvokeDynamicInsnNode)node);
      } else {
         return node instanceof FieldInsnNode ? new ElementNode.ElementNodeFieldInsn((FieldInsnNode)node) : null;
      }
   }

   public static <TNode> List<ElementNode<TNode>> listOf(ClassNode owner, List<TNode> list) {
      List<ElementNode<TNode>> nodes = new ArrayList<>();

      for (TNode node : list) {
         nodes.add(of(owner, node));
      }

      return nodes;
   }

   public static List<ElementNode<FieldNode>> fieldList(ClassNode owner) {
      List<ElementNode<FieldNode>> fields = new ArrayList<>();

      for (FieldNode field : owner.fields) {
         fields.add(new ElementNode.ElementNodeField(owner, field));
      }

      return fields;
   }

   public static List<ElementNode<MethodNode>> methodList(ClassNode owner) {
      List<ElementNode<MethodNode>> methods = new ArrayList<>();

      for (MethodNode method : owner.methods) {
         methods.add(new ElementNode.ElementNodeMethod(owner, method));
      }

      return methods;
   }

   public static Iterable<ElementNode<AbstractInsnNode>> insnList(InsnList insns) {
      return new ElementNode.ElementNodeIterable(insns, false);
   }

   public static Iterable<ElementNode<AbstractInsnNode>> dynamicInsnList(InsnList insns) {
      return new ElementNode.ElementNodeIterable(insns, true);
   }

   static class ElementNodeField extends ElementNode<FieldNode> {
      private final ClassNode owner;
      private final FieldNode field;

      ElementNodeField(ClassNode owner, FieldNode field) {
         this.owner = owner;
         this.field = field;
      }

      @Override
      public ElementNode.NodeType getType() {
         return ElementNode.NodeType.FIELD;
      }

      @Override
      public boolean isField() {
         return true;
      }

      @Override
      public FieldNode getField() {
         return this.field;
      }

      @Override
      public String getOwner() {
         return this.owner != null ? this.owner.name : null;
      }

      @Override
      public String getName() {
         return this.field.name;
      }

      @Override
      public String getDesc() {
         return this.field.desc;
      }

      @Override
      public String getSignature() {
         return this.field.signature;
      }

      public FieldNode get() {
         return this.field;
      }

      @Override
      public boolean equals(Object obj) {
         return this.field.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.field.hashCode();
      }
   }

   static class ElementNodeFieldInsn extends ElementNode<FieldInsnNode> {
      private FieldInsnNode insn;

      ElementNodeFieldInsn(FieldInsnNode field) {
         this.insn = field;
      }

      @Override
      public ElementNode.NodeType getType() {
         return ElementNode.NodeType.FIELD_INSN;
      }

      @Override
      public boolean isField() {
         return true;
      }

      @Override
      public AbstractInsnNode getInsn() {
         return this.insn;
      }

      @Override
      public String getOwner() {
         return this.insn.owner;
      }

      @Override
      public String getName() {
         return this.insn.name;
      }

      @Override
      public String getDesc() {
         return this.insn.desc;
      }

      @Override
      public String getSignature() {
         return null;
      }

      public FieldInsnNode get() {
         return this.insn;
      }

      @Override
      public boolean equals(Object obj) {
         return this.insn.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.insn.hashCode();
      }
   }

   static class ElementNodeInvokeDynamicInsn extends ElementNode<InvokeDynamicInsnNode> {
      private InvokeDynamicInsnNode insn;
      private Type samMethodType;
      private Handle implMethod;
      private Type instantiatedMethodType;

      ElementNodeInvokeDynamicInsn(InvokeDynamicInsnNode invokeDynamic) {
         this.insn = invokeDynamic;
         if (invokeDynamic.bsmArgs != null && invokeDynamic.bsmArgs.length > 1) {
            Object samMethodType = invokeDynamic.bsmArgs[0];
            Object implMethod = invokeDynamic.bsmArgs[1];
            Object instantiatedMethodType = invokeDynamic.bsmArgs[2];
            if (samMethodType instanceof Type && implMethod instanceof Handle && instantiatedMethodType instanceof Type) {
               this.samMethodType = (Type)samMethodType;
               this.implMethod = (Handle)implMethod;
               this.instantiatedMethodType = (Type)instantiatedMethodType;
            }
         }
      }

      @Override
      public ElementNode.NodeType getType() {
         return ElementNode.NodeType.INVOKEDYNAMIC_INSN;
      }

      @Override
      public boolean isField() {
         return this.implMethod != null && Handles.isField(this.implMethod);
      }

      @Override
      public AbstractInsnNode getInsn() {
         return this.insn;
      }

      @Override
      public String getOwner() {
         return this.implMethod != null ? this.implMethod.getOwner() : this.insn.name;
      }

      @Override
      public String getName() {
         return this.insn.name;
      }

      @Override
      public String getSyntheticName() {
         return this.implMethod != null ? this.implMethod.getName() : this.insn.name;
      }

      @Override
      public String getDesc() {
         return this.implMethod != null ? this.implMethod.getDesc() : this.insn.desc;
      }

      @Override
      public String getDelegateDesc() {
         return this.samMethodType != null ? this.samMethodType.getDescriptor() : this.getDesc();
      }

      @Override
      public String getImplDesc() {
         return this.instantiatedMethodType != null ? this.instantiatedMethodType.getDescriptor() : this.getDesc();
      }

      @Override
      public String getSignature() {
         return null;
      }

      public InvokeDynamicInsnNode get() {
         return this.insn;
      }

      @Override
      public boolean equals(Object obj) {
         return this.insn.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.insn.hashCode();
      }
   }

   static class ElementNodeIterable implements Iterable<ElementNode<AbstractInsnNode>> {
      private final Iterable<AbstractInsnNode> iterable;
      private final boolean filterDynamic;

      public ElementNodeIterable(Iterable<AbstractInsnNode> iterable, boolean filterDynamic) {
         this.iterable = iterable;
         this.filterDynamic = filterDynamic;
      }

      @Override
      public Iterator<ElementNode<AbstractInsnNode>> iterator() {
         return new ElementNode.ElementNodeIterator(this.iterable.iterator(), this.filterDynamic);
      }
   }

   static class ElementNodeIterator implements Iterator<ElementNode<AbstractInsnNode>> {
      private final Iterator<AbstractInsnNode> iter;
      private final boolean filterDynamic;

      ElementNodeIterator(Iterator<AbstractInsnNode> iter, boolean filterDynamic) {
         this.iter = iter;
         this.filterDynamic = filterDynamic;
      }

      @Override
      public boolean hasNext() {
         return this.iter.hasNext();
      }

      public ElementNode<AbstractInsnNode> next() {
         AbstractInsnNode elem = this.iter.next();
         return this.filterDynamic && (elem == null || elem.getOpcode() != 186) ? null : ElementNode.of(elem);
      }
   }

   static class ElementNodeMethod extends ElementNode<MethodNode> {
      private final ClassNode owner;
      private final MethodNode method;

      ElementNodeMethod(ClassNode owner, MethodNode method) {
         this.owner = owner;
         this.method = method;
      }

      @Override
      public ElementNode.NodeType getType() {
         return ElementNode.NodeType.METHOD;
      }

      @Override
      public MethodNode getMethod() {
         return this.method;
      }

      @Override
      public String getOwner() {
         return this.owner != null ? this.owner.name : null;
      }

      @Override
      public String getName() {
         return this.method.name;
      }

      @Override
      public String getDesc() {
         return this.method.desc;
      }

      @Override
      public String getSignature() {
         return this.method.signature;
      }

      public MethodNode get() {
         return this.method;
      }

      @Override
      public boolean equals(Object obj) {
         return this.method.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.method.hashCode();
      }
   }

   static class ElementNodeMethodInsn extends ElementNode<MethodInsnNode> {
      private MethodInsnNode insn;

      ElementNodeMethodInsn(MethodInsnNode method) {
         this.insn = method;
      }

      @Override
      public ElementNode.NodeType getType() {
         return ElementNode.NodeType.METHOD_INSN;
      }

      @Override
      public AbstractInsnNode getInsn() {
         return this.insn;
      }

      @Override
      public String getOwner() {
         return this.insn.owner;
      }

      @Override
      public String getName() {
         return this.insn.name;
      }

      @Override
      public String getDesc() {
         return this.insn.desc;
      }

      @Override
      public String getSignature() {
         return null;
      }

      public MethodInsnNode get() {
         return this.insn;
      }

      @Override
      public boolean equals(Object obj) {
         return this.insn.equals(obj);
      }

      @Override
      public int hashCode() {
         return this.insn.hashCode();
      }
   }

   public static enum NodeType {
      UNDEFINED(false, false, false),
      METHOD(true, false, false),
      FIELD(false, true, false),
      METHOD_INSN(false, false, true),
      FIELD_INSN(false, false, true),
      INVOKEDYNAMIC_INSN(false, false, true);

      public final boolean hasMethod;
      public final boolean hasField;
      public final boolean hasInsn;

      private NodeType(boolean isMethod, boolean isField, boolean isInsn) {
         this.hasMethod = isMethod;
         this.hasField = isField;
         this.hasInsn = isInsn;
      }
   }
}
