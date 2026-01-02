package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class LifecycleTransformer implements ASMUtil.TransformerRule {

    @Override
    public void accept(ASMUtil.ClassContext ctx) {
        MethodNode preInitMethod = null;
        MethodNode initMethod = null;

        for (MethodNode m : ctx.node().methods) {
            if (m.desc.contains("FMLPreInitializationEvent")) {
                preInitMethod = m;
            } else if (m.desc.contains("FMLInitializationEvent")) {
                initMethod = m;
            }
        }

        if (preInitMethod != null && initMethod != null) {
            if (containsBlockRegistration(initMethod)) {
                moveInstructions(initMethod, preInitMethod);
            }
        }
    }

    private boolean containsBlockRegistration(MethodNode m) {
        for (AbstractInsnNode i : m.instructions.toArray()) {
            if (i instanceof MethodInsnNode min) {
                if (min.name.equals("registerBlock") ||
                        (min.owner.contains("RedirectHelper") && min.name.contains("registerBlock"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveInstructions(MethodNode sourceInit, MethodNode targetPreInit) {
        InsnList instructionsToMove = sourceInit.instructions;

        AbstractInsnNode last = instructionsToMove.getLast();
        while (last != null && last.getOpcode() == -1) {
            last = last.getPrevious();
        }
        if (last != null && last.getOpcode() == Opcodes.RETURN) {
            instructionsToMove.remove(last);
        }

        AbstractInsnNode targetReturn = targetPreInit.instructions.getLast();
        while (targetReturn != null && targetReturn.getOpcode() != Opcodes.RETURN) {
            targetReturn = targetReturn.getPrevious();
        }

        if (targetReturn != null) {
            targetPreInit.instructions.insertBefore(targetReturn, instructionsToMove);

            if (sourceInit.tryCatchBlocks != null) {
                targetPreInit.tryCatchBlocks.addAll(sourceInit.tryCatchBlocks);
            }

            sourceInit.instructions.clear();
            sourceInit.instructions.add(new InsnNode(Opcodes.RETURN));

            if (sourceInit.localVariables != null) sourceInit.localVariables.clear();
            if (sourceInit.tryCatchBlocks != null) sourceInit.tryCatchBlocks.clear();
            if (sourceInit.attrs != null) sourceInit.attrs.clear();
        }
    }
}
