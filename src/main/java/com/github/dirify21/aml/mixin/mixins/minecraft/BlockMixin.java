package com.github.dirify21.aml.mixin.mixins.minecraft;

import com.github.dirify21.aml.api.IArchaicBlock;
import com.github.dirify21.aml.api.IIcon;
import com.github.dirify21.aml.api.IIconRegister;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

@Mixin(Block.class)
public abstract class BlockMixin implements IArchaicBlock {

    public IIcon blockIcon;

    @Unique
    private String aml$textureName;

    @Unique
    private Method aml$getIconMethod = null;
    @Unique
    private boolean aml$methodLookupFailed = false;

    @Override
    public Block aml$setBlockTextureName(String name) {
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
    public void aml$registerBlockIcons(IIconRegister register) {
        String name = aml$getBlockTextureName();
        if (name != null) {
            register.registerIcon(name);
        }

        try {
            Method m = findMethod(this.getClass(), new String[]{"registerBlockIcons", "func_149651_a"}, IIconRegister.class);
            if (m != null) {
                m.setAccessible(true);
                m.invoke(this, register);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IIcon aml$getIcon(int side, int meta) {
        if (aml$methodLookupFailed) return this.blockIcon;
        try {
            if (aml$getIconMethod == null) {
                for (Method m : this.getClass().getMethods()) {
                    if (m.getParameterCount() == 2 &&
                            m.getParameterTypes()[0] == int.class &&
                            m.getParameterTypes()[1] == int.class &&
                            IIcon.class.isAssignableFrom(m.getReturnType())) {
                        if (m.getDeclaringClass() == BlockMixin.class) continue;
                        aml$getIconMethod = m;
                        break;
                    }
                }

                if (aml$getIconMethod == null) {
                    for (Method m : this.getClass().getMethods()) {
                        if (m.getParameterCount() == 1 &&
                                m.getParameterTypes()[0] == int.class &&
                                IIcon.class.isAssignableFrom(m.getReturnType())) {
                            if (m.getDeclaringClass() == BlockMixin.class) continue;
                            aml$getIconMethod = m;
                            break;
                        }
                    }
                }
                if (aml$getIconMethod == null) {
                    aml$methodLookupFailed = true;
                    return this.blockIcon;
                }
                aml$getIconMethod.setAccessible(true);
            }
            Object result;
            if (aml$getIconMethod.getParameterCount() == 2) {
                result = aml$getIconMethod.invoke(this, side, meta);
            } else {
                result = aml$getIconMethod.invoke(this, side);
            }
            return (IIcon) result;

        } catch (Exception e) {
            aml$methodLookupFailed = true;
            return this.blockIcon;
        }
    }

    private Method findMethod(Class<?> clazz, String[] names, Class<?>... types) {
        for (String name : names) {
            try {
                return clazz.getMethod(name, types);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }
}