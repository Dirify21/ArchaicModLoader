package com.github.dirify21.aml.mixin.forge;

import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeRegistry.class)
public interface IForgeRegistryAccessor {

    @Accessor("isFrozen")
    boolean getFrozen();

    @Accessor("isFrozen")
    void setFrozen(boolean frozen);
}