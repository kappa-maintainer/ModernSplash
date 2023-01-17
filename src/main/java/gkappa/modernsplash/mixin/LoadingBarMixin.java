package gkappa.modernsplash.mixin;


import com.mumfrey.liteloader.client.gui.startup.LoadingBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mumfrey.liteloader.launch.LiteLoaderTweaker.loadingBarEnabled;

@Mixin(value = LoadingBar.class, remap = false)
public class LoadingBarMixin {
    @Inject(method = "render()V", at = @At("HEAD"), cancellable = true)
    private void cancelRender(CallbackInfo ci) {
        if(!loadingBarEnabled())ci.cancel();
    }

    @Inject(method = "render(D)V", at = @At("HEAD"), cancellable = true)
    private void cancelRender2(CallbackInfo ci) {
        if(!loadingBarEnabled())ci.cancel();
    }
}
