package io.github.legacymoddingmc.unimixins.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinErrorHandler implements IMixinErrorHandler {

    private static Map<String, List<String>> caughtMixinErrors = new HashMap<>();

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        for(String className : mixin.getTargetClasses()) {
            putError(className, th);
        }
        return null;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        putError(targetClassName, th);
        return null;
    }

    private static void putError(String className, Throwable th) {
        caughtMixinErrors.computeIfAbsent(className, (String k) -> new ArrayList<String>()).add(th.toString());
    }

    public static List<String> getErrorsForClass(String className) {
        return caughtMixinErrors.getOrDefault(className, Collections.EMPTY_LIST);
    }
}
