package com.github.dirify21.aml.client;

import com.gihtub.dirify21.aml.Reference;
import com.github.dirify21.aml.AMLMod;
import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IArchaicItem;
import com.github.dirify21.aml.api.IIcon;
import com.github.dirify21.aml.api.IIconRegister;
import com.github.dirify21.aml.client.model.ArchaicModelLoader;
import com.github.dirify21.aml.client.model.block.ArchaicStateMapper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class ModelRegistryHandler {

    public static final Map<String, Block> TEXTURE_TO_BLOCK = new HashMap<>();
    private static boolean loaderRegistered = false;

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
        if (!loaderRegistered) {
            ModelLoaderRegistry.registerLoader(new ArchaicModelLoader());
            loaderRegistered = true;
            AMLMod.LOGGER.info("Registering Models...");
        }

        for (Item item : Item.REGISTRY) {
            if (item instanceof IArchaicItem archaicItem) {
                String textureName = archaicItem.aml$getTextureName();
                if (textureName != null && !textureName.isEmpty()) {
                    ModelResourceLocation mrl = getItemModelResourceLocation(textureName);
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

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TEXTURE_TO_BLOCK.clear();
        IIconRegister customRegister = name -> {
            ResourceLocation loc = fixTexturePath(name);
            return new ArchaicIcon(event.getMap().registerSprite(loc));
        };

        for (Block block : Block.REGISTRY) {
            if (block instanceof IArchaicBlock archaic) {
                String texName = archaic.aml$getBlockTextureName();
                if (texName != null) {
                    String key = texName.toLowerCase().replace("blocks/", "");
                    TEXTURE_TO_BLOCK.put(key, block);
                }
                archaic.aml$registerBlockIcons(customRegister);
            }
        }
    }

    private record ArchaicIcon(TextureAtlasSprite sprite) implements IIcon {
        @Override
        public String getIconName() {
            return sprite.getIconName();
        }

        @Override
        public float getMinU() {
            return sprite.getMinU();
        }

        @Override
        public float getMaxU() {
            return sprite.getMaxU();
        }

        @Override
        public float getMinV() {
            return sprite.getMinV();
        }

        @Override
        public float getMaxV() {
            return sprite.getMaxV();
        }
    }

}

