package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class RenderTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        ClassNode cn = ctx.node();
        if (cn.superName == null) return;

        boolean isRenderer = cn.superName.contains("TileEntitySpecialRenderer");

        if (isRenderer) {
            for (MethodNode m : cn.methods) {
                if (m.name.equals("renderTileEntityAt") || m.name.equals("func_147500_a")) {
                    m.name = "func_192841_a";
                    m.desc = "(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V";
                }

                AbstractInsnNode[] instructions = m.instructions.toArray();
                for (AbstractInsnNode i : instructions) {

                    if (i instanceof FieldInsnNode fin) {
                        if (fin.owner.contains("RenderManager") && (fin.name.equals("field_78727_a") || fin.name.equals("instance"))) {
                            m.instructions.set(i, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "getRenderManager", "()Lnet/minecraft/client/renderer/entity/RenderManager;", false));
                        }
                    }

                    if (i instanceof MethodInsnNode min) {
                        if (min.owner.contains("RenderManager") && (min.name.equals("func_147940_a") || min.name.equals("doRender"))) {
                            if (min.desc.equals("(Lnet/minecraft/entity/Entity;DDDFF)Z")) {

                                InsnList colorReset = new InsnList();
                                colorReset.add(new LdcInsnNode(1.0F));
                                colorReset.add(new LdcInsnNode(1.0F));
                                colorReset.add(new LdcInsnNode(1.0F));
                                colorReset.add(new LdcInsnNode(1.0F));
                                colorReset.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", "func_179131_c", "(FFFF)V", false));
                                m.instructions.insertBefore(min, colorReset);

                                InsnList lightFix = new InsnList();
                                lightFix.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/client/renderer/OpenGlHelper", "field_77478_a", "I"));
                                lightFix.add(new LdcInsnNode(240.0F));
                                lightFix.add(new LdcInsnNode(240.0F));
                                lightFix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/renderer/OpenGlHelper", "func_77475_a", "(IFF)V", false));
                                m.instructions.insertBefore(min, lightFix);

                                min.name = "func_188391_a";
                                min.desc = "(Lnet/minecraft/entity/Entity;DDDFFZ)V";

                                m.instructions.insertBefore(min, new InsnNode(Opcodes.ICONST_0));

                                m.instructions.insert(min, new InsnNode(Opcodes.ICONST_1));
                            }
                        }
                    }
                }
            }
        }
    }
}