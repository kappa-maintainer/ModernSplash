package gkappa.modernsplash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.MixinEnvironment;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class MSLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public static long expectedTime = 0;
    public static final MixinEnvironment.Side side = MixinEnvironment.getCurrentEnvironment()
        .getSide();

    public MSLoadingPlugin() {
        if (side == MixinEnvironment.Side.CLIENT) {
            expectedTime = TimeHistory.getEstimateTime();
        }
    }

    @Override
    public String getMixinConfig() {
        return "mixins.modernsplash.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<>();
        if (side == MixinEnvironment.Side.CLIENT) {
            mixins.add("FMLClientHandlerMixin");
            mixins.add("MinecraftMixin");
            mixins.add("TextureUtilMixin");
        }
        return mixins;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

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
}
