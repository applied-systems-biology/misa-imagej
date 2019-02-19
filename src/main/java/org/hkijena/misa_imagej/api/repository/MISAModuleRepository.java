package org.hkijena.misa_imagej.api.repository;

import com.google.gson.Gson;
import org.hkijena.misa_imagej.MISACommand;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.GsonUtils;
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

    private static MISAModuleRepository instance;

    public static final Path USER_MODULE_PATH = FilesystemUtils.getSystemConfigPath().resolve("MISA-ImageJ").resolve("misa-modules");

    /**
     * Paths where to look for modules
     */
    private List<Path> paths = new ArrayList<>();
    /**
     * Known modules
     */
    private List<MISAModule> modules = new ArrayList<>();

    private MISAModuleRepository() {
        paths.add(USER_MODULE_PATH);
        if(System.getenv().containsKey("MISA_MODULE_LINK_PATHS")) {
            for(String path : System.getenv("MISA_MODULE_LINK_PATHS").split(";")) {
                paths.add(Paths.get(path));
            }
        }
        if(OSUtils.detectOperatingSystem() == OperatingSystem.Linux) {
            paths.add(Paths.get("/usr/lib/misaxx/modules"));
            paths.add(Paths.get("/usr/local/lib32/misaxx/modules"));
            paths.add(Paths.get("/usr/local/lib64/misaxx/modules"));
            paths.add(Paths.get("/usr/local/lib/misaxx/modules"));
        }
    }

    public List<MISAModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public MISAModule getModule(String name) {
        Optional<MISAModule> result = modules.stream().filter(misaModule -> misaModule.getModuleInfo().getName().equals(name)).findFirst();
        return result.orElse(null);
    }

    public void refresh() {
        modules.clear();
        for(Path path : paths) {
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
        Gson gson = GsonUtils.getGson();
        try (InputStreamReader r = new InputStreamReader(new FileInputStream(path))) {
            MISAModule module = gson.fromJson(r, MISAModule.class);
            module.linkPath = path;
            if(module.getModuleInfo() == null) {
                return;
            }

            // Try to load parameter schema
            if(module.getParameterSchemaJSON() == null) {
                return;
            }

            modules.add(module);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MISAModuleRepository getInstance() {
        if(instance == null)
            instance = new MISAModuleRepository();
        return instance;
    }
}
