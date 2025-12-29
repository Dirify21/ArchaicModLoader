package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RegistryTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        for (MethodNode m : ctx.node().methods) {
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof MethodInsnNode min) {
                    if (min.owner.equals("net/minecraftforge/fml/common/registry/GameRegistry") &&
                            min.name.equals("addRecipe") &&
                            min.desc.equals("(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V")) {

                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = HELPER;
                        min.name = "addRecipeRedirect";
                        min.desc = "(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V";
                    }
                    if (min.owner.equals("net/minecraftforge/fml/common/registry/GameRegistry") &&
                            min.name.equals("registerTileEntity")) {
                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = HELPER;
                        min.name = "registerTileEntityRedirect";
                    }
                }
            }
        }
    }
}