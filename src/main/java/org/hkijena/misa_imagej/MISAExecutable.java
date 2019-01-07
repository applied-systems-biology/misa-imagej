package org.hkijena.misa_imagej;

import com.google.common.base.Charsets;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.OSUtils;
import org.hkijena.misa_imagej.utils.ResourceUtils;
import org.hkijena.misa_imagej.utils.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MISAExecutable {

    private OSUtils.OperatingSystem operatingSystem;

    public MISAExecutable(OSUtils.OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the base name of the executable. The executables contained in the resources should be consistent with it
     * @return
     */
    public static String getExecutableBaseName() {
        // Do not change this code even if the IDE tells you.
        // This code is in here to allow debugging of the application in IDEA
        if("${project.artifactId}".contains("{")) {
            return "misa_imagej";
        }
        else {
            return "${project.artifactId}";
        }
    }

    private static void tryAddExecutable(OSUtils.OperatingSystem os, Map<OSUtils.OperatingSystem, MISAExecutable> result) {
        if(ResourceUtils.getPluginResource(os.name().toString().toLowerCase(Locale.ENGLISH) + ".zip") != null) {
            result.put(os, new MISAExecutable(os));
        }
    }

    public static MISAExecutable getBestMatchingExecutable() {
        OSUtils.OperatingSystem os = OSUtils.detect();
        Map<OSUtils.OperatingSystem, MISAExecutable> available = getAvailableExecutables();
        if(os == OSUtils.OperatingSystem.Linux_amd64 || os == OSUtils.OperatingSystem.Linux_x86) {
            if(os == OSUtils.OperatingSystem.Linux_amd64 && available.containsKey(OSUtils.OperatingSystem.Linux_amd64)) {
                return available.get(OSUtils.OperatingSystem.Linux_amd64);
            }
            return available.get(OSUtils.OperatingSystem.Linux_x86);
        }
        else if(os == OSUtils.OperatingSystem.Windows_amd64 || os == OSUtils.OperatingSystem.Windows_x86) {
            if(os == OSUtils.OperatingSystem.Windows_amd64 && available.containsKey(OSUtils.OperatingSystem.Windows_amd64)) {
                return available.get(OSUtils.OperatingSystem.Windows_amd64);
            }
            return available.get(OSUtils.OperatingSystem.Windows_x86);
        }
        else {
            throw new RuntimeException("Unsupported operating system!");
        }
    }

    public static Map<OSUtils.OperatingSystem, MISAExecutable> getAvailableExecutables() {
        Map<OSUtils.OperatingSystem, MISAExecutable> result = new HashMap<>();
        tryAddExecutable(OSUtils.OperatingSystem.Windows_amd64, result);
        tryAddExecutable(OSUtils.OperatingSystem.Windows_x86, result);
        tryAddExecutable(OSUtils.OperatingSystem.Linux_amd64, result);
        tryAddExecutable(OSUtils.OperatingSystem.Linux_x86, result);
        return result;
    }

    /**
     * Installs the executable into the root directory and returns the path of the executable
     * @param rootPath
     * @param withRunner Create a standard runner that runs the executable with
     * @param runnerParameters
     * @return Path of the executable
     * @throws IOException
     */
    public Path install(Path rootPath, boolean withRunner, String runnerParameters) throws IOException {
        Path executablePath = rootPath.resolve(getName()).resolve(getExecutableName());
        Path installPath = rootPath.resolve(getName());
        Files.createDirectories(installPath);

        // Unpack the zip file
        ZipUtils.unzip(ResourceUtils.getPluginResourceAsStream(getResourcePath()), installPath.toFile());
        FilesystemUtils.addPosixExecutionPermission(executablePath);

        // Install runner if enabled
        if(withRunner) {
            switch(operatingSystem) {
                case Linux_amd64:
                case Linux_x86: {
                    String content = "#!/bin/bash\n" +
                            "./" + getName() + "/" + getExecutableName() + " " + runnerParameters;
                    Files.write(rootPath.resolve(getRunnerScriptName()), content.getBytes(Charsets.UTF_8));
                    FilesystemUtils.addPosixExecutionPermission(rootPath.resolve(getRunnerScriptName()));
                }
                break;
                case Windows_x86:
                case Windows_amd64: {
                    String content = "@echo off\r\n" +
                            ".\\" + getName() + "\\" + getExecutableName() + " " + runnerParameters;
                    Files.write(rootPath.resolve(getRunnerScriptName()), content.getBytes(Charsets.UTF_8));
                }
                break;
                default:
                    throw new RuntimeException("Unsupported operating system!");
            }
        }

        return executablePath;
    }

    /**
     * Returns the name of the script that runs the executable
     * @return
     */
    public String getRunnerScriptName() {
        switch(operatingSystem) {
            case Linux_amd64:
            case Linux_x86:
                return getName() + ".sh";
            case Windows_x86:
            case Windows_amd64:
                return getName() + ".bat";
            default:
                throw new RuntimeException("Unsupported operating system!");
        }
    }

    /**
     * Returns the name of the executable
     * @return
     */
    public String getExecutableName() {
        switch(operatingSystem) {
            case Linux_amd64:
            case Linux_x86:
                return getExecutableBaseName();
            case Windows_x86:
            case Windows_amd64:
                return getExecutableBaseName() + ".exe";
            default:
                throw new RuntimeException("Unsupported operating system!");
        }
    }

    public String getName() {
        return operatingSystem.name().toLowerCase(Locale.ENGLISH);
    }

    public String getResourcePath() {
        return "/" + getName() + ".zip";
    }
}
