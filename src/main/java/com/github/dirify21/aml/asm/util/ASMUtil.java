package com.github.dirify21.aml.asm.util;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class ASMUtil {
    private ASMUtil() {
    }

    public static Transformer builder(byte[] initialClass) {
        return new Transformer(initialClass);
    }

    private static AnnotationVisitor wrapAnnotationVisitor(AnnotationVisitor av, String currentDesc, String targetDesc, String key, Function<Object, Object> mapper) {
        if (currentDesc.equals(targetDesc)) {
            return new AnnotationValueModifier(av, key, mapper);
        }
        return av;
    }

    @FunctionalInterface
    public interface TransformerRule {
        byte[] accept(byte[] input) throws Exception;
    }

    public static class Transformer {
        private byte[] data;

        private Transformer(byte[] data) {
            this.data = data;
        }

        public Transformer apply(TransformerRule rule) {
            try {
                this.data = rule.accept(this.data);
            } catch (Exception e) {
                throw new RuntimeException("Error applying TransformerRule", e);
            }
            return this;
        }

        public Transformer transformNode(Consumer<ClassNode> action) {
            ClassReader reader = new ClassReader(data);
            ClassNode node = new ClassNode(Opcodes.ASM9);
            reader.accept(node, 0);

            action.accept(node);

            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
                @Override
                protected String getCommonSuperClass(String type1, String type2) {
                    try {
                        return super.getCommonSuperClass(type1, type2);
                    } catch (Exception e) {
                        return "java/lang/Object";
                    }
                }
            };
            node.accept(writer);
            this.data = writer.toByteArray();
            return this;
        }

        public Transformer remap(String oldName, String newName) {
            ClassReader reader = new ClassReader(data);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassRemapper remapper = new ClassRemapper(writer, new PackageRemapper(oldName, newName));
            reader.accept(remapper, 0);
            this.data = writer.toByteArray();
            return this;
        }

        public Transformer renameMethod(String oldName, String newName) {
            ClassReader reader = new ClassReader(data);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            Remapper methodRemapper = new Remapper(Opcodes.ASM9) {
                @Override
                public String mapMethodName(String owner, String name, String descriptor) {
                    return name.equals(oldName) ? newName : name;
                }
            };
            reader.accept(new ClassRemapper(writer, methodRemapper), 0);
            this.data = writer.toByteArray();
            return this;
        }

        public Transformer redirectFieldToMethod(String targetOwner, String targetName, String newOwner, String newMethod, String newDesc) {
            ClassReader reader = new ClassReader(data);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, sig, ex)) {
                        @Override
                        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                            if (owner.equals(targetOwner) && name.equals(targetName)) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, newOwner, newMethod, newDesc, false);
                            } else {
                                super.visitFieldInsn(opcode, owner, name, descriptor);
                            }
                        }
                    };
                }
            }, 0);

            this.data = writer.toByteArray();
            return this;
        }

        public Transformer modifyAnnotationValue(String targetDesc, String key, Function<Object, Object> mapper) {
            ClassReader reader = new ClassReader(data);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return wrapAnnotationVisitor(super.visitAnnotation(desc, visible), desc, targetDesc, key, mapper);
                }

                @Override
                public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                    return new FieldVisitor(Opcodes.ASM9, super.visitField(access, name, desc, signature, value)) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String d, boolean v) {
                            return wrapAnnotationVisitor(super.visitAnnotation(d, v), d, targetDesc, key, mapper);
                        }
                    };
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, sig, ex)) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String d, boolean v) {
                            return wrapAnnotationVisitor(super.visitAnnotation(d, v), d, targetDesc, key, mapper);
                        }

                        @Override
                        public AnnotationVisitor visitParameterAnnotation(int param, String d, boolean v) {
                            return wrapAnnotationVisitor(super.visitParameterAnnotation(param, d, v), d, targetDesc, key, mapper);
                        }
                    };
                }
            };

            reader.accept(cv, 0);
            this.data = writer.toByteArray();
            return this;
        }

        public byte[] build() {
            return this.data;
        }
    }

    private static class PackageRemapper extends Remapper {
        private final String oldPackage;
        private final String newPackage;

        public PackageRemapper(String oldPackage, String newPackage) {
            super(Opcodes.ASM9);
            this.oldPackage = oldPackage;
            this.newPackage = newPackage;
        }

        @Override
        public String map(String internalName) {
            if (internalName.startsWith(oldPackage)) {
                return newPackage + internalName.substring(oldPackage.length());
            }
            return internalName;
        }
    }

    private static class AnnotationValueModifier extends AnnotationVisitor {
        private final String targetKey;
        private final Function<Object, Object> mapper;

        public AnnotationValueModifier(AnnotationVisitor av, String key, Function<Object, Object> mapper) {
            super(Opcodes.ASM9, av);
            this.targetKey = key;
            this.mapper = mapper;
        }

        @Override
        public void visit(String name, Object value) {
            if (name != null && name.equals(targetKey)) {
                Object newValue = mapper.apply(value);
                super.visit(name, newValue);
            } else {
                super.visit(name, value);
            }
        }
    }
}
