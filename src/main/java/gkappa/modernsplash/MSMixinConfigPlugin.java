package gkappa.modernsplash;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.Config;

import java.util.List;
import java.util.Set;

public class MSMixinConfigPlugin implements IMixinConfigPlugin {
    private static Config config;
    @Override
    public void onLoad(String s) {

    }

    @Override
    public void injectConfig(Config config) {
        MSMixinConfigPlugin.config = config;
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String s, String s1) {
        if (s1.equals("gkappa.modernsplash.mixin.FontRendererHookMixin")) {
            boolean sfLoaded = true;
            try {
                Class.forName("bre.smoothfont.mod_SmoothFont");
            } catch (Throwable ignored) {
                sfLoaded = false;
            }
            return sfLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
