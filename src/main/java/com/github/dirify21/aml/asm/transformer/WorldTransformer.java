package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class WorldTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        for (MethodNode m : ctx.node().methods) {
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof MethodInsnNode min && min.owner.equals("net/minecraft/world/World")) {
                    if (min.name.equals("func_147439_a") || min.name.equals("getBlock")) {
                        redirect(min, "getBlockRedirect", "(Lnet/minecraft/world/World;III)Lnet/minecraft/block/Block;");
                    } else if (min.name.equals("func_72805_g") || min.name.equals("getBlockMetadata")) {
                        redirect(min, "getMetadataRedirect", "(Lnet/minecraft/world/World;III)I");
                    }
                }
            }
        }
    }

    private void redirect(MethodInsnNode min, String name, String desc) {
        min.setOpcode(Opcodes.INVOKESTATIC);
        min.owner = HELPER;
        min.name = name;
        min.desc = desc;
    }
}