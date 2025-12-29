package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraft.launchwrapper.IClassTransformer;
import static com.github.dirify21.aml.asm.util.ASMUtil.*;

public class AMLTransformer implements IClassTransformer {

    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";
    private static final String TEX_DESC = "(Lnet/minecraft/item/Item;Ljava/lang/String;)Lnet/minecraft/item/Item;";

    public static byte[] patchArchaicClass(byte[] basicClass) {
        return new AMLTransformer().transform(basicClass);
    }

    public byte[] transform(byte[] basicClass) {
        return process(basicClass,
                remap("cpw/mods/fml", "net/minecraftforge/fml"),
                remap("cpw.mods.fml", "net.minecraftforge.fml"),
                setAnn("net.minecraftforge.fml.common.Mod", "acceptedMinecraftVersions", "[1.12.2]"),
                redirect("func_111206_d", HELPER, "setTextureNameRedirect", TEX_DESC),
                redirect("setTextureName", HELPER, "setTextureNameRedirect", TEX_DESC)
        );
    }

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        if ("net.minecraftforge.fml.common.registry.GameRegistry".equals(transformedName)) {
            return process(basicClass, ctx -> ctx.addStaticMethod("registerItem",
                    "(Lnet/minecraft/item/Item;Ljava/lang/String;)V", this::buildRegisterItem));
        }

        if (transformedName.startsWith("com.github.dirify21.aml.") || !AMLClassLoader.isArchaicClass(transformedName)) {
            return basicClass;
        }

        return transform(basicClass);
    }

    private void buildRegisterItem(MethodBodyBuilder m) {
        m.loadArg(0)
                .getRegistryName()
                .ifNull(() -> {
                    m.loadArg(0).setRegistryName(1);
                });
        m.registerInForge(0)
                .returnValue();
    }
}