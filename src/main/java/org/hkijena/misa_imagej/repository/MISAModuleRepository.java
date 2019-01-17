package org.hkijena.misa_imagej.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hkijena.misa_imagej.MISACommand;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.OSUtils;
import org.hkijena.misa_imagej.utils.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages MISA++ repositories
 */
public class MISAModuleRepository {

    public static final Path USER_MODULE_PATH = FilesystemUtils.getSystemConfigPath().resolve("MISA-ImageJ").resolve("misa-modules");

    private MISACommand command;

    /**
     * Paths where to look for modules
     */
    private List<Path> paths = new ArrayList<>();
    /**
     * Known modules
     */
    private List<MISAModule> modules = new ArrayList<>();

    public MISAModuleRepository(MISACommand command) {
        this.command = command;
        paths.add(USER_MODULE_PATH);
        if(OSUtils.detectOperatingSystem() == OperatingSystem.Linux) {
            paths.add(Paths.get("/usr/lib/misaxx/modules"));
            paths.add(Paths.get("/usr/local/lib/misaxx/modules"));
        }
    }

    public List<MISAModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public void refresh() {
        modules.clear();
        for(Path path : paths) {
            command.getLogService().info("Checking for MISA++ modules in " + path);
            if(Files.isDirectory(path)) {
                File f = path.toFile();
                File[] files = f.listFiles();
                if(files != null) {
                    for(File entry : files) {
                        tryLoadModule(entry.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void tryLoadModule(String path) {
        command.getLogService().info("Trying to load MISA++ module " + path);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try (InputStreamReader r = new InputStreamReader(new FileInputStream(path))) {
            MISAModule module = gson.fromJson(r, MISAModule.class);
            module.linkPath = path;
            if(module.getModuleInfo() == null) {
                command.getLogService().info("Error: Unable load MISA++ module " + path + " as no module info could be retrieved!");
                return;
            }
            command.getLogService().info("Loaded module information " + module.getModuleInfo().toString());

            // Try to load parameter schema
            if(module.getParameterSchema() == null) {
                command.getLogService().warn("Could not load parameter schema for module " +module.getModuleInfo().toString());
            }

            modules.add(module);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
