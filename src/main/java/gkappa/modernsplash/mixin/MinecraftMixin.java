package gkappa.modernsplash.mixin;

import gkappa.modernsplash.CustomSplash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    //@Shadow public TextureManager renderEngine;

    @Redirect(method = "startGame", at = @At(value = "INVOKE", remap = false, target = "Lcpw/mods/fml/client/SplashProgress;drawVanillaScreen()V"))
    private void rdDrawVanillaScreen() throws LWJGLException {
        CustomSplash.drawVanillaScreen();
    }

    @Redirect(method = "startGame", at = @At(value = "INVOKE", remap = false, target = "Lcpw/mods/fml/client/SplashProgress;clearVanillaResources(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/util/ResourceLocation;)V"))
    private void rdClear(TextureManager renderEngine, ResourceLocation mojangLogo) {
        CustomSplash.clearVanillaResources(renderEngine, mojangLogo);
    }

}
