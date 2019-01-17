package org.hkijena.misa_imagej.utils;

import org.hkijena.misa_imagej.utils.OSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FilesystemUtils {
    /**
     * Applies equivalent of chmod +x to the path
     * @param path
     * @throws IOException
     */
    public static void addPosixExecutionPermission(Path path) throws IOException {
        if(OSUtils.detectOperatingSystem() == OperatingSystem.Linux) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        }
    }

    private static void copy(Path source, Path dest) {
        System.out.println("Copying " + source.toString() + " to " + dest.toString());
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void copyFileOrFolder(Path src, Path dest) throws IOException {
        if(src.toFile().isDirectory()) {
            Files.walk(src)
                    .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        }
        else {
            copy(src, dest);
        }
    }

    public static boolean directoryIsEmpty(Path dir) throws IOException {
        File[] entries = dir.toFile().listFiles();
        return entries == null || entries.length == 0;
    }

    /**
     * Returns the configuration path for the current operating system
     * @return
     */
    public static Path getSystemConfigPath() {
        switch (OSUtils.detectOperatingSystem()) {
            case Windows:
                return Paths.get(System.getenv("APPDATA"));
            case Linux:
                if(System.getenv().containsKey("XDG_CONFIG_HOME")) {
                    return Paths.get(System.getenv("XDG_CONFIG_HOME"));
                }
                else {
                    return Paths.get(System.getProperty("user.home")).resolve(".config");
                }
            default:
                throw new UnsupportedOperationException("Unsupported ");
        }
    }
}
