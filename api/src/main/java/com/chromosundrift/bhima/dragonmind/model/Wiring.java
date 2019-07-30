package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Map from Segment channel to wiring port.
 * <p>
 * The patch panel has ports numbered from 1 to n.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@JsonInclude(NON_EMPTY)
@JsonPropertyOrder({"numPorts", "segments"})
public class Wiring {

    private static final Logger logger = LoggerFactory.getLogger(Wiring.class);

    private static final String DEFAULT_WIRING_FILE = "bhima.wiring.json";

    /**
     * Bhima patch panel at time of writing.
     */
    private static final int DEFAULT_NUM_PORTS = 24;

    private int numPorts;
    private Map<String, Map<Integer, Integer>> segments;

    public static Wiring load() throws IOException {
        try {
            return load(DEFAULT_WIRING_FILE);
        } catch (IOException e) {
            throw new IOException("Failed to load wiring " + e.getMessage(), e);
        }
    }

    public static Wiring load(String filename) throws IOException {
        logger.info("will try to load wiring file " + filename);
        return new ObjectMapper().readValue(new File(filename), Wiring.class);
    }

    public Wiring save() throws IOException {
        return save(DEFAULT_WIRING_FILE);
    }

    public Wiring save(String filename) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter().writeValue(new File(filename), this);
        return this;
    }

    public Wiring(@JsonProperty("numPorts") int numPorts) {
        this.numPorts = numPorts;
        this.segments = new HashMap<>();
    }

    public void setNumPorts(int numPorts) {
        this.numPorts = numPorts;
    }

    public int getNumPorts() {
        return numPorts;
    }

    public Map<String, Map<Integer, Integer>> getSegments() {
        return segments;
    }

    public void setSegments(Map<String, Map<Integer, Integer>> segments) {
        this.segments = segments;
    }

    /**
     * Generates an identity function wiring file from a config. All segment names must be unique.
     *
     * @throws IOException in the event of json or file issues.
     */
    public static void main(String[] args) throws IOException {
        Wiring wiring = new Wiring(24);
        Config config = Config.load("dragonmind-mini.config.json");
        // check invariant that all segment names are unique
        List<Segment> segments = config.getPixelMap();
        if (segments.stream().map(Segment::getName).distinct().count() != segments.size()) {
            throw new RuntimeException("nonunique segment detected");
        }
        segments.stream().filter(s -> s.getEnabled() && !s.getIgnored()).forEach(segment -> {

            String name = segment.getName();
            logger.info("segment {}", name);
            Map<Integer, Integer> stripMap = new TreeMap<>();
            segment.getEffectiveStripNumbers().forEach(st -> stripMap.put(st, st));
            wiring.segments.put(name, stripMap);

        });
        wiring.save();
    }
}
