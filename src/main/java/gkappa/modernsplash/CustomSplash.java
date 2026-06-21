/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package gkappa.modernsplash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.SharedDrawable;
import org.lwjgl.util.glu.GLU;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

/**
 * Not a fully fleshed out API, may change in future MC versions.
 * However feel free to use and suggest additions.
 */
public class CustomSplash
{
    private static Drawable d;
    private static volatile boolean pause = false;
    private static volatile boolean done = false;
    private static Thread thread;
    private static volatile Throwable threadError;
    private static int angle = 0;
    private static final Lock lock = new ReentrantLock(true);
    private static SplashFontRenderer fontRenderer;

    private static final IResourcePack mcPack = Minecraft.getMinecraft().defaultResourcePack;
    private static final IResourcePack fmlPack = createResourcePack(FMLSanityChecker.fmlLocation);
    private static IResourcePack miscPack;

    private static Texture fontTexture;
    private static Texture logoTexture;
    private static Texture forgeTexture;

    private static float memoryColorPercent;
    private static long memoryColorChangeTime;
    public static boolean isDisplayVSyncForced = false;
    private static final int TIMING_FRAME_COUNT = 200;
    private static final int TIMING_FRAME_THRESHOLD = TIMING_FRAME_COUNT * 5 * 1000000; // 5 ms per frame, scaled to nanos

    public static final Semaphore mutex = new Semaphore(1);

    public static void start()
    {
        Config.load();

        if (!Config.enabled) return;

        final ResourceLocation fontLoc = Config.fontLoc;
        final ResourceLocation logoLoc = new ResourceLocation("modernsplash:textures/gui/title/mojang.png");
        final ResourceLocation forgeLoc = Config.forgeLoc;
        final ResourceLocation forgeFallbackLoc = new ResourceLocation("fml:textures/gui/forge.png");

        miscPack = createResourcePack(Config.miscPackFile);
        // getting debug info out of the way, while we still can
        FMLCommonHandler.instance().registerCrashCallable(new ICrashCallable()
        {
            @Override
            public String call() throws Exception
            {
                if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
                {
                    return "' Vendor: '" + glGetString(GL_VENDOR) +
                            "' Version: '" + glGetString(GL_VERSION) +
                            "' Renderer: '" + glGetString(GL_RENDERER) +
                            "'";
                }
                return "No OpenGL context found in the current thread: " + Thread.currentThread().getName();
            }

            @Override
            public String getLabel()
            {
                return "GL info";
            }
        });
        CrashReport report = CrashReport.makeCrashReport(new Throwable(), "Loading screen debug info");
        StringBuilder systemDetailsBuilder = new StringBuilder();
        report.getCategory().appendToStringBuilder(systemDetailsBuilder);
        FMLLog.log.info(systemDetailsBuilder.toString());

        try
        {
            d = new SharedDrawable(Display.getDrawable());
            Display.getDrawable().releaseContext();
            d.makeCurrent();
        }
        catch (LWJGLException e)
        {
            FMLLog.log.error("Error starting SplashProgress:", e);
            disableSplash(e);
        }

        //Call this ASAP if splash is enabled so that threading doesn't cause issues later
        getMaxTextureSize();

        //Thread mainThread = Thread.currentThread();
        thread = new Thread(new Runnable()
        {
            private final int barWidth = 400;
            private final int barHeight = 20;
            private final int textHeight2 = 20;
            private final int barOffset = 45;
            private long updateTiming;
            private long framecount;
            private float smoothProgressFirst = 0.0f;
            private float smoothProgressPenult = 0.0f;
            private float smoothProgressLast = 0.0f;
            @Override
            public void run()
            {
                setGL();
                fontTexture = new Texture(fontLoc, null);
                logoTexture = new Texture(logoLoc, null, true);
                forgeTexture = new Texture(forgeLoc, forgeFallbackLoc);
                glEnable(GL_TEXTURE_2D);
                fontRenderer = new SplashFontRenderer();
                glDisable(GL_TEXTURE_2D);
                while(!done)
                {
                    framecount++;

                    long fadeProgress = 0;
                    float phase1Alpha = 1.0f;
                    if (ModernSplash.fadeOutStart > 0) {
                        fadeProgress = (System.nanoTime() - ModernSplash.fadeOutStart) / 1000000; // ms
                        if (fadeProgress < ModernSplash.FADE_PHASE1) {
                            phase1Alpha = 1.0f - (float) fadeProgress / ModernSplash.FADE_PHASE1;
                        } else {
                            // phase 2: splash thread exits, main thread draws overlay
                            done = true;
                            continue;
                        }
                    }

                    ProgressBar first = null, penult = null, last = null;
                    Iterator<ProgressBar> i = ProgressManager.barIterator();
                    while(i.hasNext())
                    {
                        if(first == null) first = i.next();
                        else
                        {
                            penult = last;
                            last = i.next();
                        }
                    }

                    if (first != null) {
                        float target = (float) (first.getStep() + 1) / (first.getSteps() + 1);
                        smoothProgressFirst = smoothProgressFirst * 0.95f + target * 0.05f;
                        if (smoothProgressFirst < 0.0f) smoothProgressFirst = 0.0f;
                        if (smoothProgressFirst > 1.0f) smoothProgressFirst = 1.0f;
                    } else if (ModernSplash.fadeOutStart > 0 && smoothProgressFirst < 0.99f) {
                        smoothProgressFirst = smoothProgressFirst * 0.95f + 0.05f;
                        if (smoothProgressFirst > 1.0f) smoothProgressFirst = 1.0f;
                        ModernSplash.fadeOutStart = System.nanoTime();
                    }
                    if (penult != null) {
                        float target = (float) (penult.getStep() + 1) / (penult.getSteps() + 1);
                        smoothProgressPenult = smoothProgressPenult * 0.95f + target * 0.05f;
                        if (smoothProgressPenult < 0.0f) smoothProgressPenult = 0.0f;
                        if (smoothProgressPenult > 1.0f) smoothProgressPenult = 1.0f;
                    }
                    if (last != null) {
                        float target = (float) (last.getStep() + 1) / (last.getSteps() + 1);
                        smoothProgressLast = smoothProgressLast * 0.95f + target * 0.05f;
                        if (smoothProgressLast < 0.0f) smoothProgressLast = 0.0f;
                        if (smoothProgressLast > 1.0f) smoothProgressLast = 1.0f;
                    }

                    glClear(GL_COLOR_BUFFER_BIT);

                    // matrix setup
                    int w = Display.getWidth();
                    int h = Display.getHeight();
                    glViewport(0, 0, w, h);
                    glMatrixMode(GL_PROJECTION);
                    glLoadIdentity();
                    glOrtho(320 - w/2, 320 + w/2, 240 + h/2, 240 - h/2, -1, 1);
                    glMatrixMode(GL_MODELVIEW);
                    glLoadIdentity();

                    // mojang logo
                    if (phase1Alpha > 0.0f) {
                        setColorWithAlpha(Config.logoColor, phase1Alpha);
                        glEnable(GL_TEXTURE_2D);
                        logoTexture.bind();
                        glBegin(GL_QUADS);
                        logoTexture.texCoord(0, 0, 0);
                        glVertex2f(320 - 256, 240 - 256);
                        logoTexture.texCoord(0, 0, 1);
                        glVertex2f(320 - 256, 240 + 256);
                        logoTexture.texCoord(0, 1, 1);
                        glVertex2f(320 + 256, 240 + 256);
                        logoTexture.texCoord(0, 1, 0);
                        glVertex2f(320 + 256, 240 - 256);
                        glEnd();
                        glDisable(GL_TEXTURE_2D);
                    }

                    // memory usage
                    if (Config.showMemory && !Config.mimicModern)
                    {
                        glPushMatrix();
                        glTranslatef(320 - (float) barWidth / 2, 20, 0);
                        drawMemoryBar(phase1Alpha);
                        glPopMatrix();
                    }

                    // timer
                    if(Config.enableTimer && phase1Alpha > 0.0f && !Config.mimicModern) {
                        glPushMatrix();
                        setColorWithAlpha(Config.fontColor, phase1Alpha);
                        glTranslatef(320 - (float) Display.getWidth() / 2 + 4, 240 + (float) Display.getHeight() / 2 - textHeight2, 0);
                        glScalef(2, 2, 1);
                        glEnable(GL_TEXTURE_2D);
                        String renderString = getString();
                        fontRenderer.drawString(renderString, 0, 0, 0x000000);
                        glDisable(GL_TEXTURE_2D);
                        glPopMatrix();
                    }

                    // bars
                    boolean fadingOut = ModernSplash.fadeOutStart > 0;
                    if(first != null || (fadingOut && smoothProgressFirst > 0.0f))
                    {
                        glPushMatrix();
                        glTranslatef(320 - (float)barWidth / 2, 310, 0);
                        drawBar(first, phase1Alpha, smoothProgressFirst);
                        if(!Config.mimicModern && first != null) {
                            if(penult != null)
                            {
                                glTranslatef(0, barOffset, 0);
                                drawBar(penult, phase1Alpha, smoothProgressPenult);
                            }
                            if(last != null)
                            {
                                glTranslatef(0, barOffset, 0);
                                drawBar(last, phase1Alpha, smoothProgressLast);
                            }
                        }
                        glPopMatrix();
                    }

                    if (Config.forgeLogo && phase1Alpha > 0.0f && !Config.mimicModern) {

                        angle += 1;

                        // forge logo
                        setColorWithAlpha(0xFFFFFF, phase1Alpha);
                        float fw = (float) forgeTexture.getWidth() / 2;
                        float fh = (float) forgeTexture.getHeight() / 2;
                        if (Config.rotate) {
                            float sh = Math.max(fw, fh);
                            glTranslatef(320 + w / 2 - sh - Config.logoOffset, 240 + h / 2 - sh - Config.logoOffset, 0);
                            glRotatef(angle, 0, 0, 1);
                        } else {
                            glTranslatef(320 + w / 2 - fw - Config.logoOffset, 240 + h / 2 - fh - Config.logoOffset, 0);
                        }
                        int f = (angle / 5) % forgeTexture.getFrames();
                        glEnable(GL_TEXTURE_2D);
                        forgeTexture.bind();
                        glBegin(GL_QUADS);
                        forgeTexture.texCoord(f, 0, 0);
                        glVertex2f(-fw, -fh);
                        forgeTexture.texCoord(f, 0, 1);
                        glVertex2f(-fw, fh);
                        forgeTexture.texCoord(f, 1, 1);
                        glVertex2f(fw, fh);
                        forgeTexture.texCoord(f, 1, 0);
                        glVertex2f(fw, -fh);
                        glEnd();
                        glDisable(GL_TEXTURE_2D);
                    }

                    // We use mutex to indicate safely to the main thread that we're taking the display global lock
                    // So the main thread can skip processing messages while we're updating.
                    // There are system setups where this call can pause for a while, because the GL implementation
                    // is trying to impose a framerate or other thing is occurring. Without the mutex, the main
                    // thread would delay waiting for the same global display lock
                    mutex.acquireUninterruptibly();
                    long updateStart = System.nanoTime();
                    Display.update();
                    // As soon as we're done, we release the mutex. The other thread can now ping the processmessages
                    // call as often as it wants until we get get back here again
                    long dur = System.nanoTime() - updateStart;
                    if (framecount < TIMING_FRAME_COUNT) {
                        updateTiming += dur;
                    }
                    mutex.release();
                    if(pause)
                    {
                        clearGL();
                        setGL();
                    }
                    // Such a hack - if the time taken is greater than 10 milliseconds, we're gonna guess that we're on a
                    // system where vsync is forced through the swapBuffers call - so we have to force a sleep and let the
                    // loading thread have a turn - some badly designed mods access Keyboard and therefore GlobalLock.lock
                    // during splash screen, and mutex against the above Display.update call as a result.
                    // 4 milliseconds is a guess - but it should be enough to trigger in most circumstances. (Maybe if
                    // 240FPS is possible, this won't fire?)
                    if (framecount >= TIMING_FRAME_COUNT && updateTiming > TIMING_FRAME_THRESHOLD) {
                        if (!isDisplayVSyncForced)
                        {
                            isDisplayVSyncForced = true;
                            FMLLog.log.info("Using alternative sync timing : {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                        try { Thread.sleep(16); } catch (InterruptedException ignored) {}
                    } else
                    {
                        if (framecount ==TIMING_FRAME_COUNT) {
                            FMLLog.log.info("Using sync timing. {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                        Display.sync(100);
                    }
                }
                clearGL();
            }

            private String getString(){
                long startupTime = ManagementFactory.getRuntimeMXBean().getUptime();

                if(ModernSplash.doneTime > 0) startupTime = ModernSplash.doneTime;

                long minutes = (startupTime / 1000) / 60;
                long seconds = (startupTime / 1000) % 60;

                String str = "Startup: " + minutes + "m " + seconds + "s";

                if(MSLoadingPlugin.expectedTime > 0){
                    long ex_minutes = (MSLoadingPlugin.expectedTime / 1000) / 60;
                    long ex_seconds = (MSLoadingPlugin.expectedTime / 1000) % 60;

                    str += " / ~" + ex_minutes + "m " + ex_seconds + "s";
                }

                return str;
            }

            private void setColor(int color)
            {
                glColor3ub((byte)((color >> 16) & 0xFF), (byte)((color >> 8) & 0xFF), (byte)(color & 0xFF));
            }

            private void setColorWithAlpha(int color, float alpha)
            {
                glColor4f(
                    ((color >> 16) & 0xFF) / 255.0f,
                    ((color >> 8) & 0xFF) / 255.0f,
                    (color & 0xFF) / 255.0f,
                    alpha
                );
            }

            private void drawBox(int w, int h)
            {
                glBegin(GL_QUADS);
                glVertex2f(0, 0);
                glVertex2f(0, h);
                glVertex2f(w, h);
                glVertex2f(w, 0);
                glEnd();
            }

            private void drawBar(ProgressBar b, float alpha, float smoothProgress)
            {
                if (alpha <= 0.0f) return;
                if (!Config.mimicModern && b != null) {
                    String progress = "" + b.getStep() + "/" + b.getSteps();
                    glPushMatrix();
                    // title - message
                    setColorWithAlpha(Config.fontColor, alpha);
                    glScalef(2, 2, 1);
                    glEnable(GL_TEXTURE_2D);
                    fontRenderer.drawString(b.getTitle() + " " + progress + " - " + b.getMessage(), 0, 0, 0x000000);
                    glDisable(GL_TEXTURE_2D);
                    glPopMatrix();
                }
                // border
                glPushMatrix();
                glTranslatef(0, (!Config.mimicModern && b != null) ? textHeight2 : 0, 0);
                setColorWithAlpha(Config.barBorderColor, alpha);
                drawBox(barWidth, barHeight);
                // interior
                setColorWithAlpha(Config.barBackgroundColor, alpha);
                glTranslatef(2, 2, 0);
                drawBox(barWidth - 4, barHeight - 4);
                // slidy part
                setColorWithAlpha(Config.barColor, alpha);
                glTranslatef(2, 2, 0);
                drawBox((int) ((barWidth - 8) * smoothProgress), barHeight - 8);
                glPopMatrix();
            }

            private void drawMemoryBar(float alpha) {
                if (alpha <= 0.0f) return;
                int maxMemory = bytesToMb(Runtime.getRuntime().maxMemory());
                int totalMemory = bytesToMb(Runtime.getRuntime().totalMemory());
                int freeMemory = bytesToMb(Runtime.getRuntime().freeMemory());
                int usedMemory = totalMemory - freeMemory;
                float usedMemoryPercent = usedMemory / (float) maxMemory;
                String progress = getMemoryString(usedMemory) + " / " + getMemoryString(maxMemory);
                glPushMatrix();
                // title - message
                setColorWithAlpha(Config.fontColor, alpha);
                glScalef(2, 2, 1);
                glEnable(GL_TEXTURE_2D);
                fontRenderer.drawString("Memory Usage : " + progress, 0, 0, 0x000000);
                glDisable(GL_TEXTURE_2D);
                glPopMatrix();
                // border
                glPushMatrix();
                glTranslatef(0, textHeight2, 0);
                setColorWithAlpha(Config.barBorderColor, alpha);
                drawBox(barWidth, barHeight);
                // interior
                setColorWithAlpha(Config.barBackgroundColor, alpha);
                glTranslatef(2, 2, 0);
                drawBox(barWidth - 4, barHeight - 4);
                // slidy part

                long time = System.currentTimeMillis();
                if (usedMemoryPercent > memoryColorPercent || (time - memoryColorChangeTime > 1000))
                {
                    memoryColorChangeTime = time;
                    memoryColorPercent = usedMemoryPercent;
                }

                int memoryBarColor;
                if (memoryColorPercent < 0.75f)
                {
                    memoryBarColor = Config.memoryGoodColor;
                }
                else if (memoryColorPercent < 0.85f)
                {
                    memoryBarColor = Config.memoryWarnColor;
                }
                else
                {
                    memoryBarColor = Config.memoryLowColor;
                }
                if(Config.showTotalMemoryLine) {
                    setColorWithAlpha(Config.memoryLowColor, alpha);
                    glPushMatrix();
                    glTranslatef((float) ((barWidth - 8) * (totalMemory)) / (maxMemory) - 2, 2, 0);
                    drawBox(2, barHeight - 8);
                    glPopMatrix();
                }
                setColorWithAlpha(memoryBarColor, alpha);
                glTranslatef(2, 2, 0);
                drawBox((barWidth - 8) * (usedMemory) / (maxMemory), barHeight - 8);

                // progress text
                //String progress = getMemoryString(usedMemory) + " / " + getMemoryString(maxMemory);
                /*glTranslatef(((float)barWidth - 2) / 2 - fontRenderer.getStringWidth(progress), 2, 0);
                setColor(Config.fontColor);
                glScalef(2, 2, 1);
                glEnable(GL_TEXTURE_2D);
                fontRenderer.drawString(progress, 0, 0, 0x000000);*/
                glPopMatrix();
            }

            private String getMemoryString(int memory)
            {
                return StringUtils.leftPad(Integer.toString(memory), 4, ' ') + " MB";
            }

            private void setGL()
            {
                lock.lock();
                try
                {
                    Display.getDrawable().makeCurrent();
                }
                catch (LWJGLException e)
                {
                    FMLLog.log.error("Error setting GL context:", e);
                    throw new RuntimeException(e);
                }
                glClearColor((float)((Config.backgroundColor >> 16) & 0xFF) / 0xFF, (float)((Config.backgroundColor >> 8) & 0xFF) / 0xFF, (float)(Config.backgroundColor & 0xFF) / 0xFF, 1);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }

            private void clearGL()
            {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayWidth = Display.getWidth();
                mc.displayHeight = Display.getHeight();
                mc.resize(mc.displayWidth, mc.displayHeight);
                glClearColor(1, 1, 1, 1);
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LEQUAL);
                glEnable(GL_ALPHA_TEST);
                glAlphaFunc(GL_GREATER, .1f);
                try
                {
                    Display.getDrawable().releaseContext();
                }
                catch (LWJGLException e)
                {
                    FMLLog.log.error("Error releasing GL context:", e);
                    throw new RuntimeException(e);
                }
                finally
                {
                    lock.unlock();
                }
            }
        });
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                FMLLog.log.error("Splash thread Exception", e);
                threadError = e;
            }
        });
        thread.start();
        checkThreadState();
    }

    private static int max_texture_size = -1;
    public static int getMaxTextureSize()
    {
        if (max_texture_size != -1) return max_texture_size;
        max_texture_size = glGetInteger(GL_MAX_TEXTURE_SIZE);
        return max_texture_size;
    }

    private static void checkThreadState()
    {
        if(thread.getState() == Thread.State.TERMINATED || threadError != null)
        {
            throw new IllegalStateException("Splash thread", threadError);
        }
    }
    /**
     * Call before you need to explicitly modify GL context state during loading.
     * Resource loading doesn't usually require this call.
     * Call {@link #resume()} when you're done.
     * @deprecated not a stable API, will break, don't use this yet
     */
    @Deprecated
    public static void pause()
    {
        if(!Config.enabled) return;
        checkThreadState();
        pause = true;
        lock.lock();
        try
        {
            d.releaseContext();
            Display.getDrawable().makeCurrent();
        }
        catch (LWJGLException e)
        {
            FMLLog.log.error("Error setting GL context:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated not a stable API, will break, don't use this yet
     */
    @Deprecated
    public static void resume()
    {
        if(!Config.enabled) return;
        checkThreadState();
        pause = false;
        try
        {
            Display.getDrawable().releaseContext();
            d.makeCurrent();
        }
        catch (LWJGLException e)
        {
            FMLLog.log.error("Error releasing GL context:", e);
            throw new RuntimeException(e);
        }
        lock.unlock();
    }

    public static void finish()
    {
        if(!Config.enabled) return;
        try
        {
            checkThreadState();
            ModernSplash.fadeOutStart = System.nanoTime();
            thread.join(3000);
            if (!done) {
                FMLLog.log.warn("Splash fade-out timed out, forcing finish");
                done = true;
                thread.join(1000);
            }
            glFlush();
            d.releaseContext();
            Display.getDrawable().makeCurrent();
            fontTexture.delete();
            logoTexture.delete();
            forgeTexture.delete();
        }
        catch (Exception e)
        {
            FMLLog.log.error("Error finishing SplashProgress:", e);
            disableSplash(e);
        }
    }

    private static void disableSplash(Exception e)
    {
        if (Config.disableSplash())
        {
            throw new EnhancedRuntimeException(e)
            {
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    stream.println("SplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("We have automatically disabled the new Splash Screen in config/modernsplash.cfg.");
                    stream.println("Try reloading minecraft before reporting any errors.");
                }
            };
        }
        else
        {
            throw new EnhancedRuntimeException(e)
            {
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    stream.println("SplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("Please try disabling the new Splash Screen in config/modernsplash.cfg.");
                    stream.println("After doing so, try reloading minecraft before reporting any errors.");
                }
            };
        }
    }

    private static IResourcePack createResourcePack(File file)
    {
        if(file.isDirectory())
        {
            return new FolderResourcePack(file);
        }
        else
        {
            return new FileResourcePack(file);
        }
    }

    private static final IntBuffer buf = BufferUtils.createIntBuffer(4 * 1024 * 1024);

    @SuppressWarnings("unused")
    private static class Texture
    {
        private final ResourceLocation location;
        private final int name;
        private final int width;
        private final int height;
        private final int frames;
        private final int size;

        public Texture(ResourceLocation location, @Nullable ResourceLocation fallback)
        {
            this(location, fallback, true);
        }

        public Texture(ResourceLocation location, @Nullable ResourceLocation fallback, boolean allowRP)
        {
            InputStream s = null;
            try
            {
                this.location = location;
                s = open(location, fallback, allowRP);
                ImageInputStream stream = ImageIO.createImageInputStream(s);
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if(!readers.hasNext()) throw new IOException("No suitable reader found for image" + location);
                ImageReader reader = readers.next();
                reader.setInput(stream);
                int frames = reader.getNumImages(true);
                BufferedImage[] images = new BufferedImage[frames];
                for(int i = 0; i < frames; i++)
                {
                    images[i] = reader.read(i);
                }
                reader.dispose();
                width = images[0].getWidth();
                int height = images[0].getHeight();
                // Animation strip
                if (height > width && height % width == 0)
                {
                    frames = height / width;
                    BufferedImage original = images[0];
                    height = width;
                    images = new BufferedImage[frames];
                    for (int i = 0; i < frames; i++)
                    {
                        images[i] = original.getSubimage(0, i * height, width, height);
                    }
                }
                this.frames = frames;
                this.height = height;
                int size = 1;
                while((size / width) * (size / height) < frames) size *= 2;
                this.size = size;
                glEnable(GL_TEXTURE_2D);
                synchronized(CustomSplash.class)
                {
                    name = glGenTextures();
                    glBindTexture(GL_TEXTURE_2D, name);
                }
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)null);
                checkGLError("Texture creation");
                for(int i = 0; i * (size / width) < frames; i++)
                {
                    for(int j = 0; i * (size / width) + j < frames && j < size / width; j++)
                    {
                        buf.clear();
                        BufferedImage image = images[i * (size / width) + j];
                        for(int k = 0; k < height; k++)
                        {
                            for(int l = 0; l < width; l++)
                            {
                                buf.put(image.getRGB(l, k));
                            }
                        }
                        buf.position(0).limit(width * height);
                        glTexSubImage2D(GL_TEXTURE_2D, 0, j * width, i * height, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buf);
                        checkGLError("Texture uploading");
                    }
                }
                glBindTexture(GL_TEXTURE_2D, 0);
                glDisable(GL_TEXTURE_2D);
            }
            catch(IOException e)
            {
                FMLLog.log.error("Error reading texture from file: {}", location, e);
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtils.closeQuietly(s);
            }
        }

        public ResourceLocation getLocation()
        {
            return location;
        }

        public int getName()
        {
            return name;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public int getFrames()
        {
            return frames;
        }

        public int getSize()
        {
            return size;
        }

        public void bind()
        {
            glBindTexture(GL_TEXTURE_2D, name);
        }

        public void delete()
        {
            glDeleteTextures(name);
        }

        public float getU(int frame, float u)
        {
            return width * (frame % (size / width) + u) / size;
        }

        public float getV(int frame, float v)
        {
            return height * (frame / (size / width) + v) / size;
        }

        public void texCoord(int frame, float u, float v)
        {
            glTexCoord2f(getU(frame, u), getV(frame, v));
        }
    }

    private static class SplashFontRenderer extends FontRenderer
    {
        public SplashFontRenderer()
        {
            super(Minecraft.getMinecraft().gameSettings, fontTexture.getLocation(), null, false);
            super.onResourceManagerReload(null);
        }

        @Override
        protected void bindTexture(@Nonnull ResourceLocation location)
        {
            if(location != locationFontTexture) throw new IllegalArgumentException();
            fontTexture.bind();
        }

        @Nonnull
        @Override
        protected IResource getResource(@Nonnull ResourceLocation location) throws IOException
        {
            DefaultResourcePack pack = Minecraft.getMinecraft().defaultResourcePack;
            return new SimpleResource(pack.getPackName(), location, pack.getInputStream(location), null, null);
        }
    }

    public static void drawVanillaScreen(TextureManager renderEngine) throws LWJGLException
    {
        if(!Config.enabled)
        {
            Minecraft.getMinecraft().drawSplashScreen(renderEngine);
        }
    }

    public static void clearVanillaResources(TextureManager renderEngine, ResourceLocation mojangLogo)
    {
        if(!Config.enabled)
        {
            renderEngine.deleteTexture(mojangLogo);
        }
    }

    public static void checkGLError(String where)
    {
        int err = glGetError();
        if (err != 0)
        {
            throw new IllegalStateException(where + ": " + GLU.gluErrorString(err));
        }
    }

    private static InputStream open(ResourceLocation loc, @Nullable ResourceLocation fallback, boolean allowResourcePack) throws IOException
    {
        if (!allowResourcePack)
            return mcPack.getInputStream(loc);

        if(miscPack.resourceExists(loc))
        {
            return miscPack.getInputStream(loc);
        }
        else if(fmlPack.resourceExists(loc))
        {
            return fmlPack.getInputStream(loc);
        }
        else if(!mcPack.resourceExists(loc) && fallback != null)
        {
            return open(fallback, null, true);
        }
        return mcPack.getInputStream(loc);
    }

    private static int bytesToMb(long bytes)
    {
        return (int) (bytes / 1024L / 1024L);
    }
}