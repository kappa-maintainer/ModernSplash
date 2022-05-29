package gkappa.modernsplash.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(TextureUtil.class)
public class TextureUtilMixin {

    @Shadow
    static void bindTexture(int p_94277_0_) {}

    @Shadow
    public static void deleteTexture(int textureId) {}

    @Inject(method = "allocateTextureImpl", at = @At("HEAD"), cancellable = true)
    private static void injectTextureImpl(int glTextureId, int mipmapLevels, int width, int height, CallbackInfo ci) {
        synchronized (gkappa.modernsplash.CustomSplash.class)
        {
            deleteTexture(glTextureId);
            bindTexture(glTextureId);
        }
        if (mipmapLevels >= 0)
        {
            GlStateManager.glTexParameteri(3553, 33085, mipmapLevels);
            GlStateManager.glTexParameteri(3553, 33082, 0);
            GlStateManager.glTexParameteri(3553, 33083, mipmapLevels);
            GlStateManager.glTexParameterf(3553, 34049, 0.0F);
        }

        for (int i = 0; i <= mipmapLevels; ++i)
        {
            GlStateManager.glTexImage2D(3553, i, 6408, width >> i, height >> i, 0, 32993, 33639, (IntBuffer)null);
        }

        ci.cancel();
    }
}
