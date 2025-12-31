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

        boolean hasOnLoad = false;

        for (MethodNode m : ctx.node().methods) {
            if ((m.name.equals("updateEntity") || m.name.equals("func_145845_h")) && m.desc.equals("()V")) {
                m.name = "func_73660_a";
            }

            if (m.name.equals("onLoad") && m.desc.equals("()V")) {
                hasOnLoad = true;
                for (AbstractInsnNode i : m.instructions.toArray()) {
                    if (i.getOpcode() == Opcodes.RETURN) {
                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "makeTransparent", "(Ljava/lang/Object;)V", false));
                        m.instructions.insertBefore(i, list);
                    }
                }
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

        if (!hasOnLoad) {
            MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "onLoad", "()V", null, null);
            InsnList list = m.instructions;

            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, ctx.node().superName, "onLoad", "()V", false));

            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "makeTransparent", "(Ljava/lang/Object;)V", false));

            list.add(new InsnNode(Opcodes.RETURN));
            ctx.node().methods.add(m);
        }
    }

    private void replaceWithStatic(MethodNode m, AbstractInsnNode i, String methodName) {
        m.instructions.set(i, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, methodName, "(Lnet/minecraft/tileentity/TileEntity;)I", false));
    }
}