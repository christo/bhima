package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.Math.max;
import static java.lang.Math.min;

@SuppressWarnings("WeakerAccess")
@JsonInclude(NON_NULL)
@JsonPropertyOrder({"name", "description", "background", "transforms", "pixels"})
public final class Segment {

    @JsonProperty("segment")
    private String name;
    private String description;
    private String background;
    private boolean enabled = true;
    @JsonInclude(NON_NULL)
    private List<Transform> transforms = Collections.emptyList();
    private List<PixelPoint> pixels;

    /**
     * Get the smallest rectangle that contains all the points.
     *
     * @return a rectangle snugly containing all points (including on the line).
     * @throws IllegalStateException if pixels is empty.
     */
    @JsonIgnore
    public Rect getBoundingBox() throws IllegalStateException {
        if (pixels.isEmpty()) {
            throw new IllegalStateException("There are no points");
        }
        // initialise to the first point
        int minX = pixels.get(0).getX();
        int maxX = minX;
        int minY = pixels.get(0).getY();
        int maxY = minY;

        for (PixelPoint pixel : pixels) {
            Point point = pixel.getPoint();
            minX = min(minX, point.getX());
            maxX = max(maxX, point.getX());
            minY = min(minY, point.getY());
            maxY = max(maxY, point.getY());
        }
        return new Rect(new Point(minX, minY), new Point(maxX, maxY));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public List<PixelPoint> getPixels() {
        return pixels;
    }

    public void setPixels(List<PixelPoint> pixels) {
        this.pixels = pixels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
