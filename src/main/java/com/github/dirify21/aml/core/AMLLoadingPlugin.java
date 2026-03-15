package com.github.dirify21.aml.core;

import com.gihtub.dirify21.aml.Reference;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

public class AMLLoadingPlugin implements IFMLLoadingPlugin {

    public static final Set<String> ARCHAIC_CLASSES = ConcurrentHashMap.newKeySet();
    public static final Set<Path> ARCHAIC_FILES = ConcurrentHashMap.newKeySet();

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    public AMLLoadingPlugin() {
        discoverArchaicMods();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "com.github.dirify21.aml.asm.transformer.AMLTransformer"
        };
    }

    private void discoverArchaicMods() {
        Path modsDir = Path.of("mods");
        if (!Files.exists(modsDir)) return;

        try (var stream = Files.list(modsDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .filter(this::hasArchaicAssets)
                    .forEach(ARCHAIC_FILES::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan mods directory", e);
        }
    }

    private boolean hasArchaicAssets(Path modPath) {
        try (var zip = new ZipFile(modPath.toFile())) {
            return zip.stream()
                    .filter(e -> !e.isDirectory() && e.getName().startsWith("assets/"))
                    .anyMatch(e -> {
                        String name = e.getName().substring(e.getName().lastIndexOf('/') + 1);
                        return name.toLowerCase().endsWith(".png") && !name.equals(name.toLowerCase());
                    });
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}