package com.github.dirify21.aml.interfaces;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public interface IArchaicBlock {
    Block func_149658_d(String name);

    String aml$getBlockTextureName();

    void func_149651_a(IIconRegister register);

    IIcon aml$getIcon(int side, int meta);
}