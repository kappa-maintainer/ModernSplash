package gkappa.modernsplash;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;

public class MSLoadingPlugin implements IFMLLoadingPlugin {

    public static long expectedTime = 0;

    public MSLoadingPlugin() {
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            expectedTime = TimeHistory.getEstimateTime();
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "gkappa.modernsplash.asm.ReplaceSplashTransformer" };
    }

}
