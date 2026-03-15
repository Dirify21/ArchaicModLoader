package com.github.dirify21.aml.mixin;

import fr.catcore.cursedmixinextensions.annotations.Public;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxisAlignedBB.class)
@SuppressWarnings("unused")
public class AxisAlignedBBMixin {
    @Public
    private static AxisAlignedBB func_72330_a(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }
}
