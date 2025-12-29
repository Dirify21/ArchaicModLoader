package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class AABBTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        for (MethodNode m : ctx.node().methods) {
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof MethodInsnNode min) {
                    if (min.owner.equals("net/minecraft/util/math/AxisAlignedBB") &&
                            (min.name.equals("func_72330_a") || min.name.equals("getBoundingBox"))) {

                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = HELPER;
                        min.name = "createAABB";
                        min.desc = "(DDDDDD)Lnet/minecraft/util/math/AxisAlignedBB;";
                    }
                }
            }
        }
    }
}