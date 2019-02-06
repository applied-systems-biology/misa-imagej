package org.hkijena.misa_imagej.api.datasources;

import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.MISAParameterValidity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MISAFolderLinkDataSource implements MISADataSource {

    private Path sourceFolder;

    public MISAFolderLinkDataSource(Path sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    @Override
    public void install(Path installFolder, boolean forceCopy) {
        try {
            Files.deleteIfExists(installFolder);
            Files.createSymbolicLink(installFolder, sourceFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Folder link";
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public MISAParameterValidity isValidParameter() {
        return new MISAParameterValidity();
    }

    public Path getSourceFolder() {
        return sourceFolder;
    }
}
