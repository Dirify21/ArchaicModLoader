package com.github.dirify21.aml.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
@SuppressWarnings("unused")
public class WorldMixin {

    public final World worldObj = (World) (Object) this;

    public Block func_147439_a(int x, int y, int z) {
        return worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public int func_72805_g(int x, int y, int z) {
        IBlockState state = worldObj.getBlockState(new BlockPos(x, y, z));
        return state.getBlock().getMetaFromState(state);
    }
}
