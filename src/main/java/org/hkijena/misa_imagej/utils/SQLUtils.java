package org.hkijena.misa_imagej.utils;

public class SQLUtils {

    private SQLUtils() {

    }

    public static String column(String s) {
        if(s.contains(" ") || s.contains("-")) {
            return "\"" + s.replace("\\", "\\\\")
                    .replace("\b","\\b")
                    .replace("\n","\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\\x1A", "\\Z")
                    .replace("\\x00", "\\0")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"") + "\"";
        }
        else {
            return s;
        }
    }

    public static String value(String s) {
        return "'" + s.replace("\\", "\\\\")
                .replace("\b","\\b")
                .replace("\n","\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\\x1A", "\\Z")
                .replace("\\x00", "\\0")
                .replace("'", "\\'") + "'";
    }

    public static String escapeWildcardsForSQLite(String s) {
        return s.replace("\\", "\\\\")
                .replace("\b","\\b")
                .replace("\n","\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\\x1A", "\\Z")
                .replace("\\x00", "\\0")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("%", "\\%")
                .replace("_","\\_");
    }


}
