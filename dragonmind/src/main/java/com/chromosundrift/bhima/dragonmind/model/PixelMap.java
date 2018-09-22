package com.chromosundrift.bhima.dragonmind.model;

public class PixelMap {
    private String segment;
    private String background;
    private Transform[] transforms;
    private PixelPoint[] pixels;

    public PixelMap(String segment, String background, Transform[] transforms, PixelPoint[] pixels) {
        this.segment = segment;
        this.background = background;
        this.transforms = transforms;
        this.pixels = pixels;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Transform[] getTransforms() {
        return transforms;
    }

    public void setTransforms(Transform[] transforms) {
        this.transforms = transforms;
    }

    public PixelPoint[] getPixels() {
        return pixels;
    }

    public void setPixels(PixelPoint[] pixels) {
        this.pixels = pixels;
    }
}
