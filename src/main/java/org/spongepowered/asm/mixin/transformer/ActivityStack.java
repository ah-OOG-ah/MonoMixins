package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.extensibility.IActivityContext;

public class ActivityStack implements IActivityContext {
   public static final String GLUE_STRING = " -> ";
   private final ActivityStack.Activity head;
   private ActivityStack.Activity tail;
   private String glue;

   public ActivityStack() {
      this(null, " -> ");
   }

   public ActivityStack(String root) {
      this(root, " -> ");
   }

   public ActivityStack(String root, String glue) {
      this.head = this.tail = new ActivityStack.Activity(null, root);
      this.glue = glue;
   }

   @Override
   public void clear() {
      this.tail = this.head;
      this.head.next = null;
   }

   @Override
   public IActivityContext.IActivity begin(String description) {
      return this.tail = new ActivityStack.Activity(this.tail, description != null ? description : "null");
   }

   @Override
   public IActivityContext.IActivity begin(String descriptionFormat, Object... args) {
      if (descriptionFormat == null) {
         descriptionFormat = "null";
      }

      return this.tail = new ActivityStack.Activity(this.tail, String.format(descriptionFormat, args));
   }

   void end(ActivityStack.Activity activity) {
      this.tail = activity.last;
      this.tail.next = null;
   }

   @Override
   public String toString() {
      return this.toString(this.glue);
   }

   @Override
   public String toString(String glue) {
      if (this.head.description == null && this.head.next == null) {
         return "Unknown";
      } else {
         StringBuilder sb = new StringBuilder();

         for (ActivityStack.Activity activity = this.head; activity != null; activity = activity.next) {
            if (activity.description != null) {
               sb.append(activity.description);
               if (activity.next != null) {
                  sb.append(glue);
               }
            }
         }

         return sb.toString();
      }
   }

   public class Activity implements IActivityContext.IActivity {
      public String description;
      ActivityStack.Activity last;
      ActivityStack.Activity next;

      Activity(ActivityStack.Activity last, String description) {
         if (last != null) {
            last.next = this;
         }

         this.last = last;
         this.description = description;
      }

      @Override
      public void append(String text) {
         this.description = this.description != null ? this.description + text : text;
      }

      @Override
      public void append(String textFormat, Object... args) {
         this.append(String.format(textFormat, args));
      }

      @Override
      public void end() {
         if (this.last != null) {
            ActivityStack.this.end(this);
            this.last = null;
         }
      }

      @Override
      public void next(String description) {
         if (this.next != null) {
            this.next.end();
         }

         this.description = description;
      }

      @Override
      public void next(String descriptionFormat, Object... args) {
         if (descriptionFormat == null) {
            descriptionFormat = "null";
         }

         this.next(String.format(descriptionFormat, args));
      }
   }
}
