package com.github.dirify21.aml.asm.transformer;

import com.github.dirify21.aml.asm.api.AbstractStringRemapper;
import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class FMLTransformer extends AbstractStringRemapper implements IClassTransformer {

    private static final String OLD_FML = "cpw/mods/fml";
    private static final String NEW_FML = "net/minecraftforge/fml";
    private static final String OLD_DOT = "cpw.mods.fml";
    private static final String NEW_DOT = "net.minecraftforge.fml";

    public static byte[] patchArchaicClass(byte[] basicClass) {
        return new FMLTransformer().transform(basicClass);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;

        if (transformedName.equals("net.minecraftforge.fml.common.registry.GameRegistry")) {
            return injectGameRegistryMethods(basicClass);
        }

        if (transformedName.startsWith("com.github.dirify21.aml.")) return basicClass;
        if (!AMLClassLoader.isArchaicClass(transformedName)) return basicClass;

        return super.transform(basicClass);
    }

    @Override
    protected boolean processInstructions(InsnList instructions) {
        boolean changed = false;
        for (AbstractInsnNode insn : instructions.toArray()) {
            if (insn instanceof MethodInsnNode minsn) {
                boolean isTextureMethod = (minsn.name.equals("func_111206_d") || minsn.name.equals("setTextureName"));

                if (isTextureMethod && minsn.desc.contains("Ljava/lang/String;")) {
                    minsn.setOpcode(Opcodes.INVOKESTATIC);
                    minsn.owner = "com/github/dirify21/aml/util/RedirectHelper";
                    minsn.name = "setTextureNameRedirect";
                    minsn.desc = "(Lnet/minecraft/item/Item;Ljava/lang/String;)Lnet/minecraft/item/Item;";
                    changed = true;
                    continue;
                }

                String mOwner = mapInternalName(minsn.owner);
                String mDesc = mapDescriptor(minsn.desc);
                if (!minsn.owner.equals(mOwner)) {
                    minsn.owner = mOwner;
                    changed = true;
                }
                if (!minsn.desc.equals(mDesc)) {
                    minsn.desc = mDesc;
                    changed = true;
                }

            } else if (insn instanceof FieldInsnNode finsn) {
                String mOwner = mapInternalName(finsn.owner);
                String mDesc = mapDescriptor(finsn.desc);
                if (!finsn.owner.equals(mOwner)) {
                    finsn.owner = mOwner;
                    changed = true;
                }
                if (!finsn.desc.equals(mDesc)) {
                    finsn.desc = mDesc;
                    changed = true;
                }
            } else if (insn instanceof TypeInsnNode tinsn) {
                String mType = mapInternalName(tinsn.desc);
                if (!tinsn.desc.equals(mType)) {
                    tinsn.desc = mType;
                    changed = true;
                }
            } else if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String str) {
                String mStr = mapStringValue(str);
                if (!str.equals(mStr)) {
                    ldc.cst = mStr;
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    protected String mapInternalName(String name) {
        return name == null ? null : name.replace(OLD_FML, NEW_FML);
    }

    @Override
    protected String mapDescriptor(String desc) {
        return desc == null ? null : desc.replace(OLD_FML, NEW_FML);
    }

    @Override
    protected String mapStringValue(String value) {
        if (value == null) return null;
        return value.replace(OLD_FML, NEW_FML).replace(OLD_DOT, NEW_DOT);
    }

    @Override
    protected boolean handleSpecialAnnotation(AnnotationNode ann) {
        if (ann.desc.equals("L" + NEW_FML + "/common/Mod;")) {
            if (ann.values == null) ann.values = new ArrayList<>();
            for (int i = 0; i < ann.values.size(); i += 2) {
                if ("acceptedMinecraftVersions".equals(ann.values.get(i))) {
                    ann.values.set(i + 1, "[1.12.2]");
                    return true;
                }
            }
            ann.values.add("acceptedMinecraftVersions");
            ann.values.add("[1.12.2]");
            return true;
        }
        return false;
    }

    private byte[] injectGameRegistryMethods(byte[] basicClass) {
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public void visitEnd() {
                generateRegisterItem(cv);
                super.visitEnd();
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    private void generateRegisterItem(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "registerItem", "(Lnet/minecraft/item/Item;Ljava/lang/String;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "getRegistryName", "()Lnet/minecraft/util/ResourceLocation;", false);
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, l0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/util/ResourceLocation");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/util/ResourceLocation", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/item/Item", "setRegistryName", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/item/IForgeRegistryEntry;", false);
        mv.visitInsn(Opcodes.POP);
        mv.visitLabel(l0);
        mv.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraftforge/fml/common/registry/ForgeRegistries", "ITEMS", "Lnet/minecraftforge/registries/IForgeRegistry;");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraftforge/registries/IForgeRegistry", "register", "(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}