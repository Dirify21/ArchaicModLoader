package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import com.github.dirify21.aml.core.AMLLoadingPlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Mod;

import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;

public class AMLTransformer implements IClassTransformer {

    public static byte[] patchArchaicClass(byte[] basicClass) {
        return new AMLTransformer().transform(basicClass);
    }

    public byte[] transform(byte[] basicClass) {
        return ASMUtil.builder(basicClass)
                .remap("cpw/mods/fml", "net/minecraftforge/fml")
                .modifyAnnotationValue(getDescriptor(Mod.class), "acceptedMinecraftVersions", _ -> "[1.12.2]")
                .modifyAnnotationValue(getDescriptor(Mod.class), "modid", v -> v.toString().toLowerCase())
                .renameMethod("func_111206_d", "setTextureName")
                .remap("net/minecraft/util/AxisAlignedBB", getInternalName(AxisAlignedBB.class))
                .apply(new LifecycleTransformer())
                .apply(new PotionTransformer())
                .apply(new TileEntityTransformer())
                .build();
    }

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        if (AMLLoadingPlugin.ARCHAIC_CLASSES.contains(className)) {
            return transform(basicClass);
        }

        return basicClass;
    }
}