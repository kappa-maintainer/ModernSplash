package gkappa.modernsplash.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraftforge.fml.client.SplashProgress.fontColor;
import static org.lwjgl.opengl.GL11.*;


@Mixin(targets = "net/minecraftforge/fml/client/SplashProgress$2", remap = false)
public class SplashProgress2Mixin {
    @Shadow
    private void drawBox(int w, int h) {}



    @Shadow
    private void setColor(int color) {}
    @Final
    @Shadow
    private final int textHeight2 = 20;

    @Dynamic
    @Inject(method = "drawBar", at = @At(value = "HEAD"), cancellable = true)
    private void redirectDrawBox(ProgressManager.ProgressBar b, CallbackInfo ci) {
        glPushMatrix();
        // title - message
        setColor(fontColor);
        glScalef(2, 2, 1);
        glEnable(GL_TEXTURE_2D);
        fontRenderer.drawString(b.getTitle() + " - " + b.getMessage(), 0, 0, 0x000000);
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
        // border
        glPushMatrix();
        glTranslatef(0, textHeight2, 0);
        setColor(barBorderColor);
        drawBox(barWidth, barHeight);
        // interior
        setColor(barBackgroundColor);
        glTranslatef(1, 1, 0);
        drawBox(barWidth - 2, barHeight - 2);
        // slidy part
        setColor(barColor);
        drawBox((barWidth - 2) * (b.getStep() + 1) / (b.getSteps() + 1), barHeight - 2); // Step can sometimes be 0.
        // progress text
        String progress = "" + b.getStep() + "/" + b.getSteps();
        glTranslatef(((float)barWidth - 2) / 2 - fontRenderer.getStringWidth(progress), 2, 0);
        setColor(fontColor);
        glScalef(2, 2, 1);
        glEnable(GL_TEXTURE_2D);
        fontRenderer.drawString(progress, 0, 0, 0x000000);
        ci.cancel();
    }
}
