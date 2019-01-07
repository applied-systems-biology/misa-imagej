package org.hkijena.misa_imagej.data.importing;

import org.hkijena.misa_imagej.utils.IOUtils;
import org.hkijena.misa_imagej.data.MISAImportedData;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MISAFileImportSource implements MISADataImportSource {

    private MISAImportedData importedData;
    private Path path;

    public MISAFileImportSource(MISAImportedData importedData, Path path) {
        this.importedData = importedData;
        this.path = path;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public void runImport(Path fullpath, boolean forceCopy) {
        if(forceCopy) {
            try {
                Files.createDirectories(fullpath.getParent());
                IOUtils.copyFileOrFolder(path, fullpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Setting external path is not needed, as the imported directory will get its iternal path set
        }
        else {
            importedData.getSchemaObject().getPropertyFromPath("external-path").setValue(path.toAbsolutePath().toString()); // Just set the internal values accordingly
        }
    }

    /**
     * Creates an appropriate file chooser for the data
     * @param data
     * @return
     */
    public static JFileChooser createFileChooserFor(MISAImportedData data) {
        JFileChooser chooser = new JFileChooser();
        switch (data.getType()) {
            case image_file:
            case file:
            case json_file: {
                chooser.setDialogTitle("Open file");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(true);
            }
            break;
            case generic_file_stack:
            case image_stack: {
                chooser.setDialogTitle("Open folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
            }
            break;
        }
        return chooser;
    }

    @Override
    public MISAImportedData getData() {
        return importedData;
    }
}
