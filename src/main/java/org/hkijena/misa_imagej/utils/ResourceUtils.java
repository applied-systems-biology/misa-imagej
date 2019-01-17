package org.hkijena.misa_imagej.utils;

import java.io.InputStream;
import java.net.URL;

/**
 * Special functions needed to work together with other MISA++ plugins
 */
public class ResourceUtils {

    public static String getResourceBasePath() {
        return "/org/hkijena/misa_imagej";
    }

    public static String getResourcePath(String internalResourcePath) {
        if(internalResourcePath.startsWith("/"))
            internalResourcePath = internalResourcePath.substring(1);
        return getResourceBasePath() + "/" + internalResourcePath;
    }

    public static URL getPluginResource(String internalResourcePath) {
        return ResourceUtils.class.getResource(getResourcePath(internalResourcePath));
    }

    public static InputStream getPluginResourceAsStream(String internalResourcePath) {
        return ResourceUtils.class.getResourceAsStream(getResourcePath(internalResourcePath));
    }

}
