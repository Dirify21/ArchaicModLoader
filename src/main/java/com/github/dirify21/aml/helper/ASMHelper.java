package com.github.dirify21.aml.helper;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public final class ASMHelper {
    public ASMHelper() {
    }

    public static boolean isMethod(MethodNode method, String name) {
        return name.equals(method.name);
    }

    public static boolean isMethod(MethodNode method, String name, String desc) {
        return name.equals(method.name) && desc.equals(method.desc);
    }

    public static boolean isMethodCall(AbstractInsnNode insn, int opcode, String owner, String name, String desc) {
        if (!(insn instanceof MethodInsnNode min)) return false;
        return min.getOpcode() == opcode
                && owner.equals(min.owner)
                && name.equals(min.name)
                && (desc == null || desc.equals(min.desc));
    }

    public static String methodDesc(Class<?> returnType, Class<?>... params) {
        Type[] asmParams = new Type[params.length];
        for (int i = 0; i < params.length; i++) {
            asmParams[i] = Type.getType(params[i]);
        }
        return Type.getMethodDescriptor(Type.getType(returnType), asmParams);
    }

    public static boolean isFieldAccess(AbstractInsnNode insn, int opcode, String owner, String namePrefix) {
        if (!(insn instanceof FieldInsnNode fin)) return false;
        return fin.getOpcode() == opcode
                && owner.equals(fin.owner)
                && (namePrefix == null || fin.name.startsWith(namePrefix));
    }
}
