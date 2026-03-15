package com.github.dirify21.aml.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TextureAtlasSprite.class)
@SuppressWarnings("unused")
public abstract class TextureAtlasSpriteMixin implements IIcon {
}
