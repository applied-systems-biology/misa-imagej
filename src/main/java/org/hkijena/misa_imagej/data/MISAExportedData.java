package org.hkijena.misa_imagej.data;

import ij.ImagePlus;
import ij.text.TextWindow;
import org.hkijena.misa_imagej.MISADialog;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;

import java.io.IOException;
import java.nio.file.Path;

public class MISAExportedData extends MISAData {

    private MISAExportedDataAction exportedDataAction;

    public MISAExportedData(JSONSchemaObject schema) {
        super(schema, MISADataIOType.Exported);

        // Auto-select exported data action
        switch (getType()) {
            case image_stack:
            case image_file:
            case exportable_meta_data:
                exportedDataAction = MISAExportedDataAction.Import;
                break;
            default:
                exportedDataAction = MISAExportedDataAction.Nothing;
        }
    }

    public MISAExportedDataAction getExportedDataAction() {
        return exportedDataAction;
    }

    public void setExportedDataAction(MISAExportedDataAction exportedDataAction) {
        this.exportedDataAction = exportedDataAction;
    }


    /**
     * Imports this MISA++ export into ImageJ
     *
     * @param exportedPath
     */
    public void applyImportImageJAction(MISADialog app, Path exportedPath) throws IOException {
        if (exportedDataAction == MISAExportedDataAction.Import) {
            Path resolvedPath = exportedPath.resolve(getRelativePath()).toAbsolutePath();
            app.getLogService().info("Importing " + getType().toString() + " " + getRelativePath().toString());
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
