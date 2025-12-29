package com.github.dirify21.aml.client.model.block;

import com.github.dirify21.aml.AMLMod;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record ArchaicBlockModel(ResourceLocation texture) implements IModel {

    private static @NotNull String getPath(String rawPath) {
        String cleanPath = rawPath;
        if (cleanPath.startsWith("aml_virtual:")) {
            cleanPath = cleanPath.substring("aml_virtual:".length());
            if (cleanPath.startsWith("minecraft:")) {
                cleanPath = cleanPath.substring("minecraft:".length());
            }
        }
        return cleanPath;
    }

    @Override
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format, @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        try {
            String rawPath = texture.toString();
            String cleanPath = getPath(rawPath);

            ResourceLocation checkRes = new ResourceLocation(cleanPath);
            bakedTextureGetter.apply(checkRes);

            IModel cubeModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"));

            IModel retexturedModel = cubeModel.retexture(ImmutableMap.of("all", cleanPath));

            return retexturedModel.bake(state, format, bakedTextureGetter);

        } catch (Exception e) {
            AMLMod.LOGGER.error("Failed to bake archaic block model", e);
            return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
        }
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}