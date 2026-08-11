package org.spongepowered.asm.lib.util;

import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.FieldVisitor;
import org.spongepowered.asm.lib.TypePath;

public final class TraceFieldVisitor extends FieldVisitor {
   public final Printer p;

   public TraceFieldVisitor(Printer printer) {
      this(null, printer);
   }

   public TraceFieldVisitor(FieldVisitor fieldVisitor, Printer printer) {
      super(589824, fieldVisitor);
      this.p = printer;
   }

   @Override
   public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      Printer annotationPrinter = this.p.visitFieldAnnotation(descriptor, visible);
      return new TraceAnnotationVisitor(super.visitAnnotation(descriptor, visible), annotationPrinter);
   }

   @Override
   public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
      Printer annotationPrinter = this.p.visitFieldTypeAnnotation(typeRef, typePath, descriptor, visible);
      return new TraceAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), annotationPrinter);
   }

   @Override
   public void visitAttribute(Attribute attribute) {
      this.p.visitFieldAttribute(attribute);
      super.visitAttribute(attribute);
   }

   @Override
   public void visitEnd() {
      this.p.visitFieldEnd();
      super.visitEnd();
   }
}
