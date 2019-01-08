package org.hkijena.misa_imagej.repository;

import com.google.gson.annotations.SerializedName;
import org.hkijena.misa_imagej.utils.OperatingSystem;
import org.hkijena.misa_imagej.utils.OperatingSystemArchitecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Describes an executable of a MISA++ module and all information that allow to run it
 */
public class MISAExecutable {
    @SerializedName("executable-path")
    public String executablePath;

    @SerializedName("operating-system")
    public OperatingSystem operatingSystem;

    @SerializedName("architecture")
    public OperatingSystemArchitecture operatingSystemArchitecture;

    public MISAExecutable() {

    }

    /**
     * Returns the parameter schema if possible
     * @return The parameter Schema JSON if successful. Otherwise null.
     */
    public String queryParameterSchema() {
        try {
            Path tmppath = Files.createTempFile("MISAParameterSchema", ".json");
//            System.out.println(executablePath + " " + tmppath.toString());
            ProcessBuilder pb = new ProcessBuilder(executablePath, "--write-parameter-schema", tmppath.toString());
            Process p = pb.start();
            if(p.waitFor() == 0) {
                return new String(Files.readAllBytes(tmppath));
            }
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return null;
    }
}
