package com.chromosundrift.bhima.dragonmind.model;


import com.chromosundrift.bhima.geometry.PixelPoint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@SuppressWarnings({"WeakerAccess", "unused"})
@JsonInclude(NON_NULL)
@JsonPropertyOrder({"project", "version", "brightnessThreshold", "cameraMask", "pixelPushers"})
public final class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    public final static String DEFAULT_CONFIG_FILE = "dragonmind.config.json";

    /**
     * Name
     */
    private String project;

    private String version;

    /**
     * Background image.
     */
    private Background background;

    /**
     * Closed polygon defined by array of [x,y] points.
     */
    private float[][] cameraMask;

    private List<Segment> pixelMap;

    private List<PixelPusherInfo> pixelPushers;

    private int brightnessThreshold;
    private BufferedImage bgImage;

    public Config() {
    }

    public Config(@JsonProperty("project") String project, @JsonProperty("version") String version) {
        this.project = project;
        this.version = version;
    }

    public Config save(String filename) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), this);
        return this;
    }

    public Config save() throws IOException {
        return save(DEFAULT_CONFIG_FILE);
    }

    public static Config load(String filename) throws IOException {
        logger.info("will try to load config file " + filename);
        ObjectMapper objectMapper = getObjectMapper();
        Config config = objectMapper.readValue(new File(filename), Config.class);
        logger.info("config loaded: " + config.project);
        return config;
    }

    public static Config load() throws IOException {
        try {
            return load(DEFAULT_CONFIG_FILE);
        } catch (IOException e) {
            throw new IOException("Failed to load config " + e.getMessage(), e);
        }
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

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public float[][] getCameraMask() {
        return cameraMask;
    }

    public void setCameraMask(float[][] cameraMask) {
        this.cameraMask = cameraMask;
    }

    public List<Segment> getPixelMap() {
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

    @JsonIgnore
    public BufferedImage getBackgroundImage() {
        if (bgImage == null && background != null && background.getFilename() != null) {
            try {
                return loadBackgroundImage();
            } catch (IOException e) {
                logger.error("cannot load background image " + background.getFilename(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    private BufferedImage loadBackgroundImage() throws IOException {
        String filename = background.getFilename();
        BufferedImage read = ImageIO.read(new File(background.getFilename()));
        bgImage = read;
        return read;
    }

    public Stream<Segment> enabledSegments() {
        return getPixelMap().stream().filter(Segment::getEnabled);
    }
}
