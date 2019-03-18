package org.hkijena.misa_imagej.utils;

import java.util.List;

public class SQLUtils {

    private SQLUtils() {

    }

    public static String concatFilters(List<String> filters, String operator) {
        StringBuilder result = new StringBuilder();
        for(String filter : filters) {
            if(result.length() > 0) {
                result.append(" ").append(operator).append(" ");
            }
            result.append("(").append(filter).append(")");
        }
        return result.toString();
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
