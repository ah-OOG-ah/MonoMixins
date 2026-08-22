package io.github.legacymoddingmc.unimixins.compat.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonoMixinsRfbPlugin implements RfbPlugin {
    @Override
    public @NotNull RfbClassTransformer @Nullable [] makeTransformers() {
        return new RfbClassTransformer[] {
                new RFBUnshader()
        };
    }
}
