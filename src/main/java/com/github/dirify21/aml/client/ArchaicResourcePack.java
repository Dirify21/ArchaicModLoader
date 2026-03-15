package com.github.dirify21.aml.client;

import com.github.dirify21.aml.core.AMLLoadingPlugin;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchaicResourcePack implements IResourcePack {
    private final Map<String, ZipEntrySource> resourceCache = new HashMap<>();
    private final Set<String> domains = new HashSet<>();

    public ArchaicResourcePack() {
        refreshMap();
    }

    public void refreshMap() {
        resourceCache.clear();
        domains.clear();

        for (Path modPath : AMLLoadingPlugin.ARCHAIC_FILES) {
            try (ZipFile zip = new ZipFile(modPath.toFile())) {
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith("assets/") && !name.endsWith("/")) {
                        String[] parts = name.split("/");
                        if (parts.length >= 3) {
                            domains.add(parts[1]);
                            resourceCache.put(name.toLowerCase(Locale.ROOT), new ZipEntrySource(modPath, name));
                        }
                    }
                }
            } catch (IOException e) {
                AMLLoadingPlugin.LOGGER.error("Failed to read {}: {}", modPath, e.getMessage());
            }
        }
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        String path = "assets/%s/%s".formatted(location.getNamespace(), location.getPath()).toLowerCase(Locale.ROOT);
        ZipEntrySource source = resourceCache.get(path);

        if (source == null) throw new FileNotFoundException(location.toString());

        ZipFile zip = new ZipFile(source.path().toFile());
        ZipEntry entry = zip.getEntry(source.originalName());

        if (entry == null) {
            zip.close();
            throw new FileNotFoundException(source.originalName());
        }

        return new FilterInputStream(zip.getInputStream(entry)) {
            @Override
            public void close() throws IOException {
                super.close();
                zip.close();
            }
        };
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        String path = "assets/" + location.getNamespace() + "/" + location.getPath();
        return resourceCache.containsKey(path.toLowerCase(Locale.ROOT));
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.unmodifiableSet(domains);
    }

    @Override
    public @Nullable <T extends IMetadataSection> T getPackMetadata(MetadataSerializer ms, String s) {
        return null;
    }

    @Override
    public BufferedImage getPackImage() {
        return null;
    }

    @Override
    public String getPackName() {
        return "Archaic Resources";
    }

    private record ZipEntrySource(Path path, String originalName) {
    }
}