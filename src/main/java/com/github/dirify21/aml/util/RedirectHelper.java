package com.github.dirify21.aml.util;

import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IArchaicItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class RedirectHelper {
    public static Item setTextureNameRedirect(Item item, String name) {
        if (item instanceof IArchaicItem archaic) {
            return archaic.aml$setTextureName(name);
        }
        return item;
    }

    public static Block setBlockTextureNameRedirect(Block block, String name) {
        if (block instanceof IArchaicBlock archaic) {
            return archaic.aml$setBlockTextureName(name);
        }
        return block;
    }
}