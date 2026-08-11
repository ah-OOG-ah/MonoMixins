package io.github.legacymoddingmc.unimixins.gtnhmixins.repackage.common.config;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class PropertyToken {
   private List<String> comment;
   private String key;
   private String value;
   private boolean dirty;

   public PropertyToken(List<String> comment, String propertyString) {
      this.comment = ImmutableList.copyOf(comment);
      String[] pair = propertyString.split("=");
      if (pair.length != 2) {
         throw new RuntimeException();
      } else {
         this.key = pair[0];
         this.value = pair[1];
      }
   }

   public PropertyToken(List<String> comment, String key, String value) {
      this.comment = new ArrayList<>(comment);
      this.key = key;
      this.value = value;
   }

   public List<String> getComment() {
      return this.comment;
   }

   public void setComment(List<String> comment) {
      if (!comment.equals(this.comment)) {
         this.comment = ImmutableList.copyOf(comment);
         this.dirty = true;
      }
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(String key) {
      if (!key.equals(this.key)) {
         this.key = key;
         this.dirty = true;
      }
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      if (!value.equals(this.value)) {
         this.value = value;
         this.dirty = true;
      }
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty() {
      this.dirty = true;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof PropertyToken)) {
         return super.equals(obj);
      } else {
         PropertyToken o = (PropertyToken)obj;
         return this.comment.equals(o.comment) && this.key.equals(o.key) && this.value.equals(o.value);
      }
   }
}
