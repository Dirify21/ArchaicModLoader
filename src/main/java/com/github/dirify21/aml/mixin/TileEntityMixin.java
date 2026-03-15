package com.github.dirify21.aml.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin {
    public int field_145851_c;
    public int field_145848_d;
    public int field_145849_e;

    @Shadow
    public abstract BlockPos getPos();

    @Inject(method = "setPos", at = @At("RETURN"))
    private void onSetPos(BlockPos posIn, CallbackInfo ci) {
        this.updateCoords(posIn);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void onReadNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.updateCoords(this.getPos());
    }

    @Unique
    private void updateCoords(BlockPos pos) {
        this.field_145851_c = pos.getX();
        this.field_145848_d = pos.getY();
        this.field_145849_e = pos.getZ();
    }
}
