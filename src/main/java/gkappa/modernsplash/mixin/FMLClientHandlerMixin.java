package gkappa.modernsplash.mixin;

import gkappa.modernsplash.CustomSplash;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Semaphore;

@Mixin(value = FMLClientHandler.class, remap = false)
public class FMLClientHandlerMixin {
    @Redirect(method = "beginMinecraftLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;start()V"))
    private void rdStart() {
        CustomSplash.start();
    }

    @Redirect(method = "haltGame", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;finish()V"))
    private void rdFinish() {
        CustomSplash.finish();
    }

    @Redirect(method = "finishMinecraftLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;finish()V"))
    private void rdFinish2() {
        CustomSplash.finish();
    }

    @Redirect(method = "processWindowMessages", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Semaphore;tryAcquire()Z"))
    private boolean rdTryAcquire(Semaphore instance) {
        return CustomSplash.mutex.tryAcquire();
    }

    @Redirect(method = "processWindowMessages", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Semaphore;release()V"))
    private void rdRelease(Semaphore instance) {
        CustomSplash.mutex.release();
    }

    @Inject(method = "isDisplayVSyncForced", at = @At("HEAD"), cancellable = true)
    private void injectVSync(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CustomSplash.isDisplayVSyncForced);
    }
}
