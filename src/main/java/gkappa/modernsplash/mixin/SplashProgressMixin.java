package gkappa.modernsplash.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;


@Mixin(value = SplashProgress.class, remap = false)
public class SplashProgressMixin {

    @Coerce
    FontRenderer fontRenderer;

    @Shadow
    private static boolean getBool(String name, boolean def)
    {
        return false;
    }

    @Shadow
    private static int getInt(String name, int def)
    {
        return 0;
    }

    @Shadow
    private static int getHex(String name, int def)
    {
        return 0;
    }
    @Redirect(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;getBool(Ljava/lang/String;Z)Z"))
    private static boolean redirectGetBool(String name, boolean def) {
        return getBool(name, def);
    }

    @Redirect(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;getInt(Ljava/lang/String;I)I"))
    private static int redirectGetInt(String name, int def) {
        return getInt(name, def);
    }

    @Redirect(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;getHex(Ljava/lang/String;I)I"))
    private static int redirectGetHex(String name, int def) {
        switch (name) {
            case "background":
            case "barBackground":
                return getHex(name, 0xEF323D);
            default:
                return getHex(name, 0xFFFFFF);
        }

    }

    /*
    @Redirect(method = "start", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;<init>(Ljava/lang/Runnable;)V"))
    private static void redirectThread(Thread instance, Runnable runnable) {
        return new Thread(new Runnable());
    }*/

}
