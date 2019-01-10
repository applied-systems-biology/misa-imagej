package org.hkijena.misa_imagej.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hkijena.misa_imagej.MISACommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Manages MISA++ repositories
 */
public class MISAModuleRepository {

    private MISACommand command;

    /**
     * Paths where to look for modules
     */
    private List<String> paths = new ArrayList<>();
    /**
     * Known modules
     */
    private Map<String, MISAModule> modules = new HashMap<>();

    public MISAModuleRepository(MISACommand command) {
        this.command = command;
        paths.add("/home/rgerst/tmp/misaxx-modules-repository/"); // TODO: Debug
    }

    public Map<String, MISAModule> getModules() {
        return Collections.unmodifiableMap(modules);
    }

    public void refresh() {
        for(String path : paths) {
            command.getLogService().info("Checking for MISA++ modules in " + path);
            File f = new File(path);
            File[] files = f.listFiles();
            if(files != null) {
                for(File entry : files) {
                    tryLoadModule(entry.getAbsolutePath());
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
            command.getLogService().info("Loaded module information " + module.id + " version " + module.version);

            // Try to load parameter schema
            if(module.getParameterSchema() == null) {
                command.getLogService().warn("Could not load parameter schema for module " + module.id + " version " + module.version);
            }

            modules.put(module.id, module);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
