package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import com.github.dirify21.aml.helper.ASMHelper;
import com.github.dirify21.aml.helper.PotionHelper;
import net.minecraft.potion.Potion;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static com.github.dirify21.aml.helper.ASMHelper.methodDesc;

public class PotionTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = Type.getInternalName(PotionHelper.class);
    private static final String POTION = Type.getInternalName(Potion.class);

    @Override
    public byte[] accept(byte[] input) {
        return ASMUtil.builder(input)
                .transformNode(node -> {
                    for (MethodNode m : node.methods) {
                        transformPotionFields(m);
                    }
                })
                .redirectFieldToMethod(POTION, "field_76415_H", POTION, "func_188409_a", methodDesc(int.class, Potion.class))
                .build();
    }

    private void transformPotionFields(MethodNode m) {
        for (AbstractInsnNode i : m.instructions) {
            if (ASMHelper.isFieldAccess(i, Opcodes.GETSTATIC, POTION, "field_")) {
                FieldInsnNode fin = (FieldInsnNode) i;

                InsnList list = new InsnList();
                list.add(new LdcInsnNode(fin.name));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "getPotionField", methodDesc(Potion.class, String.class), false));

                m.instructions.insertBefore(i, list);
                m.instructions.remove(i);
            }
        }
    }
}