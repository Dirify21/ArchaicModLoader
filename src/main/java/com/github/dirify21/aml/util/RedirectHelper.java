package com.github.dirify21.aml.util;

import net.minecraft.item.Item;

public class RedirectHelper {
    public static Item setTextureNameRedirect(Item item, String name) {
        if (item instanceof IArchaicItem archaic) {
            return archaic.aml$setTextureName(name);
        }
        return item;
    }
}