package com.github.dirify21.aml.client.utils;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientUtils {
    private static final Map<Class<?>, MethodHandle> HANDLE_CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> FAILED_CLASSES = ConcurrentHashMap.newKeySet();

    public static IIcon invokeGetIcon(Object instance, int side, int meta) {
        Class<?> clazz = instance.getClass();
        if (FAILED_CLASSES.contains(clazz)) return null;

        try {
            MethodHandle mh = HANDLE_CACHE.computeIfAbsent(clazz, c -> {
                Method m = findMethod(c);
                if (m == null) return null;
                try {
                    m.setAccessible(true);
                    return MethodHandles.lookup().unreflect(m);
                } catch (Exception e) {
                    return null;
                }
            });

            if (mh == null) {
                FAILED_CLASSES.add(clazz);
                return null;
            }
            if (mh.type().parameterCount() == 2) {
                return (IIcon) mh.invoke(instance, side);
            } else {
                return (IIcon) mh.invoke(instance, side, meta);
            }
        } catch (Throwable e) {
            FAILED_CLASSES.add(clazz);
            return null;
        }
    }

    private static Method findMethod(Class<?> clazz) {
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() == Block.class) continue;
            if (IIcon.class.isAssignableFrom(m.getReturnType())) {
                if (m.getParameterCount() == 2 && m.getParameterTypes()[0] == int.class && m.getParameterTypes()[1] == int.class)
                    return m;
            }
        }
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() == Block.class) continue;
            if (IIcon.class.isAssignableFrom(m.getReturnType())) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == int.class) return m;
            }
        }
        return null;
    }
}