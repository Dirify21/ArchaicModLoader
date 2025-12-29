package com.github.dirify21.aml.asm.util;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import java.util.function.*;

public class ASMUtil {

    @FunctionalInterface
    public interface TransformerRule extends Consumer<ClassContext> {}

    public record ClassContext(ClassNode node) {

        public ClassContext replaceInStrings(String oldText, String newText) {
                remapClassStrings(node, s -> s == null ? null : s.replace(oldText, newText));
                return this;
            }

            public ClassContext setAnnotationValue(String annDesc, String key, Object value) {
                String desc = annDesc.startsWith("L") ? annDesc : "L" + annDesc.replace('.', '/') + ";";
                updateAnnotationValue(node.visibleAnnotations, desc, key, value);
                return this;
            }

            public ClassContext redirectMethod(String targetName, String newOwner, String newName, String newDesc) {
                for (MethodNode method : node.methods) redirectMethodCall(method, targetName, newOwner, newName, newDesc);
                return this;
            }

            public ClassContext addStaticMethod(String name, String desc, Consumer<MethodBodyBuilder> generator) {
                buildStaticMethod(node, name, desc, generator);
                return this;
            }
        }

    public interface MethodBodyBuilder {
        MethodBodyBuilder loadArg(int index);
        MethodBodyBuilder invokeVirtual(String owner, String name, String desc);
        MethodBodyBuilder invokeInterface(String owner, String name, String desc);
        MethodBodyBuilder getStaticField(String owner, String name, String desc);
        MethodBodyBuilder createNewObject(String type, String ctor, Consumer<MethodBodyBuilder> args);
        MethodBodyBuilder pop();
        MethodBodyBuilder ifNull(Runnable branch);
        void returnValue();

        default MethodBodyBuilder getRegistryName() {
            return invokeVirtual("net/minecraft/item/Item", "getRegistryName", "()Lnet/minecraft/util/ResourceLocation;");
        }

        default MethodBodyBuilder setRegistryName(int nameArgIndex) {
            createNewObject("net/minecraft/util/ResourceLocation", "(Ljava/lang/String;)V", a -> a.loadArg(nameArgIndex));
            invokeVirtual("net/minecraft/item/Item", "setRegistryName", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/item/IForgeRegistryEntry;");
            return pop();
        }

        default MethodBodyBuilder registerInForge(int itemArgIndex) {
            getStaticField("net/minecraftforge/fml/common/registry/ForgeRegistries", "ITEMS", "Lnet/minecraftforge/registries/IForgeRegistry;");
            loadArg(itemArgIndex);
            return invokeInterface("net/minecraftforge/registries/IForgeRegistry", "register", "(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V");
        }
    }

    public static byte[] process(byte[] basicClass, TransformerRule... rules) {
        if (basicClass == null) return null;
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        ClassContext context = new ClassContext(classNode);
        for (TransformerRule rule : rules) rule.accept(context);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override protected String getCommonSuperClass(String t1, String t2) { return "java/lang/Object"; }
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
        cn.superName = r.apply(cn.superName);
        if (cn.interfaces != null) cn.interfaces.replaceAll(r::apply);
        processAnns(cn.visibleAnnotations, r);
        processAnns(cn.invisibleAnnotations, r);
        for (FieldNode f : cn.fields) { f.desc = r.apply(f.desc); processAnns(f.visibleAnnotations, r); }
        for (MethodNode m : cn.methods) {
            m.desc = r.apply(m.desc); processAnns(m.visibleAnnotations, r);
            for (AbstractInsnNode i : m.instructions.toArray()) {
                if (i instanceof MethodInsnNode min) { min.owner = r.apply(min.owner); min.desc = r.apply(min.desc); }
                else if (i instanceof FieldInsnNode fin) { fin.owner = r.apply(fin.owner); fin.desc = r.apply(fin.desc); }
                else if (i instanceof TypeInsnNode tin) { tin.desc = r.apply(tin.desc); }
                else if (i instanceof LdcInsnNode ldc && ldc.cst instanceof String s) { ldc.cst = r.apply(s); }
            }
        }
    }

    private static void redirectMethodCall(MethodNode m, String t, String o, String n, String d) {
        for (AbstractInsnNode i : m.instructions.toArray()) {
            if (i instanceof MethodInsnNode min && min.name.equals(t)) {
                min.setOpcode(Opcodes.INVOKESTATIC); min.owner = o; min.name = n; min.desc = d;
            }
        }
    }

    private static void updateAnnotationValue(List<AnnotationNode> anns, String d, String k, Object v) {
        if (anns == null) return;
        for (AnnotationNode a : anns) {
            if (a.desc.equals(d)) {
                if (a.values == null) a.values = new ArrayList<>();
                int idx = -1;
                for (int i = 0; i < a.values.size(); i += 2) if (k.equals(a.values.get(i))) idx = i;
                if (idx != -1) a.values.set(idx + 1, v); else { a.values.add(k); a.values.add(v); }
            }
        }
    }

    private static void buildStaticMethod(ClassNode node, String name, String desc, Consumer<MethodBodyBuilder> body) {
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, desc, null, null);
        MethodBodyBuilder builder = new MethodBodyBuilder() {
            MethodVisitor mv = mn;
            @Override public MethodBodyBuilder loadArg(int i) { mv.visitVarInsn(Opcodes.ALOAD, i); return this; }
            @Override public MethodBodyBuilder pop() { mv.visitInsn(Opcodes.POP); return this; }
            @Override public void returnValue() { mv.visitInsn(Opcodes.RETURN); }
            @Override public MethodBodyBuilder getStaticField(String o, String n, String d) { mv.visitFieldInsn(Opcodes.GETSTATIC, o, n, d); return this; }
            @Override public MethodBodyBuilder invokeVirtual(String o, String n, String d) { mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, o, n, d, false); return this; }
            @Override public MethodBodyBuilder invokeInterface(String o, String n, String d) { mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, o, n, d, true); return this; }
            @Override public MethodBodyBuilder createNewObject(String t, String c, Consumer<MethodBodyBuilder> a) {
                mv.visitTypeInsn(Opcodes.NEW, t); mv.visitInsn(Opcodes.DUP); a.accept(this);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, t, "<init>", c, false); return this;
            }
            @Override public MethodBodyBuilder ifNull(Runnable b) {
                Label end = new Label(); mv.visitJumpInsn(Opcodes.IFNONNULL, end); b.run(); mv.visitLabel(end); return this;
            }
        };
        mn.visitCode(); body.accept(builder); mn.visitMaxs(0, 0); node.methods.add(mn);
    }

    private static void processAnns(List<AnnotationNode> anns, Function<String, String> r) {
        if (anns == null) return;
        for (AnnotationNode a : anns) {
            a.desc = r.apply(a.desc);
            if (a.values != null) for (int i = 1; i < a.values.size(); i += 2) if (a.values.get(i) instanceof String s) a.values.set(i, r.apply(s));
        }
    }
}