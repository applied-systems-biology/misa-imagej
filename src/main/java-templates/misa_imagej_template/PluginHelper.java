package misa_imagej_template;

public class PluginHelper {
    /**
     * Returns true if the code is actually templated as plugin (not running in an IDE for example)
     * @return
     */
    public static boolean isPlugin() {
        return !"${project.artifactId}".contains("{");
    }

    public static String getPluginName() {
        return isPlugin() ? "${project.artifactId}" : "misa_imagej";
    }

    public static String getPluginFullyQualifiedName() {
        return isPlugin() ? "${project.groupId}.${project.artifactId}" : "misa_imagej";
    }
}
