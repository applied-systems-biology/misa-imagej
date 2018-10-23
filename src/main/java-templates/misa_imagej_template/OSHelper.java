package misa_imagej_template;

import java.util.Locale;

public class OSHelper {

    public enum OperatingSystem {
        Linux_x86,
        Linux_amd64,
        Windows_x86,
        Windows_amd64,
        Other
    }

    public static OperatingSystem detect() {
        boolean x64 = System.getProperty("os.arch").endsWith("64");
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if(os.contains("windows")) {
            if(x64)
                return OperatingSystem.Windows_amd64;
            else
                return OperatingSystem.Windows_x86;
        }
        else if(os.contains("linux")) {
            if(x64)
                return OperatingSystem.Linux_amd64;
            else
                return OperatingSystem.Linux_x86;
        }
        else {
            return OperatingSystem.Other;
        }
    }
}
