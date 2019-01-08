package com.chromosundrift.bhima.dragonmind.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Arrays.asList;

@SuppressWarnings({"WeakerAccess", "unused"})
@JsonInclude(NON_EMPTY)
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

    private List<Segment> pixelMap = new ArrayList<>();

    private List<PixelPusherInfo> pixelPushers;

    private float brightnessThreshold;

    private BufferedImage bgImage;

    public Config() {
    }

    public Config(@JsonProperty("project") String project, @JsonProperty("version") String version) {
        this.project = project;
        this.version = version;
    }

    public static String describeClashes(Map<ImmutablePair<String, String>, Set<Integer>> clashes) {
        StringBuilder clashMessage = new StringBuilder("Strip Number Clashes:\n");
        for (ImmutablePair<String, String> pair : clashes.keySet()) {
            clashMessage.append(pair).append(": ").append(clashes.get(pair)).append("\n");
        }
        return clashMessage.toString();
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
        int nPixels = config.getPixelMap().stream().filter(Segment::isActive).mapToInt(s -> s.getPixels().size()).sum();
        logger.info("config loaded: {} with {} pixels", config.project, nPixels);

        sanityCheck(config);
        return config;
    }

    /**
     * Check a few invariants about the config.
     *
     * @param config the config to check.
     * @throws IllegalStateException when insanity is found.
     */
    private static void sanityCheck(Config config) throws IllegalStateException {
        boolean ok = true; // until proven otherwise
        for (Segment segment : config.getPixelMap()) {
            logger.debug("Sanity checking segment: {}", segment);
            PixelPoint prev = null;

            for (PixelPoint pixel : segment.getPixels()) {
                // each pixelIndexBase-adjusted pixel number must be non-negative
                boolean negativePixelNum = pixel.getPixel() - segment.getPixelIndexBase() < 0;
                if (negativePixelNum) {
                    logger.error("Negative pixel number: segment: {} pixel: {}", segment, pixel);
                    ok = false;
                }
                if (prev != null) {
                    if (prev.equals(pixel)) {
                        // pixels should be unique
                        logger.warn("two pixels identical, you should remove one: {} prev: {} pixel: {}",
                                segment.getName(), prev, pixel);
                        // TODO for enabled and not ignored segments, strip/pixelNum pairs should be globally unique
                    } else if (prev.getStrip() == pixel.getStrip() && prev.getPixel() >= pixel.getPixel()) {
                        // pixels in each strip must be sorted by pixel number
                        logger.error("pixel out of order: {} prev: {} pixel: {}", segment.getName(), prev, pixel);
                    }
                }
                prev = pixel;
            }

        }
        if (!ok) {
            throw new IllegalStateException("insanity (check logs)");
        }
    }

    public Map<ImmutablePair<String, String>, Set<Integer>> calculateClashes() {
        return calculateClashes(s -> true);
    }

    public Map<ImmutablePair<String, String>, Set<Integer>> calculateClashes(Predicate<? super Segment> filter) {
        Map<ImmutablePair<String, String>, Set<Integer>> clashes = new HashMap<>();
        Map<String, Set<Integer>> segmentNumbersByName = new HashMap<>();
        getPixelMap().stream().filter(filter).forEach(s ->
                segmentNumbersByName.put(s.getName(), s.getEffectiveStripNumbers())
        );
        for (Map.Entry<String, Set<Integer>> seg : segmentNumbersByName.entrySet()) {
            Set<Integer> sNums = seg.getValue();
            for (Map.Entry<String, Set<Integer>> other : segmentNumbersByName.entrySet()) {
                if (!seg.getKey().equals(other.getKey())) {
                    ImmutablePair<String, String> clashPair = new ImmutablePair<>(seg.getKey(), other.getKey());
                    Set<Integer> clashingStripNums = new HashSet<>();
                    for (Integer sNum : sNums) {
                        if (other.getValue().contains(sNum)) {
                            clashingStripNums.add(sNum);
                        }
                    }
                    if (!clashingStripNums.isEmpty()) {
                        clashes.put(clashPair, clashingStripNums);
                    }
                }
            }
        }
        return clashes;
    }

    @JsonIgnore
    public Set<Integer> getUnusedStripNumbers(int from, int to) {
        Set<Integer> unused = new HashSet<>();
        Set<Integer> used = getUsedStripNumbers();
        for (int i = from; i <= to; i++) {
            if (!used.contains(i)) {
                unused.add(i);
            }
        }
        return unused;
    }

    @JsonIgnore
    private Set<Integer> getUsedStripNumbers() {
        return getPixelMap().stream().flatMap(s -> s.getEffectiveStripNumbers().stream()).collect(Collectors.toSet());
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

    public float getBrightnessThreshold() {
        return brightnessThreshold;
    }

    public void setBrightnessThreshold(float brightnessThreshold) {
        this.brightnessThreshold = brightnessThreshold;
    }

    public void addSegment(Segment segment) {

        pixelMap.add(segment);
    }

    public void addSegments(Segment segment, Segment... segments) {
        pixelMap.add(segment);
        pixelMap.addAll(asList(segments));
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

    /**
     * Fetches from config, defaulting to identity transform if there is no configured global transform.
     *
     * @param type
     * @return
     */
    public Transform getGlobalTransformByType(final Transform.Type type) {
        return getBackground().getTransforms().stream().filter(t -> t.is(type)).findFirst().orElse(type.id);
    }

    public void setGlobalTransforms(Transform offset, Transform scale) {
        List<Transform> transforms = asList(offset, scale);
        getBackground().setTransforms(transforms);
    }

    @JsonIgnore
    public Transform getGlobalScale() {
        return getGlobalTransformByType(Transform.Type.SCALE);
    }

    @JsonIgnore
    public Transform getGlobalTranslate() {
        return getGlobalTransformByType(Transform.Type.TRANSLATE);
    }

    @JsonIgnore
    public void setGlobalTranslate(Transform translate) {
        setGlobalTransforms(translate, getGlobalScale());
    }

    @JsonIgnore
    public void setGlobalScale(Transform scale) {
        setGlobalTransforms(getGlobalTranslate(), scale);
    }

    public void multiplyGlobalScale(double v) {
        setGlobalScale(getGlobalScale().multiplyScale(v));
    }

    public void addGlobalTranslateY(int dy) {
        setGlobalTranslate(getGlobalTranslate().addTranslateY(dy));
    }

    public void addGlobalTranslateX(int dx) {
        Transform globalTranslate = getGlobalTranslate();
        Transform translate = globalTranslate.addTranslateX(dx);
        setGlobalTranslate(translate);
    }

}
