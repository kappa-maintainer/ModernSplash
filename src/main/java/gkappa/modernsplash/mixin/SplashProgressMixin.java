package gkappa.modernsplash.mixin;

import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;



@Mixin(value = SplashProgress.class, remap = false)
public class SplashProgressMixin {

    @Shadow
    private static boolean rotate;
    @Shadow
    private static int logoOffset;
    @Shadow
    private static int backgroundColor;
    @Shadow
    private static int fontColor;
    @Shadow
    private static int barBorderColor;
    @Shadow
    private static int barColor;
    @Shadow
    private static int barBackgroundColor;
    @Shadow
    private static boolean showMemory;
    @Shadow
    private static int memoryGoodColor;
    @Shadow
    private static int memoryWarnColor;
    @Shadow
    private static int memoryLowColor;

    @Shadow
    private static boolean getBool(String name, boolean def) {return false;}
    @Shadow
    private static int getInt(String name, int def) {return 0;}
    @Shadow
    private static int getHex(String name, int def) {return 0;}

    @Inject(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ResourceLocation;<init>(Ljava/lang/String;)V", ordinal = 0))
    private static void handleColor(CallbackInfo ci) {

        rotate =             getBool("rotate",       false);
        showMemory =         getBool("showMemory",   true);
        logoOffset =         getInt("logoOffset",    0);
        backgroundColor =    getHex("background",    0xEF323D);
        fontColor =          getHex("font",          0xFFFFFF);
        barBorderColor =     getHex("barBorder",     0xFFFFFF);
        barColor =           getHex("bar",           0xFFFFFF);
        barBackgroundColor = getHex("barBackground", 0xFFFFFF);
        memoryGoodColor =    getHex("memoryGood",    0xFFFFFF);
        memoryWarnColor =    getHex("memoryWarn",    0xFFFFFF);
        memoryLowColor =     getHex("memoryLow",     0xFFFFFF);
    }
}
