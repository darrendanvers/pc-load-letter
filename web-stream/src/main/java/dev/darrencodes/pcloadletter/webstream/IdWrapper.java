package dev.darrencodes.pcloadletter.webstream;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Simple DAO to hold an ID and text.
 */
@JsonPropertyOrder({"id", "text"})
final class IdWrapper {

    private static final int MAX_STRING_SIZE = 50;

    private long id;
    private String text;

    /**
     * Returns the ID.
     *
     * @return The ID.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets the ID.
     *
     * @param id The ID.
     * @return This object for further configuration.
     */
    public IdWrapper setId(final long id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the text value.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text value.
     *
     * @param text The text value.
     * @return This object for further configuration.
     */
    public IdWrapper setText(final String text) {
        this.text = abbreviate(text);
        return this;
    }

    private static String abbreviate(final String s) {

        if (s.length() <= MAX_STRING_SIZE) {
            return s;
        }

        return s.substring(0, MAX_STRING_SIZE - 3) + "...";
    }
}
