package gkappa.modernsplash;

import joptsimple.internal.Reflection;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MSLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

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
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }


    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList( "splash.mixins.json", "smoothfont.mixins.json" );
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if(mixinConfig.equals("splash.mixins.json")) return true;
        boolean sfLoaded = true;
        try {
            Class.forName("bre.smoothfont.mod_SmoothFont");
        } catch (Throwable ignored) {
            sfLoaded = false;
        }
        ModernSplash.LOGGER.info("Found SmoothFont: " + sfLoaded);
        if(mixinConfig.equals("smoothfont.mixins.json")) return sfLoaded;
        return IEarlyMixinLoader.super.shouldMixinConfigQueue(mixinConfig);
    }
}
