package com.chromosundrift.bhima.dragonmind.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {

    private final static String DEFAULT_CONFIG_FILE = "dragonmind.config.json";
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

    public Config save() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(DEFAULT_CONFIG_FILE), this);
        return this;
    }

    public static Config load() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File("target/json_car.json"), Config.class);
    }

    String unParse() throws ConfigException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ConfigException(e);
        }
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
