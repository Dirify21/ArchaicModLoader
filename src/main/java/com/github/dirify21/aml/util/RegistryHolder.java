package com.github.dirify21.aml.util;

import com.github.dirify21.aml.AMLMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.ArrayList;
import java.util.List;

public class RegistryHolder {
    public static final List<Block> BLOCKS_TO_REGISTER = new ArrayList<>();
    public static final List<Item> ITEMS_TO_REGISTER = new ArrayList<>();

    public static void queueBlock(Block block, String name) {
        String modId = inferModId();

        if (block.getRegistryName() == null) {
            block.setRegistryName(new ResourceLocation(modId, name));
        }

        if (block.getRegistryName() == null) {
            AMLMod.LOGGER.error("CRITICAL: Block '{}' failed to get a RegistryName!", name);
        } else {
            BLOCKS_TO_REGISTER.add(block);
        }
    }

    public static void queueItem(Item item, String name) {
        String modId = inferModId();

        if (item.getRegistryName() == null) {
            item.setRegistryName(new ResourceLocation(modId, name));
        }

        if (item.getRegistryName() == null) {
            AMLMod.LOGGER.error("CRITICAL: Item '{}' failed to get a RegistryName!", name);
        } else {
            ITEMS_TO_REGISTER.add(item);
        }
    }

    private static String inferModId() {
        ModContainer mc = Loader.instance().activeModContainer();
        return (mc != null) ? mc.getModId() : "minecraft";
    }

}