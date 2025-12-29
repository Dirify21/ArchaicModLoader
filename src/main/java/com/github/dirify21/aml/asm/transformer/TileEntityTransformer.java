package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class TileEntityTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        if (ctx.node().superName == null || !ctx.node().superName.equals("net/minecraft/tileentity/TileEntity")) return;

        if (!ctx.node().interfaces.contains("net/minecraft/util/ITickable")) {
            ctx.node().interfaces.add("net/minecraft/util/ITickable");
        }

        for (MethodNode m : ctx.node().methods) {
            if ((m.name.equals("updateEntity") || m.name.equals("func_145845_h")) && m.desc.equals("()V")) {
                m.name = "func_73660_a";
            }

            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof FieldInsnNode fin) {
                    switch (fin.name) {
                        case "xCoord", "field_145851_c" -> replaceWithStatic(m, i, "getX");
                        case "yCoord", "field_145848_d" -> replaceWithStatic(m, i, "getY");
                        case "zCoord", "field_145849_e" -> replaceWithStatic(m, i, "getZ");
                    }
                }

                if (i instanceof MethodInsnNode min) {
                    if ((min.name.equals("updateEntity") || min.name.equals("func_145845_h")) &&
                            min.getOpcode() == Opcodes.INVOKESPECIAL &&
                            min.owner.equals("net/minecraft/tileentity/TileEntity")) {
                        AbstractInsnNode prev = min.getPrevious();
                        if (prev != null && prev.getOpcode() == Opcodes.ALOAD) m.instructions.remove(prev);
                        m.instructions.remove(min);
                    }
                }
            }
        }
    }

    private void replaceWithStatic(MethodNode m, AbstractInsnNode i, String methodName) {
        m.instructions.set(i, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, methodName, "(Lnet/minecraft/tileentity/TileEntity;)I", false));
    }
}