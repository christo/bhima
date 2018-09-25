package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@SuppressWarnings("WeakerAccess")
@JsonInclude(NON_NULL)
@JsonPropertyOrder({"name", "description", "background", "transforms", "pixels"})
public class Segment {

    @JsonProperty("segment")
    private String name;
    private String description;
    private String background;

    @JsonInclude(NON_NULL)
    private List<Transform> transforms = Collections.emptyList();

    private List<PixelPoint> pixels;

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
}
