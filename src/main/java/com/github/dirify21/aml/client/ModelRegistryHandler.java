package com.github.dirify21.aml.client;

import com.gihtub.dirify21.aml.Reference;
import com.github.dirify21.aml.AMLMod;
import com.github.dirify21.aml.client.model.ArchaicModelLoader;
import com.github.dirify21.aml.util.IArchaicItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
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
    public static void onModelRegister(ModelRegistryEvent event) {
        if (!loaderRegistered) {
            ModelLoaderRegistry.registerLoader(new ArchaicModelLoader());
            loaderRegistered = true;
            AMLMod.LOGGER.info("Registering Archaic Models...");
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
    }

    private static @NotNull ModelResourceLocation getItemModelResourceLocation(String textureName) {
        return getArchaicResourceLocation(textureName, "items", "inventory");
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