package net.minecraft.client.renderer.texture;

import net.minecraft.util.IIcon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IIconRegister {
    IIcon registerIcon(String textureName);

    default IIcon func_94245_a(String textureName) {
        return registerIcon(textureName);
    }
}