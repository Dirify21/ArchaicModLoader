package com.github.dirify21.aml.common;

import com.github.dirify21.aml.util.RegistryHolder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : RegistryHolder.BLOCKS_TO_REGISTER) {
            event.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Item item : RegistryHolder.ITEMS_TO_REGISTER) {
            event.getRegistry().register(item);
        }

        for (Block block : RegistryHolder.BLOCKS_TO_REGISTER) {
            ResourceLocation name = block.getRegistryName();

            if (name != null) {
                ItemBlock ib = new ItemBlock(block);
                ib.setRegistryName(name);

                event.getRegistry().register(ib);
            }
        }

        RegistryHolder.BLOCKS_TO_REGISTER.clear();
        RegistryHolder.ITEMS_TO_REGISTER.clear();
    }
}
