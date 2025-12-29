package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PotionTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = "com/github/dirify21/aml/util/RedirectHelper";

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        for (MethodNode m : ctx.node().methods) {
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof FieldInsnNode fin && fin.getOpcode() == Opcodes.GETSTATIC && fin.owner.equals("net/minecraft/potion/Potion")) {
                    if (fin.name.startsWith("field_")) {
                        InsnList list = new InsnList();
                        list.add(new LdcInsnNode(fin.name));
                        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "getPotionFieldRedirect", "(Ljava/lang/String;)Lnet/minecraft/potion/Potion;", false));
                        m.instructions.insertBefore(i, list);
                        m.instructions.remove(i);
                    }
                }

                if (i instanceof MethodInsnNode min) {
                    if (min.getOpcode() == Opcodes.INVOKESPECIAL &&
                            min.owner.equals("net/minecraft/potion/PotionEffect") &&
                            min.desc.equals("(III)V")) {

                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = HELPER;
                        min.name = "createPotionEffect";
                        min.desc = "(III)Lnet/minecraft/potion/PotionEffect;";

                        cleanUpConstructor(m, min);
                    }
                    if ((min.name.equals("func_77844_a") || min.name.equals("setPotionEffect")) &&
                            min.desc.equals("(IIIF)Lnet/minecraft/item/ItemFood;")) {

                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = HELPER;
                        min.name = "setPotionEffectRedirect";
                        min.desc = "(Lnet/minecraft/item/ItemFood;IIIF)Lnet/minecraft/item/ItemFood;";
                    }
                }
            }
        }
    }

    private void cleanUpConstructor(MethodNode m, MethodInsnNode min) {
        AbstractInsnNode prev = min.getPrevious();
        int count = 0;
        while (prev != null && count < 10) {
            if (prev.getOpcode() == Opcodes.DUP) {
                AbstractInsnNode target = prev;
                prev = prev.getPrevious();
                m.instructions.remove(target);
            } else if (prev.getOpcode() == Opcodes.NEW) {
                AbstractInsnNode target = prev;
                prev = prev.getPrevious();
                m.instructions.remove(target);
                break;
            } else {
                prev = prev.getPrevious();
            }
            count++;
        }
    }
}