package com.github.dirify21.aml.mixin.mixins.minecraft;

import com.github.dirify21.aml.api.IArchaicBlock;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin implements IArchaicBlock {

    @Unique
    private String aml$textureName;

    @Unique
    public Block aml$setBlockTextureName(String name) {
        this.aml$textureName = name;
        Block instance = (Block) (Object) this;

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
    public String aml$getBlockTextureName() {
        return this.aml$textureName;
    }
}