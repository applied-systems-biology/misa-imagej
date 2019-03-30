package org.hkijena.misa_imagej.extension.datasources;

import com.google.common.io.MoreFiles;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISADataSource;
import org.hkijena.misa_imagej.api.MISAValidityReport;
import org.hkijena.misa_imagej.utils.FilesystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MISAFolderLinkDataSource implements MISADataSource {

    private Path sourceFolder;
    private MISACache cache;

    public MISAFolderLinkDataSource(MISACache cache) {
        this.cache = cache;
    }

    @Override
    public void install(Path installFolder, boolean forceCopy) {
        if(forceCopy) {
            try {
                Files.createDirectories(installFolder);
                FilesystemUtils.copyFileOrFolder(sourceFolder, installFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                MoreFiles.createParentDirectories(installFolder);
                Files.deleteIfExists(installFolder);
                FilesystemUtils.createSymbolicLinkOrCopy(installFolder, getSourceFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    public MISACache getCache() {
        return cache;
    }

    @Override
    public MISAValidityReport getValidityReport() {
        return new MISAValidityReport();
    }

    public Path getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(Path sourceFolder) {
        this.sourceFolder = sourceFolder;
    }
}
