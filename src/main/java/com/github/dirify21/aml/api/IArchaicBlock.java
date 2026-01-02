package com.github.dirify21.aml.api;

import net.minecraft.block.Block;

public interface IArchaicBlock {
    Block aml$setBlockTextureName(String name);

    String aml$getBlockTextureName();

    void aml$registerBlockIcons(IIconRegister register);

    IIcon aml$getIcon(int side, int meta);
}