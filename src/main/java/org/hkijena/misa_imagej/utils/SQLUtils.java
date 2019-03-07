package org.hkijena.misa_imagej.utils;

public class SQLUtils {

    private SQLUtils() {

    }

    public static String escapeStringForSQLite(String s) {
        return s.replace("\\", "\\\\")
                .replace("\b","\\b")
                .replace("\n","\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\\x1A", "\\Z")
                .replace("\\x00", "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }

    public static String escapeWildcardsForSQLite(String s) {
        return escapeStringForSQLite(s)
                .replace("%", "\\%")
                .replace("_","\\_");
    }


}
