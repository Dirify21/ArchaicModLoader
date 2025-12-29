package com.github.dirify21.aml.asm.util;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ASMUtil {

    public static byte[] process(byte[] basicClass, TransformerRule... rules) {
        if (basicClass == null) return null;
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        ClassContext context = new ClassContext(classNode);
        for (TransformerRule rule : rules) rule.accept(context);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String t1, String t2) {
                return "java/lang/Object";
            }
        };
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static TransformerRule remap(String oldPath, String newPath) {
        return context -> context.replaceInStrings(oldPath, newPath);
    }

    public static TransformerRule setAnn(String desc, String key, Object value) {
        return context -> context.setAnnotationValue(desc, key, value);
    }

    public static TransformerRule redirect(String name, String owner, String newName, String desc) {
        return context -> context.redirectMethod(name, owner, newName, desc);
    }

    private static void remapClassStrings(ClassNode cn, Function<String, String> r) {
        cn.name = r.apply(cn.name);
        cn.superName = r.apply(cn.superName);
        if (cn.interfaces != null) cn.interfaces.replaceAll(r::apply);
        processAnns(cn.visibleAnnotations, r);
        processAnns(cn.invisibleAnnotations, r);
        for (FieldNode f : cn.fields) {
            f.desc = r.apply(f.desc);
            processAnns(f.visibleAnnotations, r);
        }
        for (MethodNode m : cn.methods) {
            m.desc = r.apply(m.desc);
            processAnns(m.visibleAnnotations, r);
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof MethodInsnNode min) {
                    min.owner = r.apply(min.owner);
                    min.desc = r.apply(min.desc);
                } else if (i instanceof FieldInsnNode fin) {
                    fin.owner = r.apply(fin.owner);
                    fin.desc = r.apply(fin.desc);
                } else if (i instanceof TypeInsnNode tin) {
                    tin.desc = r.apply(tin.desc);
                } else if (i instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                    ldc.cst = r.apply(s);
                }
            }
        }
    }

    private static void processAnns(List<AnnotationNode> anns, Function<String, String> r) {
        if (anns == null) return;
        for (AnnotationNode a : anns) {
            a.desc = r.apply(a.desc);
            if (a.values != null) {
                for (int i = 1; i < a.values.size(); i += 2) {
                    if (a.values.get(i) instanceof String s) a.values.set(i, r.apply(s));
                }
            }
        }
    }

    @FunctionalInterface
    public interface TransformerRule extends Consumer<ClassContext> {}

    public interface MethodBodyBuilder {
        MethodBodyBuilder loadArg(int index);
        MethodBodyBuilder ldc(Object value);
        MethodBodyBuilder invokeVirtual(String owner, String name, String desc);
        MethodBodyBuilder invokeInterface(String owner, String name, String desc);
        MethodBodyBuilder invokeStatic(String owner, String name, String desc);
        MethodBodyBuilder getStaticField(String owner, String name, String desc);
        MethodBodyBuilder putStaticField(String owner, String name, String desc);
        MethodBodyBuilder putField(String owner, String name, String desc);
        MethodBodyBuilder createNewObject(String type, String ctor, Consumer<MethodBodyBuilder> args);
        MethodBodyBuilder pop();
        MethodBodyBuilder swap();
        MethodBodyBuilder ifNull(Runnable branch);
        void returnValue();
        void returnObject();
    }

    public record ClassContext(ClassNode node) {
        public ClassContext replaceInStrings(String oldText, String newText) {
            remapClassStrings(node, s -> s == null ? null : s.replace(oldText, newText));
            return this;
        }

        public ClassContext setAnnotationValue(String annDesc, String key, Object value) {
            String desc = annDesc.startsWith("L") ? annDesc : "L" + annDesc.replace('.', '/') + ";";
            if (node.visibleAnnotations == null) node.visibleAnnotations = new ArrayList<>();
            updateAnnotationValue(node.visibleAnnotations, desc, key, value);
            return this;
        }

        private void updateAnnotationValue(List<AnnotationNode> anns, String d, String k, Object v) {
            for (AnnotationNode a : anns) {
                if (a.desc.equals(d)) {
                    if (a.values == null) a.values = new ArrayList<>();
                    for (int i = 0; i < a.values.size(); i += 2) {
                        if (k.equals(a.values.get(i))) {
                            a.values.set(i + 1, v);
                            return;
                        }
                    }
                    a.values.add(k);
                    a.values.add(v);
                    return;
                }
            }
        }

        public ClassContext redirectMethod(String targetName, String newOwner, String newName, String newDesc) {
            for (MethodNode m : node.methods) {
                for (AbstractInsnNode i : m.instructions.toArray()) {
                    if (i instanceof MethodInsnNode min && min.name.equals(targetName)) {
                        min.setOpcode(Opcodes.INVOKESTATIC);
                        min.owner = newOwner;
                        min.name = newName;
                        min.desc = newDesc;
                    }
                }
            }
            return this;
        }

        public ClassContext redirectFieldToMethod(String owner, String fieldName, String hOwner, String hName, String hDesc) {
            for (MethodNode m : node.methods) {
                for (AbstractInsnNode i : m.instructions.toArray()) {
                    if (i instanceof FieldInsnNode fin && fin.owner.equals(owner) && fin.name.equals(fieldName)) {
                        m.instructions.set(i, new MethodInsnNode(Opcodes.INVOKESTATIC, hOwner, hName, hDesc, false));
                    }
                }
            }
            return this;
        }

        public void addStaticMethod(String name, String desc, Consumer<MethodBodyBuilder> generator) {
            MethodNode mn = node.methods.stream()
                    .filter(m -> m.name.equals(name) && m.desc.equals(desc))
                    .findFirst()
                    .orElseGet(() -> {
                        MethodNode newMn = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, desc, null, null);
                        node.methods.add(newMn);
                        return newMn;
                    });

            MethodBodyBuilder builder = createBuilder(mn);
            if (mn.instructions.size() == 0) {
                mn.visitCode();
                generator.accept(builder);
                if (desc.endsWith("V")) builder.returnValue();
            } else {
                generator.accept(builder);
            }
        }

        private MethodBodyBuilder createBuilder(MethodNode mn) {
            return new MethodBodyBuilder() {
                @Override public MethodBodyBuilder loadArg(int i) { mn.visitVarInsn(Opcodes.ALOAD, i); return this; }
                @Override public MethodBodyBuilder ldc(Object v) { mn.visitLdcInsn(v); return this; }
                @Override public MethodBodyBuilder pop() { mn.visitInsn(Opcodes.POP); return this; }
                @Override public MethodBodyBuilder swap() { mn.visitInsn(Opcodes.SWAP); return this; }
                @Override public void returnValue() { mn.visitInsn(Opcodes.RETURN); }
                @Override public void returnObject() { mn.visitInsn(Opcodes.ARETURN); }
                @Override public MethodBodyBuilder getStaticField(String o, String n, String d) { mn.visitFieldInsn(Opcodes.GETSTATIC, o, n, d); return this; }
                @Override public MethodBodyBuilder putStaticField(String o, String n, String d) { mn.visitFieldInsn(Opcodes.PUTSTATIC, o, n, d); return this; }
                @Override public MethodBodyBuilder putField(String o, String n, String d) { mn.visitFieldInsn(Opcodes.PUTFIELD, o, n, d); return this; }
                @Override public MethodBodyBuilder invokeVirtual(String o, String n, String d) { mn.visitMethodInsn(Opcodes.INVOKEVIRTUAL, o, n, d, false); return this; }
                @Override public MethodBodyBuilder invokeInterface(String o, String n, String d) { mn.visitMethodInsn(Opcodes.INVOKEINTERFACE, o, n, d, true); return this; }
                @Override public MethodBodyBuilder invokeStatic(String o, String n, String d) { mn.visitMethodInsn(Opcodes.INVOKESTATIC, o, n, d, false); return this; }
                @Override public MethodBodyBuilder createNewObject(String t, String c, Consumer<MethodBodyBuilder> a) {
                    mn.visitTypeInsn(Opcodes.NEW, t);
                    mn.visitInsn(Opcodes.DUP);
                    a.accept(this);
                    mn.visitMethodInsn(Opcodes.INVOKESPECIAL, t, "<init>", c, false);
                    return this;
                }
                @Override public MethodBodyBuilder ifNull(Runnable b) {
                    Label end = new Label();
                    mn.visitJumpInsn(Opcodes.IFNONNULL, end);
                    b.run();
                    mn.visitLabel(end);
                    return this;
                }
            };
        }
    }
}