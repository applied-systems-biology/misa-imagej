package org.hkijena.misa_imagej.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hkijena.misa_imagej.MISACommand;
import org.hkijena.misa_imagej.utils.FilesystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages MISA++ repositories
 */
public class MISAModuleRepository {

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
        paths.add(FilesystemUtils.getSystemConfigPath().resolve("MISA-ImageJ").resolve("misa-modules"));
    }

    public List<MISAModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public void refresh() {
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
            module.definitionPath = path;
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
