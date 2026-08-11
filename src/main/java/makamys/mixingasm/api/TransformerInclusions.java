package makamys.mixingasm.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.Launch;

public class TransformerInclusions {
   private static final String INCLUSION_LIST_BLACKBOARD_KEY = "mixingasm.transformerInclusionList";

   public static List<String> getTransformerInclusionList() {
      List<String> list = (List<String>)Launch.blackboard.get("mixingasm.transformerInclusionList");
      if (list == null) {
         Launch.blackboard.put("mixingasm.transformerInclusionList", list = new ArrayList<>());
      }

      return list;
   }
}
