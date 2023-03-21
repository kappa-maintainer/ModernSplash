package gkappa.modernsplash;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

public class MSLateMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("smoothfont.mixins.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if (mixinConfig.equals("smoothfont.mixins.json")) {
            return Loader.isModLoaded("smoothfont");
        }
        return false;
    }
}
