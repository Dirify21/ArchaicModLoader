package com.github.dirify21.aml.client;

import com.gihtub.dirify21.aml.Reference;
import com.github.dirify21.aml.client.model.ArchaicModelLoader;
import com.github.dirify21.aml.client.model.block.ArchaicStateMapper;
import com.github.dirify21.aml.interfaces.IArchaicBlock;
import com.github.dirify21.aml.interfaces.IArchaicItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
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
        String[] parts = path.split(":", 2);
        String domain = parts.length > 1 ? parts[0] : "minecraft";
        String name = parts.length > 1 ? parts[1] : parts[0];

        return new ResourceLocation(domain, name.startsWith("blocks/") ? name : "blocks/" + name);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        if (!loaderRegistered) {
            ModelLoaderRegistry.registerLoader(new ArchaicModelLoader());
            loaderRegistered = true;
        }

        Item.REGISTRY.forEach(item -> {
            String textureName = ((IArchaicItem) item).aml$getTextureName();
            if (textureName != null && !textureName.isEmpty()) {
                ModelLoader.setCustomModelResourceLocation(item, 0, getItemModelResourceLocation(textureName));
            }
        });

        Block.REGISTRY.forEach(block -> {
            String textureName = ((IArchaicBlock) block).aml$getBlockTextureName();
            if (textureName != null && !textureName.isEmpty()) {
                Item itemBlock = Item.getItemFromBlock(block);
                ModelResourceLocation mrlInventory = getBlockModelResourceLocation(textureName);
                ModelLoader.setCustomModelResourceLocation(itemBlock, 0, mrlInventory);
                ModelLoader.setCustomStateMapper(block, new ArchaicStateMapper(new ModelResourceLocation(mrlInventory.toString(), "normal")));
            }
        });
    }

    private static @NotNull ModelResourceLocation getItemModelResourceLocation(String textureName) {
        return getArchaicResourceLocation(textureName, "items", "inventory");
    }

    private static @NotNull ModelResourceLocation getBlockModelResourceLocation(String textureName) {
        return getArchaicResourceLocation(textureName, "blocks", "inventory");
    }

    private static @NotNull ModelResourceLocation getArchaicResourceLocation(String textureName, String typeDir, String variant) {
        String[] parts = textureName.split(":", 2);
        ResourceLocation textureLoc = (parts.length > 1)
                ? new ResourceLocation(parts[0], typeDir + "/" + parts[1])
                : new ResourceLocation("minecraft", typeDir + "/" + textureName);

        return new ModelResourceLocation(new ResourceLocation("aml_virtual", textureLoc.toString()), variant);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TEXTURE_TO_BLOCK.clear();

        Block.REGISTRY.forEach(block -> {
            String texName = ((IArchaicBlock) block).aml$getBlockTextureName();
            if (texName != null) TEXTURE_TO_BLOCK.put(texName.toLowerCase().replace("blocks/", ""), block);
            ((IArchaicBlock) block).func_149651_a(name -> new ArchaicIcon(event.getMap().registerSprite(fixTexturePath(name))));
        });
    }

    private record ArchaicIcon(TextureAtlasSprite sprite) implements IIcon {
        @Override
        public String getIconName() {
            return sprite.getIconName();
        }

        @Override
        public int getIconWidth() {
            return sprite.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return sprite.getIconHeight();
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
        public float getInterpolatedU(double u) {
            return sprite.getInterpolatedU(u);
        }

        @Override
        public float getMinV() {
            return sprite.getMinV();
        }

        @Override
        public float getMaxV() {
            return sprite.getMaxV();
        }

        @Override
        public float getInterpolatedV(double v) {
            return sprite.getInterpolatedV(v);
        }
    }
}

