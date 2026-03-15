package com.github.dirify21.aml.mixin;

import com.github.dirify21.aml.client.ArchaicResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Final
    private List<IResourcePack> defaultResourcePacks;

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void aml$injectArchaicResourcePack(CallbackInfo ci) {
        this.defaultResourcePacks.add(new ArchaicResourcePack());
    }
}
