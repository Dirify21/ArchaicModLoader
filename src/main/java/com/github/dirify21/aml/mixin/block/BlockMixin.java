package com.github.dirify21.aml.mixin.block;

import com.github.dirify21.aml.client.utils.ClientUtils;
import com.github.dirify21.aml.interfaces.IArchaicBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
@SuppressWarnings("unused")
public class BlockMixin implements IArchaicBlock {

    public IIcon blockIcon;

    @Unique
    private String aml$textureName;

    @Override
    public Block func_149658_d(String name) {
        this.aml$textureName = name;
        return (Block) (Object) this;
    }

    @Override
    public String aml$getBlockTextureName() {
        return this.aml$textureName;
    }

    public String func_149641_N() {
        return this.aml$getBlockTextureName();
    }

    public IIcon func_149691_a(int side) {
        return this.aml$getIcon(side, 0);
    }

    public IIcon func_149673_e(int side, int meta) {
        return this.aml$getIcon(side, meta);
    }

    @Override
    public void func_149651_a(IIconRegister register) {
        String name = aml$getBlockTextureName();
        if (name != null) {
            register.registerIcon(name);
        }
    }

    @Override
    public IIcon aml$getIcon(int side, int meta) {
        IIcon icon = ClientUtils.invokeGetIcon(this, side, meta);
        return (icon != null) ? icon : this.blockIcon;
    }
}