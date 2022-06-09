package gkappa.modernsplash;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        useMetadata = true,
        version = Reference.MOD_VERSION,
        dependencies = Reference.MOD_DEPENDENCIES
)
public class ModernSplash {
    

	@Instance(Reference.MOD_ID)
	public static ModernSplash _instance;
    public ModernSplash() {}
        
    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
    }
    
    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
    }
}
