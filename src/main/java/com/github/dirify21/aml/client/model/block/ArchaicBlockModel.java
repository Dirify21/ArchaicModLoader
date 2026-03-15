package com.github.dirify21.aml.client.model.block;

import com.github.dirify21.aml.client.ModelRegistryHandler;
import com.github.dirify21.aml.interfaces.IArchaicBlock;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record ArchaicBlockModel(ResourceLocation texture) implements IModel {

    private static final ResourceLocation CUBE_MODEL = new ResourceLocation("minecraft:block/cube");

    @Override
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format, @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        try {
            Block block = ModelRegistryHandler.TEXTURE_TO_BLOCK.get(getCleanName(texture.toString()));
            return buildModel(((IArchaicBlock) block), state, format, bakedTextureGetter);
        } catch (Exception e) {
            return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
        }
    }

    private IBakedModel buildModel(IArchaicBlock archaic, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> getter) throws Exception {
        Map<String, String> textures = new HashMap<>();
        String u = getIconPath(archaic.aml$getIcon(1, 0));
        textures.put("particle", u);
        textures.put("down", getIconPath(archaic.aml$getIcon(0, 0)));
        textures.put("up", u);
        textures.put("north", getIconPath(archaic.aml$getIcon(2, 0)));
        textures.put("south", getIconPath(archaic.aml$getIcon(3, 0)));
        textures.put("west", getIconPath(archaic.aml$getIcon(4, 0)));
        textures.put("east", getIconPath(archaic.aml$getIcon(5, 0)));

        return ModelLoaderRegistry.getModel(CUBE_MODEL)
                .retexture(ImmutableMap.copyOf(textures))
                .bake(state, format, getter);
    }

    private String getCleanName(String path) {
        return normalizeForForge(path).replace("blocks/", "");
    }

    private String normalizeForForge(String path) {
        return path.toLowerCase().replaceFirst("^aml_virtual:", "");
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    private String getIconPath(IIcon icon) {
        return Optional.ofNullable(icon)
                .map(IIcon::getIconName)
                .orElseGet(() -> normalizeForForge(this.texture.toString()));
    }
}