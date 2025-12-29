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
                redirect("func_149658_d", HELPER, "setBlockTextureNameRedirect", "(Lnet/minecraft/block/Block;Ljava/lang/String;)Lnet/minecraft/block/Block;")
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

        if (transformedName.startsWith("com.github.dirify21.aml.") || !AMLClassLoader.isArchaicClass(transformedName)) {
            return basicClass;
        }

        return transform(basicClass);
    }

    private void buildRegisterItem(MethodBodyBuilder m) {
        String owner = "net/minecraft/item/Item";
        m.loadArg(0)
                .getRegistryName(owner)
                .ifNull(() -> m.loadArg(0).setRegistryName(owner, 1));
        m.registerItemInForge(0)
                .returnValue();
    }

    private void buildRegisterBlock(MethodBodyBuilder m) {
        String blockOwner = "net/minecraft/block/Block";
        String itemOwner = "net/minecraft/item/Item";
        String registryOwner = "net/minecraftforge/registries/IForgeRegistry";
        m.loadArg(0)
                .getRegistryName(blockOwner)
                .ifNull(() -> m.loadArg(0).setRegistryName(blockOwner, 1));
        m.getStaticField("net/minecraftforge/fml/common/registry/ForgeRegistries", "BLOCKS", "Lnet/minecraftforge/registries/IForgeRegistry;");
        m.loadArg(0);
        m.invokeInterface(registryOwner, "register", "(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V");
        m.getStaticField("net/minecraftforge/fml/common/registry/ForgeRegistries", "ITEMS", "Lnet/minecraftforge/registries/IForgeRegistry;");
        m.createNewObject("net/minecraft/item/ItemBlock", "(Lnet/minecraft/block/Block;)V", a -> a.loadArg(0));
        m.loadArg(0).invokeVirtual(blockOwner, "getRegistryName", "()Lnet/minecraft/util/ResourceLocation;");
        m.invokeVirtual(itemOwner, "setRegistryName", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraftforge/registries/IForgeRegistryEntry;");
        m.invokeInterface(registryOwner, "register", "(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V");
        m.loadArg(0).returnObject();
    }
}
