package com.pcb.pcbridge.framework.helpers;

import java.util.Arrays;
import java.util.Optional;

public class StringHelpers {

    /**
     * Converts a string of 'on' or 'off'
     * to a boolean. If neither, returns
     * an empty optional.
     *
     * @param string
     * @return boolean
     */
    public static Optional<Boolean> getBooleanFromString(String string) {
        String[] options = { "on", "off", "true", "false" };
        boolean isValidOption = Arrays.asList(options).contains(string.toLowerCase());

        if(!isValidOption) {
            return Optional.empty();
        }

        boolean value = string.equalsIgnoreCase("on") || string.equalsIgnoreCase("true");
        return Optional.of(value);
    }

}
