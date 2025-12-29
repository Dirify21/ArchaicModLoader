package com.github.dirify21.aml.client;

import com.gihtub.dirify21.aml.Reference;
import com.github.dirify21.aml.AMLMod;
import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IArchaicItem;
import com.github.dirify21.aml.client.model.ArchaicModelLoader;
import com.github.dirify21.aml.client.model.block.ArchaicStateMapper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class ModelRegistryHandler {

    private static boolean loaderRegistered = false;

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Block block : Block.REGISTRY) {
            if (block instanceof IArchaicBlock archaicBlock) {
                String textureName = archaicBlock.aml$getBlockTextureName();
                if (textureName != null && !textureName.isEmpty()) {
                    ResourceLocation textureLoc = fixTexturePath(textureName);
                    event.getMap().registerSprite(textureLoc);
                }
            }
        }
    }

    private static ResourceLocation fixTexturePath(String path) {
        if (path.contains(":")) {
            String[] parts = path.split(":", 2);
            String domain = parts[0];
            String name = parts[1];
            if (!name.startsWith("blocks/")) {
                name = "blocks/" + name;
            }
            return new ResourceLocation(domain, name);
        }
        return new ResourceLocation("minecraft", path.startsWith("blocks/") ? path : "blocks/" + path);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        System.out.println("[AML-MODEL] ModelRegistryEvent fired");
        if (!loaderRegistered) {
            ModelLoaderRegistry.registerLoader(new ArchaicModelLoader());
            loaderRegistered = true;
            AMLMod.LOGGER.info("Registering Archaic Models...");
            System.out.println("[AML-MODEL] ArchaicModelLoader registered!");
        }

        for (Item item : Item.REGISTRY) {
            if (item instanceof IArchaicItem archaicItem) {
                String textureName = archaicItem.aml$getTextureName();
                if (textureName != null && !textureName.isEmpty()) {
                    ModelResourceLocation mrl = getItemModelResourceLocation(textureName);
                    System.out.println("[AML-MODEL] Setting model for Item: " + item.getRegistryName() + " -> " + mrl);
                    ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
                }
            }
        }

        for (Block block : Block.REGISTRY) {
            if (block instanceof IArchaicBlock archaicBlock) {
                String textureName = archaicBlock.aml$getBlockTextureName();
                if (textureName != null && !textureName.isEmpty()) {
                    Item itemBlock = Item.getItemFromBlock(block);
                    ModelResourceLocation mrlInventory = getBlockModelResourceLocation(textureName);
                    ModelLoader.setCustomModelResourceLocation(itemBlock, 0, mrlInventory);
                    ModelResourceLocation mrlBlock = new ModelResourceLocation(
                            mrlInventory.getNamespace() + ":" + mrlInventory.getPath(),
                            "normal"
                    );
                    System.out.println("[AML-MODEL] Setting model for Block: " + block.getRegistryName() + " -> " + mrlInventory);
                    ModelLoader.setCustomStateMapper(block, new ArchaicStateMapper(mrlBlock));
                }
            }
        }
    }

    private static @NotNull ModelResourceLocation getItemModelResourceLocation(String textureName) {
        return getArchaicResourceLocation(textureName, "items", "inventory");
    }

    private static @NotNull ModelResourceLocation getBlockModelResourceLocation(String textureName) {
        return getArchaicResourceLocation(textureName, "blocks", "inventory");
    }

    private static @NotNull ModelResourceLocation getArchaicResourceLocation(String textureName, String typeDir, String variant) {
        ResourceLocation textureLoc;
        if (textureName.contains(":")) {
            String[] parts = textureName.split(":", 2);
            textureLoc = new ResourceLocation(parts[0], typeDir + "/" + parts[1]);
        } else {
            textureLoc = new ResourceLocation("minecraft", typeDir + "/" + textureName);
        }

        ResourceLocation virtualPath = new ResourceLocation("aml_virtual", textureLoc.toString());
        return new ModelResourceLocation(virtualPath, variant);
    }
}