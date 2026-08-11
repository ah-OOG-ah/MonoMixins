package io.github.tox1cozz.mixinextras;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.spongepowered.asm.util.logging.MessageRouter;

@SupportedAnnotationTypes({})
public class MixinExtrasAnnotationProcessor extends AbstractProcessor {
   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      return false;
   }

   @Override
   public synchronized void init(ProcessingEnvironment processingEnv) {
      super.init(processingEnv);

      try {
         MessageRouter.setMessager(processingEnv.getMessager());
         MixinExtrasBootstrap.init();
      } catch (NoClassDefFoundError var3) {
      }
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
   }
}
