package io.github.legacymoddingmc.unimixins.mixingasm.repackage.common.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

class PropertyCollection {
   private Map<String, PropertyToken> properties = new TreeMap<>();

   public static PropertyCollection fromFile(File file) {
      PropertyCollection coll = new PropertyCollection();
      coll.readPropertyTokens(file);
      return coll;
   }

   private void readPropertyTokens(File file) {
      List<String> comment = new ArrayList<>();
      String prop = "";
      List<String> keyOrder = new ArrayList<>();
      if (file.exists()) {
         try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (true) {
               String line = br.readLine();
               if (line == null) {
                  break;
               }

               if (!line.isEmpty()) {
                  line = line.trim();
                  if (line.startsWith("#")) {
                     comment.add(line.substring(1).trim());
                  } else {
                     PropertyToken token = new PropertyToken(comment, line);
                     this.properties.put(token.getKey(), token);
                     keyOrder.add(token.getKey());
                     comment.clear();
                  }
               }
            }
         } catch (Exception var19) {
            var19.printStackTrace();
         }
      }
   }

   public void writeToFile(File file) {
      this.writePropertyTokens(file);
   }

   private void writePropertyTokens(File file) {
      boolean dirty = this.properties.values().stream().anyMatch(PropertyToken::isDirty);
      if (dirty) {
         file.getParentFile().mkdirs();

         try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Entry<String, PropertyToken> e : this.properties.entrySet()) {
               PropertyToken token = e.getValue();

               for (String commentLine : token.getComment()) {
                  bw.write("# " + commentLine + "\n");
               }

               bw.write(token.getKey() + "=" + token.getValue() + "\n\n");
            }
         } catch (Exception var20) {
            var20.printStackTrace();
         }
      }
   }

   public PropertyToken get(String key) {
      return this.properties.get(key);
   }

   public void put(String key, PropertyToken token) {
      PropertyToken old = this.properties.get(key);
      if (!token.equals(old)) {
         this.properties.put(key, token);
         token.setDirty();
      }
   }
}
