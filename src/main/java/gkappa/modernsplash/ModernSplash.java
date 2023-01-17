package gkappa.modernsplash;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.management.ManagementFactory;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        useMetadata = true,
        version = Reference.MOD_VERSION,
        dependencies = Reference.MOD_DEPENDENCIES
)
public class ModernSplash {
    
    public static Logger LOGGER = LogManager.getLogger("ModernSplash");

    public static long doneTime = 0;

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;
	@Instance(Reference.MOD_ID)
	public static ModernSplash _instance;
    public ModernSplash() {
        trueFullscreen = Minecraft.getMinecraft().gameSettings.fullScreen;
        Minecraft.getMinecraft().gameSettings.fullScreen = false;}



    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!triggered && CustomSplash.enableTimer && event.gui instanceof GuiMainMenu) {
            triggered = true;

            Minecraft.getMinecraft().gameSettings.fullScreen = trueFullscreen;
            if (Minecraft.getMinecraft().gameSettings.fullScreen && !Minecraft.getMinecraft().isFullScreen()) {
                Minecraft.getMinecraft().toggleFullscreen();
                Minecraft.getMinecraft().gameSettings.fullScreen = Minecraft.getMinecraft().isFullScreen();
            }

            startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
            LOGGER.info("Startup took " + startupTime + "ms.");

            doneTime = startupTime;

            TimeHistory.saveHistory(doneTime);
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event) {
        if (!hasLeftMainMenu && CustomSplash.enableTimer && event.gui instanceof GuiMainMenu) {
            hasBeenMainMenu = true;

            if (CustomSplash.displayStartupTimeOnMainMenu) {
                GuiMainMenu mainMenu = (GuiMainMenu) event.gui;
                long minutes = (startupTime / 1000) / 60;
                long seconds = (startupTime / 1000) % 60;

                float guiScale = (float) Minecraft.getMinecraft().gameSettings.guiScale;
                if (guiScale <= 0) guiScale = 1; // failsafe to prevent divide by 0

                String txt = "Startup took " + minutes + "m " + seconds + "s.";
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(txt, (mainMenu.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(txt)) / 2, 10, Color.YELLOW.getRGB());
            }

        } else if (hasBeenMainMenu) {
            hasLeftMainMenu = true;
        }
    }
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
