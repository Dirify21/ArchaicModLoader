package com.github.dirify21.aml.asm;

import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

public class AMLLoadingPlugin implements IFMLLoadingPlugin {

    public static boolean isReadyToTransform;

    public AMLLoadingPlugin() {
        AMLClassLoader.init();
        isReadyToTransform = true;
    }

    @Override
    public @Nullable String[] getASMTransformerClass() {
        return new String[]{
                "com.github.dirify21.aml.asm.transformer.AMLTransformer"
        };
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}