package com.github.dirify21.aml.mixin.forge;

import fr.catcore.cursedmixinextensions.annotations.Public;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = GameRegistry.class)
@SuppressWarnings("unused")
public class GameRegistryMixin {
    @Public
    private static void registerItem(Item item, String name) {
        String modId = Loader.instance().activeModContainer().getModId();
        ResourceLocation rl = new ResourceLocation(modId, name);

        if (item.getRegistryName() == null) {
            item.setRegistryName(rl);
        }

        ForgeRegistry<Item> itemRegistry = (ForgeRegistry<Item>) ForgeRegistries.ITEMS;
        IForgeRegistryAccessor accessItem = (IForgeRegistryAccessor) itemRegistry;

        boolean wasItemFrozen = accessItem.getFrozen();
        accessItem.setFrozen(false);
        itemRegistry.register(item);
        accessItem.setFrozen(wasItemFrozen);
    }

    @Public
    private static Block registerBlock(Block block, String name) {
        String modId = Loader.instance().activeModContainer().getModId();
        ResourceLocation rl = new ResourceLocation(modId, name);
        block.setRegistryName(rl);

        ForgeRegistry<Block> registry = (ForgeRegistry<Block>) ForgeRegistries.BLOCKS;
        IForgeRegistryAccessor mixinRegistry = (IForgeRegistryAccessor) registry;

        boolean wasFrozen = mixinRegistry.getFrozen();
        mixinRegistry.setFrozen(false);

        registry.register(block);

        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(rl);
        ((IForgeRegistryAccessor) ForgeRegistries.ITEMS).setFrozen(false);
        ForgeRegistries.ITEMS.register(itemBlock);
        ((IForgeRegistryAccessor) ForgeRegistries.ITEMS).setFrozen(wasFrozen);

        mixinRegistry.setFrozen(wasFrozen);

        return block;
    }

    @Public
    private static void addRecipe(ItemStack output, Object[] params) {
        if (output == null || output.isEmpty()) return;
        ModContainer mc = Loader.instance().activeModContainer();
        ResourceLocation recipeName = new ResourceLocation(mc.getModId(), "recipe_" + System.nanoTime());
        GameRegistry.addShapedRecipe(recipeName, null, output, params);
    }


    /**
     * @author Dirify21
     * @reason Fix "Potentially Dangerous alternative prefix `minecraft` for..."
     */
    @Deprecated
    @Overwrite
    public static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String key) {
        String modId = Loader.instance().activeModContainer().getModId();
        if (new ResourceLocation(key).toString().contains(modId + ":")) {
            GameData.checkPrefix((new ResourceLocation(key)).toString(), true);
            TileEntity.register(key, tileEntityClass);
        } else {
            GameData.checkPrefix((new ResourceLocation(modId, key)).toString(), true);
            TileEntity.register(modId + ":" + key, tileEntityClass);
        }
    }

    /**
     * @author Dirify21
     * @reason Fix "Potentially Dangerous alternative prefix `minecraft` for..."
     */
    @Overwrite
    public static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, ResourceLocation key) {
        String modId = Loader.instance().activeModContainer().getModId();
        if (key.toString().contains(modId + ":")) {
            GameData.checkPrefix(key.toString(), true);
            TileEntity.register(key.toString(), tileEntityClass);
        } else {
            GameData.checkPrefix(key.toString(), true);
            TileEntity.register(modId + ":" + key, tileEntityClass);
        }
    }
}
