package gkappa.modernsplash;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class MSLoadingPlugin implements IFMLLoadingPlugin {

    public static long expectedTime = 0;

    public MSLoadingPlugin() {
        expectedTime = TimeHistory.getEstimateTime();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "gkappa.modernsplash.asm.ReplaceSplashTransformer" };
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
