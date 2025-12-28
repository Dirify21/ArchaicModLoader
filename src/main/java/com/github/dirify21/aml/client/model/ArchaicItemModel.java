package com.github.dirify21.aml.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public record ArchaicItemModel(ResourceLocation textureLocation) implements IModel {

    @Override
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format, @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        ItemLayerModel layerModel = new ItemLayerModel(ImmutableList.of(textureLocation));
        IModelState itemState = getState();
        IBakedModel bakedModel = layerModel.bake(itemState, format, bakedTextureGetter);
        return new PerspectiveMapWrapper(bakedModel, itemState);
    }

    private IModelState getState() {
        Map<TransformType, TRSRTransformation> tMap = Maps.newEnumMap(TransformType.class);

        tMap.put(TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(0, 3, 1, 0, 0, 0, 0.55f));
        tMap.put(TransformType.THIRD_PERSON_LEFT_HAND, getTransform(0, 3, 1, 0, 0, 0, 0.55f));

        tMap.put(TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
        tMap.put(TransformType.FIRST_PERSON_LEFT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));

        tMap.put(TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.5f));

        tMap.put(TransformType.FIXED, getTransform(0, 0, 0, 0, 180, 0, 1.0f));

        tMap.put(TransformType.GUI, getTransform(0, 0, 0, 0, 0, 0, 1.0f));
        return new SimpleModelState(ImmutableMap.copyOf(tMap));
    }

    private TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                new Vector3f(tx / 16f, ty / 16f, tz / 16f),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
                new Vector3f(s, s, s),
                null
        ));
    }

    @Override
    public IModelState getDefaultState() {
        return getState();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of(textureLocation);
    }
}