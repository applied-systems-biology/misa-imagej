package misa_imagej_template.data.importing;

import ij.*;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;
import ij.process.LUT;
import misa_imagej_template.data.MISADataType;
import misa_imagej_template.data.MISAImportedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Imports data from a window
 */
public class MISAImageJStackImportSource implements MISADataImportSource {

    private static String[] choices = new String[]{"BMP", "FITS", "GIF", "JPEG", "PGM", "PNG", "Raw", "Text", "TIFF", "ZIP"};

    private MISAImportedData importedData;

    private ImagePlus imp;
    private String name;
    private String fileType = "TIFF";
    private int ndigits = 4;
    private boolean useLabels = true;
    private boolean firstTime = true;
    private int startAt;
    private boolean hyperstack;
    private int[] dim;

    public MISAImageJStackImportSource(MISAImportedData importedData, ImagePlus imp) {
        this.importedData = importedData;
        this.imp = imp;
        this.hyperstack = imp.isHyperStack();
        this.name = imp.getTitle();
    }

    /**
     * Creates a stack writer (using a Dialog) for given image
     *
     * @return
     */
    public static MISAImageJStackImportSource createWithDialog(MISAImportedData importedData, ImagePlus imp) {

        if (imp == null || imp.getStackSize() <= 1)
            return null;

        int stackSize = imp.getStackSize();

        MISAImageJStackImportSource result = new MISAImageJStackImportSource(importedData, imp);

        GenericDialog gd = new GenericDialog("Save Image Sequence");

        gd.addChoice("Format:", choices, result.fileType);
        gd.addStringField("Name:", imp.getTitle(), 12);
        if (!result.hyperstack) {
            gd.addNumericField("Start At:", (double) result.startAt, 0);
        }

        gd.addNumericField("Digits (1-8):", (double) result.ndigits, 0);
        if (!result.hyperstack) {
            gd.addCheckbox("Use slice labels as file names", result.useLabels);
        }

        gd.setSmartRecording(true);
        gd.showDialog();

        if (!gd.wasCanceled()) {
            result.fileType = gd.getNextChoice();

            result.name = gd.getNextString();
            if (!result.hyperstack) {
                result.startAt = (int) gd.getNextNumber();
            }

            if (result.startAt < 0) {
                result.startAt = 0;
            }

            result.ndigits = (int) gd.getNextNumber();
            if (!result.hyperstack) {
                result.useLabels = gd.getNextBoolean();
            } else {
                result.useLabels = false;
            }

            if (result.ndigits < 1) {
                result.ndigits = 1;
            }

            if (result.ndigits > 8) {
                result.ndigits = 8;
            }

            int maxImages = (int) Math.pow(10.0D, (double) result.ndigits);
            if (stackSize > maxImages && !result.useLabels && !result.hyperstack) {
                IJ.error("Stack Writer", "More than " + result.ndigits + " digits are required to generate \nunique file names for " + stackSize + " images.");
                return null;
            }

            return result;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[ImageJ] " + imp.getTitle();
    }

    @Override
    public void runImport(Path importedDirectory, boolean forcedCopy) {

        try {
            Files.createDirectories(importedDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Code copied from ij.plugin.StackWriter
        int number = 0;
        int stackSize = imp.getStackSize();
        String name = imp.getTitle();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex >= 0) {
            name = name.substring(0, dotIndex);
        }

        LUT[] luts = null;
        int lutIndex = 0;
        int nChannels = imp.getNChannels();
        if (this.hyperstack) {
            this.dim = imp.getDimensions();
            if (imp.isComposite()) {
                luts = ((CompositeImage) imp).getLuts();
            }

            if (this.firstTime && this.ndigits == 4) {
                this.ndigits = 3;
                this.firstTime = false;
            }
        }

        int maxImages = (int) Math.pow(10.0D, (double) this.ndigits);
        if (stackSize > maxImages && !this.useLabels && !this.hyperstack) {
            IJ.error("Stack Writer", "More than " + this.ndigits + " digits are required to generate \nunique file names for " + stackSize + " images.");
        } else {
            String format = this.fileType.toLowerCase(Locale.US);
            if (!format.equals("gif") || FileSaver.okForGif(imp)) {
                if (!format.equals("fits") || FileSaver.okForFits(imp)) {
                    if (format.equals("text")) {
                        format = "text image";
                    }

                    String extension = "." + format;
                    if (format.equals("tiff")) {
                        extension = ".tif";
                    } else if (format.equals("text image")) {
                        extension = ".txt";
                    }

                    String directory = importedDirectory.toString() + File.separator;
                    boolean isOverlay;

                    Overlay overlay = imp.getOverlay();
                    isOverlay = overlay != null && !imp.getHideOverlay();
                    if (!format.equals("jpeg") && !format.equals("png")) {
                        isOverlay = false;
                    }

                    ImageStack stack = imp.getStack();
                    ImagePlus imp2 = new ImagePlus();
                    imp2.setTitle(imp.getTitle());
                    Calibration cal = imp.getCalibration();
                    int nSlices = stack.getSize();
                    String label = null;
                    imp.lock();

                    for (int i = 1; i <= nSlices; ++i) {
                        IJ.showStatus("writing: " + i + "/" + nSlices);
                        IJ.showProgress(i, nSlices);
                        ImageProcessor ip = stack.getProcessor(i);
                        if (isOverlay) {
                            imp.setSliceWithoutUpdate(i);
                            ip = imp.flatten().getProcessor();
                        } else if (luts != null && nChannels > 1 && this.hyperstack) {
                            ip.setColorModel(luts[lutIndex++]);
                            if (lutIndex >= luts.length) {
                                lutIndex = 0;
                            }
                        }

                        imp2.setProcessor((String) null, ip);
                        String label2 = stack.getSliceLabel(i);
                        if (label2 != null && label2.indexOf("\n") != -1) {
                            imp2.setProperty("Info", label2);
                        } else {
                            Properties props = imp2.getProperties();
                            if (props != null) {
                                props.remove("Info");
                            }
                        }

                        imp2.setCalibration(cal);
                        String digits = this.getDigits(number++);
                        if (this.useLabels) {
                            label = stack.getShortSliceLabel(i);
                            if (label != null && label.equals("")) {
                                label = null;
                            }

                            if (label != null) {
                                label = label.replaceAll("/", "-");
                            }
                        }

                        String path;
                        if (label == null) {
                            path = directory + name + digits + extension;
                        } else {
                            path = directory + label + extension;
                        }

                        if (i == 1) {
                            File f = new File(path);
                            if (f.exists() && !IJ.isMacro() && !IJ.showMessageWithCancel("Overwrite files?", "One or more files will be overwritten if you click \"OK\".\n \n" + path)) {
                                imp.unlock();
                                IJ.showStatus("");
                                IJ.showProgress(1.0D);
                                return;
                            }
                        }

                        if (Recorder.record) {
                            Recorder.disablePathRecording();
                        }

                        imp2.setOverlay((Overlay) null);
                        if (overlay != null && format.equals("tiff")) {
                            Overlay overlay2 = overlay.duplicate();
                            overlay2.crop(i, i);
                            if (overlay2.size() > 0) {
                                for (int j = 0; j < overlay2.size(); ++j) {
                                    Roi roi = overlay2.get(j);
                                    int pos = roi.getPosition();
                                    if (pos == 1) {
                                        roi.setPosition(i);
                                    }
                                }

                                imp2.setOverlay(overlay2);
                            }
                        }

                        IJ.saveAs(imp2, format, path);
                    }

                    imp.unlock();
                    if (isOverlay) {
                        imp.setSlice(1);
                    }

                    IJ.showStatus("");
                }
            }
        }
    }

    private String getDigits(int n) {
        if (this.hyperstack) {
            int c = n % this.dim[2] + 1;
            int z = n / this.dim[2] % this.dim[3] + 1;
            int t = n / (this.dim[2] * this.dim[3]) % this.dim[4] + 1;
            String cs = "";
            String zs = "";
            String ts = "";
            if (this.dim[2] > 1) {
                cs = "00000000" + c;
                cs = "_c" + cs.substring(cs.length() - this.ndigits);
            }

            if (this.dim[3] > 1) {
                zs = "00000000" + z;
                zs = "_z" + zs.substring(zs.length() - this.ndigits);
            }

            if (this.dim[4] > 1) {
                ts = "00000000" + t;
                ts = "_t" + ts.substring(ts.length() - this.ndigits);
            }

            return ts + zs + cs;
        } else {
            String digits = "00000000" + (this.startAt + n);
            return digits.substring(digits.length() - this.ndigits);
        }
    }

    @Override
    public MISAImportedData getData() {
        return importedData;
    }

    /**
     * Returns true if the ImagePlus is compatible with the imported data
     *
     * @param importedData
     * @param image
     * @return
     */
    public static boolean canHold(MISAImportedData importedData, ImagePlus image) {
        return importedData.getType() == MISADataType.image_stack && image != null && image.getStackSize() >= 2;
    }
}
