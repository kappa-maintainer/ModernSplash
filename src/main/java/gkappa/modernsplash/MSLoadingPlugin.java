package gkappa.modernsplash;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MSLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        MixinBootstrap.init();
        Mixins.addConfiguration("splash.mixins.json");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    /*
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("splash.mixins.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if(mixinConfig.equals("splash.mixins.json")) return true;
        return IEarlyMixinLoader.super.shouldMixinConfigQueue(mixinConfig);
    }*/
}
