package com.github.dirify21.aml.api;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface IIcon {
    String getIconName();

    float getMinU();

    float getMaxU();

    float getMinV();

    float getMaxV();

    TextureAtlasSprite sprite();

    default String func_94215_i() {
        return getIconName();
    }

    default float func_94209_e() {
        return getMinU();
    }

    default float func_94212_f() {
        return getMaxU();
    }

    default float func_94206_g() {
        return getMinV();
    }

    default float func_94210_h() {
        return getMaxV();
    }
}