package dev.darrencodes.pcloadletterdb.reading;

import java.util.Objects;

/**
 * Utility functions.
 */
/* default */ final class Util {

    private static final char ELLIPSIS = '\u2026';

    /**
     * Truncates a string to at most numChars characters. If the string is truncated ann ellipsis is added to the end.
     *
     * @param s The String to truncate.
     * @param numChars The maximum number of characters to return. This value should be >= 3.
     * @return The truncated String.
     * @throws IllegalArgumentException If numChars is less than 3.
     */
    /* default */ static String abbreviate(final String s, final int numChars) {

        if (Objects.isNull(s)) {
            return null;
        }

        if (numChars <= 2) {
            throw new IllegalArgumentException("The smallest number of characters this method can abbreviate to is 3.");
        }

        if (s.length() <= numChars) {
            return s;
        }

        return s.substring(0, numChars - 1) + ELLIPSIS;
    }

    private Util() {
        // Intentionally empty.
    }
}
