package gkappa.modernsplash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.lang.management.ManagementFactory;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        useMetadata = true,
        version = Reference.VERSION,
        clientSideOnly = true
)
public class ModernSplash {

    public static final Logger LOGGER = LogManager.getLogger("ModernSplash");

    public static long doneTime = 0;

    public static volatile long fadeOutStart = -1L;
    public static volatile long fadePhase2Start = -1L;
    public static final long FADE_DURATION = 1000L;
    public static final long FADE_PHASE1 = 500L;
    public static final long FADE_PHASE2 = 500L;
    public static volatile int logoGlTextureName;

    public static volatile boolean gameStarted = false;

    boolean triggered = false;
    boolean trueFullscreen;

    long startupTime;
    boolean hasBeenMainMenu = false;
    boolean hasLeftMainMenu = false;
	@Instance(Reference.MOD_ID)
	public static ModernSplash _instance;
    public ModernSplash() {
        trueFullscreen = Minecraft.getMinecraft().gameSettings.fullScreen;
        Minecraft.getMinecraft().gameSettings.fullScreen = false;
    }

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!triggered && event.getGui() instanceof GuiMainMenu) {
            triggered = true;
            gameStarted = true;

            Minecraft.getMinecraft().gameSettings.fullScreen = trueFullscreen;
            if (Minecraft.getMinecraft().gameSettings.fullScreen && !Minecraft.getMinecraft().isFullScreen()) {
                Minecraft.getMinecraft().toggleFullscreen();
                Minecraft.getMinecraft().gameSettings.fullScreen = Minecraft.getMinecraft().isFullScreen();
            }

            if (Config.enableTimer) {
                startupTime = ManagementFactory.getRuntimeMXBean().getUptime();
                LOGGER.info("Startup took {}ms.", startupTime);

                doneTime = startupTime;

                TimeHistory.saveHistory(doneTime);
            }
        }

        if (fadePhase2Start < 0 && fadeOutStart > 0 && event.getGui() instanceof GuiMainMenu) {
            fadePhase2Start = System.nanoTime();
        }
    }

    @SubscribeEvent
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (fadePhase2Start > 0 && event.getGui() instanceof GuiMainMenu menu) {
            long elapsed = (System.nanoTime() - fadePhase2Start) / 1000000;
            if (elapsed < FADE_PHASE2) {
                float alpha = 1.0f - (float) elapsed / FADE_PHASE2;
                drawFadeOverlay(menu.width, menu.height, alpha);
            } else {
                fadePhase2Start = -1;
                fadeOutStart = -1;
                if (logoGlTextureName != 0) {
                    GL11.glDeleteTextures(logoGlTextureName);
                    logoGlTextureName = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event){
        if(!hasLeftMainMenu && Config.enableTimer && event.getGui() instanceof GuiMainMenu mainMenu){
            hasBeenMainMenu = true;

            if(Config.displayStartupTimeOnMainMenu) {
                long minutes = (startupTime / 1000) / 60;
                long seconds = (startupTime / 1000) % 60;

                float guiScale = (float)Minecraft.getMinecraft().gameSettings.guiScale;
                if(guiScale <= 0) guiScale = 1; // failsafe to prevent divide by 0

                String txt = "Startup took " + minutes + "m " + seconds + "s.";
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(txt, (float) (mainMenu.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(txt)) /2, 10, Color.YELLOW.getRGB());
            }

        }else if(hasBeenMainMenu){
            hasLeftMainMenu = true;
        }
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

    private static void drawFadeOverlay(int width, int height, float alpha) {
        if (alpha <= 0.0f) return;

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int bg = Config.backgroundColor;
        int a = (int) (alpha * 255);
        int bgColor = (a << 24) | (bg & 0x00FFFFFF);

        Gui.drawRect(0, 0, width, height, bgColor);

        if (logoGlTextureName != 0) {
            int displayW = Display.getWidth();
            int rawLogoSize = 512;
            int logoSize = (int)(rawLogoSize * (double) width / displayW);
            int logoX = (width - logoSize) / 2;
            int logoY = (height - logoSize) / 2;
            float r = ((Config.logoColor >> 16) & 0xFF) / 255.0f;
            float g = ((Config.logoColor >> 8) & 0xFF) / 255.0f;
            float b = (Config.logoColor & 0xFF) / 255.0f;

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
            GL11.glPushMatrix();
            GL20.glUseProgram(0);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GL11.glColor4f(r, g, b, alpha);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, logoGlTextureName);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(logoX, logoY);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(logoX, logoY + logoSize);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(logoX + logoSize, logoY + logoSize);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(logoX + logoSize, logoY);
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
