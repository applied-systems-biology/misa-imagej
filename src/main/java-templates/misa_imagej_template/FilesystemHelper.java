package misa_imagej_template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class FilesystemHelper {
    public static void addPosixExecutionPermission(Path path) throws IOException {
        if(OSHelper.detect() == OSHelper.OperatingSystem.Linux_x86 || OSHelper.detect() == OSHelper.OperatingSystem.Linux_amd64) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        }
    }
}
