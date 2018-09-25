package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents an image with an additional alpha channel.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Background {
    private String filename;
    private String colour;
    private float opacity = 1.0f;

    public Background() {
    }

    public Background(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }
}
