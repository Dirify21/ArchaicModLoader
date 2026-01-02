package com.github.dirify21.aml.client;

import com.github.dirify21.aml.classloader.AMLClassLoader;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        for (File file : AMLClassLoader.ARCHAIC_FILES) {
            try (ZipFile zipFile = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;

                    String name = entry.getName();
                    if (name.startsWith("assets/")) {
                        String[] parts = name.split("/", 3);
                        if (parts.length >= 3) {
                            domains.add(parts[1]);
                            resourceCache.put(name.toLowerCase(), new ZipEntrySource(file, name));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        String path = String.format("assets/%s/%s", location.getNamespace(), location.getPath()).toLowerCase();
        ZipEntrySource source = resourceCache.get(path);

        if (source == null) {
            throw new java.io.FileNotFoundException(location.toString());
        }

        ZipFile zipFile = new ZipFile(source.file);
        return zipFile.getInputStream(zipFile.getEntry(source.originalName));
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        String path = String.format("assets/%s/%s", location.getNamespace(), location.getPath()).toLowerCase();
        return resourceCache.containsKey(path);
    }

    @Override
    public Set<String> getResourceDomains() {
        return domains;
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) {
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

    private record ZipEntrySource(File file, String originalName) {
    }
}