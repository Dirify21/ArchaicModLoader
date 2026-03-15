package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIcon {
    /**
     * Returns the width of the icon, in pixels.
     */
    @SideOnly(Side.CLIENT)
    int getIconWidth();

    default int func_94211_a() {
        return getIconWidth();
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    @SideOnly(Side.CLIENT)
    int getIconHeight();

    default int func_94216_b() {
        return getIconHeight();
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    @SideOnly(Side.CLIENT)
    float getMinU();

    default float func_94209_e() {
        return getMinU();
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    @SideOnly(Side.CLIENT)
    float getMaxU();

    default float func_94212_f() {
        return getMaxU();
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    @SideOnly(Side.CLIENT)
    float getInterpolatedU(double u);

    default float func_94214_a(double u) {
        return getInterpolatedU(u);
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    @SideOnly(Side.CLIENT)
    float getMinV();

    default float func_94206_g() {
        return getMinV();
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    @SideOnly(Side.CLIENT)
    float getMaxV();

    default float func_94210_h() {
        return getMaxV();
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    @SideOnly(Side.CLIENT)
    float getInterpolatedV(double v);

    default float func_94207_b(double v) {
        return getInterpolatedV(v);
    }

    @SideOnly(Side.CLIENT)
    String getIconName();

    default String func_94215_i() {
        return getIconName();
    }
}
