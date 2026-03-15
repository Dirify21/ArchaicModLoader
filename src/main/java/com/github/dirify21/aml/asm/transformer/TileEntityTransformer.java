package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.util.ASMUtil;
import com.github.dirify21.aml.helper.ASMHelper;
import com.github.dirify21.aml.helper.TileEntityHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;

import static com.github.dirify21.aml.asm.util.ASMUtil.builder;
import static com.github.dirify21.aml.helper.ASMHelper.methodDesc;
import static org.objectweb.asm.Type.getInternalName;

public class TileEntityTransformer implements ASMUtil.TransformerRule {
    private static final String HELPER = getInternalName(TileEntityHelper.class);
    private static final String TILE_ENTITY = getInternalName(TileEntity.class);
    private static final String ITICKABLE = getInternalName(ITickable.class);

    @Override
    public byte[] accept(byte[] input) {
        return builder(input)
                .transformNode(node -> {
                    if (!TILE_ENTITY.equals(node.superName)) return;

                    boolean hasOnLoad = false;

                    for (MethodNode method : node.methods) {
                        if (ASMHelper.isMethod(method, "func_145845_h", methodDesc(void.class))) {
                            if (!node.interfaces.contains(ITICKABLE)) node.interfaces.add(ITICKABLE);
                            method.name = "func_73660_a";
                        }

                        if (ASMHelper.isMethod(method, "onLoad", methodDesc(void.class))) {
                            hasOnLoad = true;
                            injectTransparentLogic(method);
                        }

                        removeSuperUpdateCalls(method);
                    }

                    if (!hasOnLoad) {
                        node.methods.add(createOnLoadMethod());
                    }
                })
                .build();
    }

    private void injectTransparentLogic(MethodNode method) {
        for (var insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.RETURN) {
                var list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "makeTransparent", methodDesc(void.class, Object.class), false));
                method.instructions.insertBefore(insn, list);
            }
        }
    }

    private void removeSuperUpdateCalls(MethodNode method) {
        var toRemove = new ArrayList<AbstractInsnNode>();

        for (var insn : method.instructions) {
            if (ASMHelper.isMethodCall(insn, Opcodes.INVOKESPECIAL, TILE_ENTITY, "func_145845_h", null)) {
                toRemove.add(insn);
            }
        }

        for (var insn : toRemove) {
            var prev = insn.getPrevious();
            if (prev instanceof VarInsnNode vin && vin.getOpcode() == Opcodes.ALOAD && vin.var == 0) {
                method.instructions.remove(prev);
            }
            method.instructions.remove(insn);
        }
    }

    private MethodNode createOnLoadMethod() {
        var mn = new MethodNode(Opcodes.ACC_PUBLIC, "onLoad", methodDesc(void.class), null, null);
        var insn = mn.instructions;

        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TILE_ENTITY, "onLoad", methodDesc(void.class), false));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "makeTransparent", methodDesc(void.class, Object.class), false));
        insn.add(new InsnNode(Opcodes.RETURN));

        return mn;
    }
}