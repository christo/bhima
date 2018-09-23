package com.chromosundrift.bhima.dragonmind.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@SuppressWarnings("WeakerAccess")
@JsonInclude(NON_NULL)
@JsonPropertyOrder({"project", "version", "brightnessThreshold", "cameraMask", "pixelPushers"})
public class Config {

    private final static String DEFAULT_CONFIG_FILE = "dragonmind.config.json";
    private String project;
    private String version;
    /**
     * Closed polygon defined by array of [x,y] points.
     */
    private float[][] cameraMask;
    private List<Segment> pixelMap;

    private List<PixelPusherInfo> pixelPushers;
    private int brightnessThreshold;

    public Config() {
    }

    public Config(@JsonProperty("project") String project, @JsonProperty("version") String version) {
        this.project = project;
        this.version = version;
    }

    public Config save() throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        objectMapper.writeValue(new File(DEFAULT_CONFIG_FILE), this);
        return this;
    }

    public static Config load() throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.readValue(new File(DEFAULT_CONFIG_FILE), Config.class);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ConfigModule", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(PixelPoint.class, new PixelPointDeserializer(PixelPoint.class));
        module.addSerializer(PixelPoint.class, new PixelPointSerializer(PixelPoint.class));
        mapper.registerModule(module);
        return mapper;
    }

    String unParse(boolean pretty) throws ConfigException {
        ObjectMapper mapper = getObjectMapper();
        try {
            if (pretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } else {
                return mapper.writeValueAsString(this);
            }
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

    public List<Segment> getSegments() {
        return pixelMap;
    }

    public void setPixelMap(List<Segment> segments) {
        this.pixelMap = segments;
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

    public void addSegment(Segment segment) {
        pixelMap.add(segment);
    }
}
