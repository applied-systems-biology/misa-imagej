package org.hkijena.misa_imagej.utils;

import org.hkijena.misa_imagej.utils.OSUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class FilesystemUtils {
    public static void addPosixExecutionPermission(Path path) throws IOException {
        if(OSUtils.detect() == OSUtils.OperatingSystem.Linux_x86 || OSUtils.detect() == OSUtils.OperatingSystem.Linux_amd64) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        }
    }
}
