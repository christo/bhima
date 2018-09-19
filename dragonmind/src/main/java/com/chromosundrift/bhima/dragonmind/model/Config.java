package com.chromosundrift.bhima.dragonmind.model;


public class Config {
    private String project;
    private String version;
    /**
     * Closed polygon defined by array of [x,y] points.
     */
    private float[][] cameraMask;
    private PixelMap pixelMap;
    private PixelPusherInfo[] pixelPushers;
    private int brightnessThreshold;
}
