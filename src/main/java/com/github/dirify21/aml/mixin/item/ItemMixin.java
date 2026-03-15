package com.github.dirify21.aml.mixin.item;

import com.github.dirify21.aml.interfaces.IArchaicItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements IArchaicItem {

    private String aml$textureName;

    public Item setTextureName(String name) {
        this.aml$textureName = name;
        return (Item) (Object) this;
    }

    @Override
    public String aml$getTextureName() {
        return this.aml$textureName;
    }
}