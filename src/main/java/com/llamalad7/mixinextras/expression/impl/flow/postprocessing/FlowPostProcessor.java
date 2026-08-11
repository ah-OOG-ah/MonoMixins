package com.llamalad7.mixinextras.expression.impl.flow.postprocessing;

import com.llamalad7.mixinextras.expression.impl.flow.FlowValue;

public interface FlowPostProcessor {
   void process(FlowValue var1, FlowPostProcessor.OutputSink var2);

   public interface OutputSink {
      void markAsSynthetic(FlowValue var1);

      void registerFlow(FlowValue... var1);
   }
}
