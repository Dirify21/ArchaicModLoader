package com.github.dirify21.aml.mixin.mixins.minecraft;

import com.github.dirify21.aml.asm.transformer.AMLTransformer;
import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import org.objectweb.asm.ClassReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Mixin(value = JarDiscoverer.class, remap = false)
public abstract class JarDiscovererMixin {

    @Redirect(
            method = "findClassesASM",
            at = @At(value = "NEW", target = "net/minecraftforge/fml/common/discovery/asm/ASMModParser")
    )
    private ASMModParser redirectASMModParser(InputStream is) throws IOException {
        byte[] originalBytes = readStream(is);

        ClassReader cr = new ClassReader(originalBytes);
        String internalName = cr.getClassName();
        String dotName = internalName.replace('/', '.');

        if (AMLClassLoader.isArchaicClass(dotName)) {
            byte[] patchedBytes = AMLTransformer.patchArchaicClass(originalBytes);
            return new ASMModParser(new ByteArrayInputStream(patchedBytes));
        }

        return new ASMModParser(new ByteArrayInputStream(originalBytes));
    }

    private byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}