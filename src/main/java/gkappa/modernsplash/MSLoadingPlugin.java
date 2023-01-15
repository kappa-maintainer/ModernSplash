package gkappa.modernsplash;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

public class MSLoadingPlugin implements IFMLLoadingPlugin {

    public static long expectedTime = 0;

    public  MSLoadingPlugin() {
        if(FMLLaunchHandler.side() == Side.CLIENT) {
            expectedTime = TimeHistory.getEstimateTime();
        }
    }
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
