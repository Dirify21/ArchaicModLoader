package com.github.dirify21.aml.mixin.forge;

import com.github.dirify21.aml.asm.transformer.AMLTransformer;
import com.github.dirify21.aml.core.AMLLoadingPlugin;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// Not working with VintageFix JarDiscoverCache right now
@Mixin(value = JarDiscoverer.class)
public class JarDiscovererMixin {

    @Unique
    private static final Set<String> PROCESSED_JARS = ConcurrentHashMap.newKeySet();

    @Redirect(
            method = "findClassesASM",
            at = @At(value = "NEW", target = "(Ljava/io/InputStream;)Lnet/minecraftforge/fml/common/discovery/asm/ASMModParser;")
    )
    private ASMModParser redirectASMModParser(InputStream is) throws IOException {
        byte[] classBytes = is.readAllBytes();

        var modParser = new ASMModParser(new ByteArrayInputStream(classBytes));

        return AMLLoadingPlugin.ARCHAIC_CLASSES.contains(modParser.getASMType().getClassName())
                ? new ASMModParser(new ByteArrayInputStream(AMLTransformer.patchArchaicClass(classBytes))) : modParser;
    }

    @Inject(method = "discover", at = @At("HEAD"))
    private void onDiscoverStart(ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable<List<ModContainer>> cir) {
        var modFile = candidate.getModContainer();
        String jarPath = modFile.getAbsolutePath();

        if (!modFile.isFile() || !jarPath.toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return;
        }

        if (!PROCESSED_JARS.add(jarPath)) {
            return;
        }

        try (var jar = new JarFile(modFile)) {
            var allClassNames = new ArrayList<String>();
            boolean hasModAnnotation = false;

            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                var entry = entries.nextElement();
                String name = entry.getName();

                if (entry.isDirectory() || !name.endsWith(".class")) continue;

                allClassNames.add(name.substring(0, name.length() - 6).replace('/', '.'));

                if (!hasModAnnotation) {
                    try (var is = jar.getInputStream(entry)) {
                        var parser = new ASMModParser(new ByteArrayInputStream(is.readAllBytes()));
                        hasModAnnotation = parser.getAnnotations().stream()
                                .anyMatch(ann -> "cpw.mods.fml.common.Mod".equals(ann.getASMType().getClassName()));
                    } catch (Exception _) {
                    }
                }
            }

            if (hasModAnnotation) {
                AMLLoadingPlugin.ARCHAIC_CLASSES.addAll(allClassNames);
            }

        } catch (IOException e) {
            AMLLoadingPlugin.LOGGER.error("Failed to process JAR: {}", jarPath);
            e.printStackTrace();
        }
    }
}
