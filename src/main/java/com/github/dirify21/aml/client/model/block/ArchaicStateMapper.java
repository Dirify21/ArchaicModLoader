package com.github.dirify21.aml.client.model.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import org.jetbrains.annotations.NotNull;

public class ArchaicStateMapper extends StateMapperBase {
    private final ModelResourceLocation location;

    public ArchaicStateMapper(ModelResourceLocation location) {
        this.location = location;
    }

    @Override
    protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
        return location;
    }
}