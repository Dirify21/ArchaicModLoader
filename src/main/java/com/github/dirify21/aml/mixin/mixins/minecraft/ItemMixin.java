package com.github.dirify21.aml.mixin.mixins.minecraft;

import com.github.dirify21.aml.util.IArchaicItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class ItemMixin implements IArchaicItem {

    @Unique
    private String aml$textureName;

    @Unique
    public Item aml$setTextureName(String name) {
        this.aml$textureName = name;
        Item instance = (Item) (Object) this;

        if (instance.getRegistryName() == null && name != null && !name.isEmpty()) {
            String domain = "minecraft";
            String path = name;
            if (name.contains(":")) {
                String[] parts = name.split(":", 2);
                domain = parts[0];
                path = parts[1];
            }
            instance.setRegistryName(new ResourceLocation(domain, path));
        }
        return instance;
    }

    @Override
    public String aml$getTextureName() {
        return this.aml$textureName;
    }
}