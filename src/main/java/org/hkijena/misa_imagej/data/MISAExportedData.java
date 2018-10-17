package org.hkijena.misa_imagej.data;

import ij.ImagePlus;
import ij.plugin.TextFileReader;
import ij.text.TextWindow;
import net.imagej.Dataset;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.hkijena.misa_imagej.MISADialog;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MISAExportedData extends MISAData {

    private ExportedDataAction exportedDataAction;

    public MISAExportedData(JSONSchemaObject schema) {
        super(schema, DataIOType.Exported);

        // Auto-select exported data action
        switch (getType()) {
            case image_stack:
            case image_file:
            case exportable_meta_data:
                exportedDataAction = ExportedDataAction.Import;
                break;
            default:
                exportedDataAction = ExportedDataAction.Nothing;
        }
    }

    public ExportedDataAction getExportedDataAction() {
        return exportedDataAction;
    }

    public void setExportedDataAction(ExportedDataAction exportedDataAction) {
        this.exportedDataAction = exportedDataAction;
    }


    /**
     * Imports this MISA++ export into ImageJ
     *
     * @param exportedPath
     */
    public void applyImportImageJAction(MISADialog app, Path exportedPath) throws IOException {
        if (exportedDataAction == ExportedDataAction.Import) {
            Path resolvedPath = exportedPath.resolve(getRelativePath()).toAbsolutePath();
            app.getLog().info("Importing " + getType().toString() + " " + getRelativePath().toString());
            switch (getType()) {
                case image_stack: {
                    ImagePlus ip = ij.plugin.FolderOpener.open(resolvedPath.toString(), "virtual");
                    ip.setTitle(getRelativePath().toString());
                    ip.show();
                }
                break;
                case exportable_meta_data: {
                    TextWindow wnd = new TextWindow(resolvedPath.toString(), 400, 450);
                    wnd.setTitle(getRelativePath().toString());
                }
                break;

            }
        }
    }
}
