package com.github.dirify21.aml.client.model;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import org.jetbrains.annotations.NotNull;

public class ArchaicModelLoader implements ICustomModelLoader {

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals("aml_virtual");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        String fullPath = modelLocation.getPath();
        ResourceLocation textureRes = new ResourceLocation(fullPath);
        return new ArchaicItemModel(textureRes);
    }
}