package misa_imagej_template;

import java.io.InputStream;
import java.net.URL;

/**
 * Special functions needed to work together with other MISA++ plugins
 */
public class ResourceHelper {

    public static String getResourceBasePath() {
        return "/" + PluginHelper.getPluginFullyQualifiedName();
    }

    public static String getResourcePath(String internalResourcePath) {
        if(internalResourcePath.startsWith("/"))
            internalResourcePath = internalResourcePath.substring(1);
        return getResourceBasePath() + "/" + internalResourcePath;
    }

    public static URL getPluginResource(String internalResourcePath) {
        return ResourceHelper.class.getResource(getResourcePath(internalResourcePath));
    }

    public static InputStream getPluginResourceAsStream(String internalResourcePath) {
        return ResourceHelper.class.getResourceAsStream(getResourcePath(internalResourcePath));
    }

}
