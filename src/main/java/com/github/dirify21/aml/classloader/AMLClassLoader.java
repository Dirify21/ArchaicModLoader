package com.github.dirify21.aml.classloader;

import com.github.dirify21.aml.AMLMod;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AMLClassLoader {

    public static final List<File> ARCHAIC_FILES = Collections.synchronizedList(new ArrayList<>());
    public static final Set<String> ARCHAIC_CLASSES = Collections.synchronizedSet(new HashSet<>());
    private static final File ARCHAIC_DIR = new File("mods/archaic");

    public static void init() {
        if (!ARCHAIC_DIR.exists()) {
            if (ARCHAIC_DIR.mkdirs()) {
                AMLMod.LOGGER.info("Created mods/archaic directory.");
            }
        }

        File[] files = ARCHAIC_DIR.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".zip"));

        if (files == null) return;

        for (File file : files) {
            try {
                indexAndInject(file);
            } catch (Exception e) {
                AMLMod.LOGGER.error("Failed to load archaic jar: {}", file.getName(), e);
            }
        }
    }

    private static void indexAndInject(File file) throws IOException {
        Launch.classLoader.addURL(file.toURI().toURL());

        ARCHAIC_FILES.add(file);

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.')
                            .substring(0, entryName.length() - 6);
                    ARCHAIC_CLASSES.add(className);
                }
            }
        }
    }

    public static boolean isArchaicClass(String className) {
        return ARCHAIC_CLASSES.contains(className);
    }
}