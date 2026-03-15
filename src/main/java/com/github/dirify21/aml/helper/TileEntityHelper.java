package com.github.dirify21.aml.helper;

import net.minecraft.tileentity.TileEntity;

@SuppressWarnings("unused")
public final class TileEntityHelper {
    public static void makeTransparent(Object tile) {
        ((TileEntity) tile).getBlockType().setLightOpacity(0);
    }
}
