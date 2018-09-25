package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents an image with an additional alpha channel.
 */
@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Background {
    private String filename;
    private String colour;
    private float opacity = 1.0f;
    private List<Transform> transforms;

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

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transform> transforms) {
        this.transforms = transforms;
    }
}
