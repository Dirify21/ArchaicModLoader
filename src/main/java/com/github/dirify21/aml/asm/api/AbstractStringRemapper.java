package com.github.dirify21.aml.asm.api;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.List;

public abstract class AbstractStringRemapper {

    protected abstract String mapInternalName(String internalName);

    protected abstract String mapDescriptor(String desc);

    protected abstract String mapStringValue(String value);

    public byte[] transform(byte[] basicClass) {
        if (basicClass == null) return null;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);

        if (processClass(classNode)) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return basicClass;
    }

    protected boolean processClass(ClassNode node) {
        boolean changed = false;

        if (node.superName != null) {
            String mappedSuper = mapInternalName(node.superName);
            if (!mappedSuper.equals(node.superName)) {
                node.superName = mappedSuper;
                changed = true;
            }
        }

        if (node.interfaces != null) {
            for (int i = 0; i < node.interfaces.size(); i++) {
                String itf = node.interfaces.get(i);
                String mappedItf = mapInternalName(itf);
                if (!itf.equals(mappedItf)) {
                    node.interfaces.set(i, mappedItf);
                    changed = true;
                }
            }
        }

        changed |= processAnnotations(node.visibleAnnotations);
        changed |= processAnnotations(node.invisibleAnnotations);

        for (FieldNode field : node.fields) {
            String mDesc = mapDescriptor(field.desc);
            if (!field.desc.equals(mDesc)) {
                field.desc = mDesc;
                changed = true;
            }
            changed |= processAnnotations(field.visibleAnnotations);
        }

        for (MethodNode method : node.methods) {
            String mDesc = mapDescriptor(method.desc);
            if (!method.desc.equals(mDesc)) {
                method.desc = mDesc;
                changed = true;
            }
            changed |= processAnnotations(method.visibleAnnotations);
            changed |= processInstructions(method.instructions);
        }

        return changed;
    }

    protected abstract boolean processInstructions(InsnList instructions);

    protected boolean processAnnotations(List<AnnotationNode> annotations) {
        if (annotations == null) return false;
        boolean changed = false;
        for (AnnotationNode ann : annotations) {
            String mDesc = mapDescriptor(ann.desc);
            if (!ann.desc.equals(mDesc)) {
                ann.desc = mDesc;
                changed = true;
            }
            if (ann.values != null) {
                for (int i = 1; i < ann.values.size(); i += 2) {
                    Object val = ann.values.get(i);
                    if (val instanceof String s) {
                        String mVal = mapStringValue(s);
                        if (!s.equals(mVal)) {
                            ann.values.set(i, mVal);
                            changed = true;
                        }
                    }
                }
            }
            changed |= handleSpecialAnnotation(ann);
        }
        return changed;
    }

    protected boolean handleSpecialAnnotation(AnnotationNode ann) {
        return false;
    }
}