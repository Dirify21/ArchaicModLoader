package com.github.dirify21.aml.mixin;

import fr.catcore.cursedmixinextensions.annotations.Public;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
@SuppressWarnings("unused")
public abstract class RenderManagerMixin {

    @Public
    private static RenderManager field_78727_a;

    @Shadow
    public abstract void renderEntity(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        field_78727_a = (RenderManager) (Object) this;
    }

    public void func_188391_a(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_) {
        Render<Entity> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityIn);
        if (render != null) {
            render.doRender(entityIn, x, y, z, yaw, partialTicks);
        }
    }

    public boolean func_147940_a(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        try {
            renderEntity(entityIn, x, y, z, yaw, partialTicks, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
