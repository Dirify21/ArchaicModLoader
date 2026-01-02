package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraft.launchwrapper.IClassTransformer;

import static com.github.dirify21.aml.asm.util.ASMUtil.*;

public class AMLTransformer implements IClassTransformer {

    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    public static byte[] patchArchaicClass(byte[] basicClass) {
        return new AMLTransformer().transform(basicClass);
    }


    public byte[] transform(byte[] basicClass) {
        return process(basicClass,
                remap("cpw/mods/fml", "net/minecraftforge/fml"),
                setAnn("net/minecraftforge/fml/common/Mod", "acceptedMinecraftVersions", "[1.12.2]"),
                remap("net/minecraft/util/IIcon", "com/github/dirify21/aml/api/IIcon"),
                remap("net/minecraft/client/renderer/texture/IIconRegister", "com/github/dirify21/aml/api/IIconRegister"),
                updateAnn("net/minecraftforge/fml/common/Mod", "modid", v -> v.toString().toLowerCase()),
                redirect("func_111206_d", HELPER, "setTextureNameRedirect", "(Lnet/minecraft/item/Item;Ljava/lang/String;)Lnet/minecraft/item/Item;"),
                redirect("func_149658_d", HELPER, "setBlockTextureNameRedirect", "(Lnet/minecraft/block/Block;Ljava/lang/String;)Lnet/minecraft/block/Block;"),
                redirect("func_149641_N", HELPER, "getTextureNameRedirect", "(Lnet/minecraft/block/Block;)Ljava/lang/String;"),
                remap("net/minecraft/util/AxisAlignedBB", "net/minecraft/util/math/AxisAlignedBB"),
                remap("worldObj", "field_72995_K"),
                new TileEntityTransformer(),
                new RegistryTransformer(),
                new PotionTransformer(),
                new WorldTransformer(),
                new AABBTransformer(),
                new RenderTransformer(),
                new LifecycleTransformer(),
                ctx -> ctx.redirectFieldToMethod("net/minecraft/potion/Potion", "field_76415_H", HELPER, "getPotionIdRedirect", "(Lnet/minecraft/potion/Potion;)I")
        );
    }

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        if ("net.minecraftforge.fml.common.registry.GameRegistry".equals(transformedName)) {
            return process(basicClass,
                    ctx -> ctx.addStaticMethod("registerItem", "(Lnet/minecraft/item/Item;Ljava/lang/String;)V", this::buildRegisterItem),
                    ctx -> ctx.addStaticMethod("registerBlock", "(Lnet/minecraft/block/Block;Ljava/lang/String;)Lnet/minecraft/block/Block;", this::buildRegisterBlock)
            );
        }

        if (!AMLClassLoader.isPackageMatch(transformedName)) {
            return basicClass;
        }

        return transform(basicClass);
    }

    private void buildRegisterItem(MethodBodyBuilder m) {
        m.loadArg(0);
        m.loadArg(1);
        m.invokeStatic("com/github/dirify21/aml/util/RegistryHolder", "queueItem", "(Lnet/minecraft/item/Item;Ljava/lang/String;)V");
        m.returnValue();
    }

    private void buildRegisterBlock(MethodBodyBuilder m) {
        m.loadArg(0);
        m.loadArg(1);
        m.invokeStatic("com/github/dirify21/aml/util/RegistryHolder",
                "queueBlock",
                "(Lnet/minecraft/block/Block;Ljava/lang/String;)V");

        m.loadArg(0);
        m.returnObject();
    }
}