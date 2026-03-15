package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Type.getInternalName;

public class LifecycleTransformer implements ASMUtil.TransformerRule {

    @Override
    public byte[] accept(byte[] input) {
        String preInitType = getInternalName(FMLPreInitializationEvent.class);
        String initType = getInternalName(FMLInitializationEvent.class);

        return ASMUtil.builder(input)
                .transformNode(node -> {
                    MethodNode preInit = null;
                    MethodNode init = null;

                    for (MethodNode m : node.methods) {
                        Type[] args = Type.getArgumentTypes(m.desc);
                        for (Type arg : args) {
                            if (arg.getInternalName().equals(preInitType)) {
                                preInit = m;
                            } else if (arg.getInternalName().equals(initType)) {
                                init = m;
                            }
                        }
                    }

                    if (preInit != null && init != null) {
                        if (containsBlockRegistration(init)) {
                            moveInstructions(init, preInit);
                        }
                    }
                })
                .build();
    }

    private boolean containsBlockRegistration(MethodNode m) {
        for (AbstractInsnNode i : m.instructions.toArray()) {
            if (i instanceof MethodInsnNode min) {
                if (min.name.equals("registerBlock")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveInstructions(MethodNode source, MethodNode target) {
        Map<LabelNode, LabelNode> labels = new HashMap<>();

        InsnList cloned = new InsnList();
        for (AbstractInsnNode insn : source.instructions) {
            if (insn instanceof LabelNode ln) {
                labels.put(ln, new LabelNode());
            }
        }

        for (AbstractInsnNode insn : source.instructions) {
            cloned.add(insn.clone(labels));
        }

        AbstractInsnNode targetReturn = null;
        for (AbstractInsnNode i : target.instructions) {
            if (i.getOpcode() == Opcodes.RETURN) targetReturn = i;
        }

        if (targetReturn != null) {
            target.instructions.insertBefore(targetReturn, cloned);

            for (TryCatchBlockNode tcb : source.tryCatchBlocks) {
                target.tryCatchBlocks.add(new TryCatchBlockNode(
                        labels.get(tcb.start),
                        labels.get(tcb.end),
                        labels.get(tcb.handler),
                        tcb.type
                ));
            }
        }

        source.instructions.clear();
        source.instructions.add(new InsnNode(Opcodes.RETURN));
    }
}
