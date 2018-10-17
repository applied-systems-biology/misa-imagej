package org.hkijena.misa_imagej;

import com.google.common.io.ByteStreams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MISAExecutable {

    private String resourcePath;

    public MISAExecutable(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    private static void tryAddExecutable(OSHelper.OperatingSystem os, String resourcePath, Map<OSHelper.OperatingSystem, MISAExecutable> result) {
        if(MISAExecutable.class.getResource(resourcePath) != null) {
            result.put(os, new MISAExecutable(resourcePath));
        }
    }

    public static MISAExecutable getBestMatchingExecutable() {
        OSHelper.OperatingSystem os = OSHelper.detect();
        Map<OSHelper.OperatingSystem, MISAExecutable> available = getAvailableExecutables();
        if(os == OSHelper.OperatingSystem.Linux_amd64 || os == OSHelper.OperatingSystem.Linux_x86) {
            if(os == OSHelper.OperatingSystem.Linux_amd64 && available.containsKey(OSHelper.OperatingSystem.Linux_amd64)) {
                return available.get(OSHelper.OperatingSystem.Linux_amd64);
            }
            return available.get(OSHelper.OperatingSystem.Linux_x86);
        }
        else if(os == OSHelper.OperatingSystem.Windows_amd64 || os == OSHelper.OperatingSystem.Windows_x86) {
            if(os == OSHelper.OperatingSystem.Windows_amd64 && available.containsKey(OSHelper.OperatingSystem.Windows_amd64)) {
                return available.get(OSHelper.OperatingSystem.Windows_amd64);
            }
            return available.get(OSHelper.OperatingSystem.Windows_x86);
        }
        else {
            throw new RuntimeException("Unsupported operating system!");
        }
    }

    public static Map<OSHelper.OperatingSystem, MISAExecutable> getAvailableExecutables() {
        Map<OSHelper.OperatingSystem, MISAExecutable> result = new HashMap<>();
        tryAddExecutable(OSHelper.OperatingSystem.Windows_amd64, "/windows_amd64.exe", result);
        tryAddExecutable(OSHelper.OperatingSystem.Windows_x86, "/windows_x86.exe", result);
        tryAddExecutable(OSHelper.OperatingSystem.Linux_amd64, "/linux_amd64", result);
        tryAddExecutable(OSHelper.OperatingSystem.Linux_x86, "/linux_x86", result);
        return result;
    }

    public void writeToFile(Path path) throws IOException {
        try(InputStream inputStream = MISAExecutable.class.getResourceAsStream(resourcePath)) {
            try(FileOutputStream outputStream = new FileOutputStream(path.toString())) {
                ByteStreams.copy(inputStream, outputStream);
            }
        }
        if(OSHelper.detect() == OSHelper.OperatingSystem.Linux_x86 || OSHelper.detect() == OSHelper.OperatingSystem.Linux_amd64) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        }
    }

    public Path getName() {
        return Paths.get(resourcePath).getFileName();
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
