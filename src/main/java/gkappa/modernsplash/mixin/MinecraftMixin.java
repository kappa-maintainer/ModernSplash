package gkappa.modernsplash.mixin;

import gkappa.modernsplash.CustomSplash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;drawVanillaScreen(Lnet/minecraft/client/renderer/texture/TextureManager;)V"))
    private void rdDrawVanillaScreen(TextureManager renderEngine) throws LWJGLException {
        CustomSplash.drawVanillaScreen(renderEngine);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;pause()V"))
    private void rdPause() {
        CustomSplash.pause();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;resume()V"))
    private void rdResume() {
        CustomSplash.resume();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;clearVanillaResources(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/util/ResourceLocation;)V"))
    private void rdClearVanillaResource(TextureManager renderEngine, ResourceLocation mojangLogo) {
        CustomSplash.clearVanillaResources(renderEngine, mojangLogo);
    }

    @Redirect(method = "getGLMaximumTextureSize", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;getMaxTextureSize()I"))
    private static int rdGetMaxTextureSize() {
        return CustomSplash.getMaxTextureSize();
    }
}
