package org.hkijena.misa_imagej.utils;

import java.util.Locale;

public class OSUtils {

    public static OperatingSystem detectOperatingSystem() {
        boolean x64 = System.getProperty("os.arch").endsWith("64");
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if(os.contains("windows")) {
            return OperatingSystem.Windows;
        }
        else if(os.contains("linux")) {
            return OperatingSystem.Linux;
        }
        else {
            return OperatingSystem.Unknown;
        }
    }

    public static OperatingSystemArchitecture detectArchitecture() {
        if(System.getProperty("os.arch").endsWith("64"))
            return OperatingSystemArchitecture.x64;
        else
            return OperatingSystemArchitecture.x32;
    }

    public static boolean isCompatible(OperatingSystem system, OperatingSystemArchitecture architecture, OperatingSystem targetSystem, OperatingSystemArchitecture targetArchitecture) {
        if(system != targetSystem)
            return false;
        return architecture == targetArchitecture || (architecture == OperatingSystemArchitecture.x64 && targetArchitecture == OperatingSystemArchitecture.x32);
    }
}
