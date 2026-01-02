package com.github.dirify21.aml.client.model.block;

import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IIcon;
import com.github.dirify21.aml.client.ModelRegistryHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
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

    @Override
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format, @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        String cleanKey = getCleanName(texture.toString());
        try {
            Block block = ModelRegistryHandler.TEXTURE_TO_BLOCK.get(cleanKey);
            if (block instanceof IArchaicBlock archaic) {
                String d = getIconPath(archaic.aml$getIcon(0, 0));
                String u = getIconPath(archaic.aml$getIcon(1, 0));
                String n = getIconPath(archaic.aml$getIcon(2, 0));
                String s = getIconPath(archaic.aml$getIcon(3, 0));
                String w = getIconPath(archaic.aml$getIcon(4, 0));
                String e = getIconPath(archaic.aml$getIcon(5, 0));

                IModel cubeModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube"));

                ImmutableMap.Builder<String, String> textures = ImmutableMap.builder();
                textures.put("particle", u);
                textures.put("down", d);
                textures.put("up", u);
                textures.put("north", n);
                textures.put("south", s);
                textures.put("west", w);
                textures.put("east", e);

                return cubeModel.retexture(textures.build()).bake(state, format, bakedTextureGetter);
            }

            IModel fallback = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"));
            return fallback.retexture(ImmutableMap.of("all", normalizeForForge(texture.toString())))
                    .bake(state, format, bakedTextureGetter);

        } catch (Exception e) {
            e.printStackTrace();
            return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
        }
    }

    private String getCleanName(String path) {
        String res = path.toLowerCase();
        if (res.startsWith("aml_virtual:")) res = res.substring(12);
        res = res.replace("blocks/", "");
        return res;
    }

    private String normalizeForForge(String path) {
        String res = path.toLowerCase();
        if (res.startsWith("aml_virtual:")) res = res.substring(12);
        return res;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    private String getIconPath(IIcon icon) {
        if (icon != null && icon.sprite() != null) {
            return icon.sprite().getIconName();
        }
        return normalizeForForge(this.texture.toString());
    }
}