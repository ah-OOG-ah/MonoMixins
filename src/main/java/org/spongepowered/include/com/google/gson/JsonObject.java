package org.spongepowered.include.com.google.gson;

import java.util.Set;
import java.util.Map.Entry;
import org.spongepowered.include.com.google.gson.internal.LinkedTreeMap;

public final class JsonObject extends JsonElement {
   private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap<>();

   public void add(String property, JsonElement value) {
      if (value == null) {
         value = JsonNull.INSTANCE;
      }

      this.members.put(property, value);
   }

   public Set<Entry<String, JsonElement>> entrySet() {
      return this.members.entrySet();
   }

   @Override
   public boolean equals(Object o) {
      return o == this || o instanceof JsonObject && ((JsonObject)o).members.equals(this.members);
   }

   @Override
   public int hashCode() {
      return this.members.hashCode();
   }
}
