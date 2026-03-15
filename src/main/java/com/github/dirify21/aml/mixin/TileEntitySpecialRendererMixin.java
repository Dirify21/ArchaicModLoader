package com.github.dirify21.aml.mixin;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySpecialRenderer.class)
@SuppressWarnings("unused")
public abstract class TileEntitySpecialRendererMixin<T extends TileEntity> {

    @Inject(method = "render", at = @At("HEAD"))
    public void renderInit(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        this.func_147500_a(te, x, y, z, partialTicks);
    }

    public abstract void func_147500_a(T tileEntity, double x, double y, double z, float partialTicks);
}
