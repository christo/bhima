package com.chromosundrift.bhima.dragonmind.model;


import java.util.List;

public class Config {
    private String project;
    private String version;
    /**
     * Closed polygon defined by array of [x,y] points.
     */
    private float[][] cameraMask;
    private PixelMap pixelMap;
    private List<PixelPusherInfo> pixelPushers;
    private int brightnessThreshold;

    public Config(String project, String version) {
        this.project = project;
        this.version = version;
    }

    public Config save() {
        return null;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public float[][] getCameraMask() {
        return cameraMask;
    }

    public void setCameraMask(float[][] cameraMask) {
        this.cameraMask = cameraMask;
    }

    public PixelMap getPixelMap() {
        return pixelMap;
    }

    public void setPixelMap(PixelMap pixelMap) {
        this.pixelMap = pixelMap;
    }

    public List<PixelPusherInfo> getPixelPushers() {
        return pixelPushers;
    }

    public void setPixelPushers(List<PixelPusherInfo> pixelPushers) {
        this.pixelPushers = pixelPushers;
    }

    public int getBrightnessThreshold() {
        return brightnessThreshold;
    }

    public void setBrightnessThreshold(int brightnessThreshold) {
        this.brightnessThreshold = brightnessThreshold;
    }
}
