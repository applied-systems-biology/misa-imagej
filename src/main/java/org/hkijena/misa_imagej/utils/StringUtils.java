package org.hkijena.misa_imagej.utils;

import java.util.Collection;

public class StringUtils {
    private StringUtils() {

    }

    public static String makeUniqueString(String input, Collection<String> existing) {
        if(!existing.contains(input))
            return input;
        int index = 1;
        while(existing.contains(input + " " + index)) {
            ++index;
        }
        return input + " " + index;
    }
}
