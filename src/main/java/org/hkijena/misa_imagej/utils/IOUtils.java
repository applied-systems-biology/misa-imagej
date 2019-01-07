package org.hkijena.misa_imagej.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class IOUtils {

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
}
