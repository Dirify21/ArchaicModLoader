package com.github.dirify21.aml.mixin.mixins.minecraft;

import com.github.dirify21.aml.api.IArchaicBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockContainer.class)
public abstract class BlockContainerMixin extends Block {

    public BlockContainerMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        if (this instanceof IArchaicBlock) {
            String texture = ((IArchaicBlock) this).aml$getBlockTextureName();
            if (texture != null && !texture.isEmpty()) {
                return EnumBlockRenderType.MODEL;
            }
        }

        return EnumBlockRenderType.INVISIBLE;
    }
}