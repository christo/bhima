package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import static com.chromosundrift.bhima.api.ImageUtils.generateNullImage;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Collections.emptyMap;


/**
 * Front end model/DTO for a runnable program.
 */
@JsonInclude(NON_EMPTY)
public final class ProgramInfo {
    private static final ProgramInfo NULL_PROGRAM_INFO =
            new ProgramInfo("NULL", "no program", "dummy", generateNullImage(400, 100), emptyMap());

    @JsonIgnore
    public static ProgramInfo getNullProgramInfo() {
        return NULL_PROGRAM_INFO;
    }

    private String name;
    private String id;
    private String type;
    private Map<String, String> settings;

    @JsonSerialize(using = ImageSerializer.class)
    private BufferedImage thumbnail;

    public ProgramInfo() {
        settings = new TreeMap<>();
    }

    public ProgramInfo(String id, String name, String type, BufferedImage thumbnail) {
        this(id, name, type, thumbnail, new Hashtable<>());
    }

    public ProgramInfo(String id, String name, String type, BufferedImage thumbnail, Map<String, String> settings) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.thumbnail = thumbnail;
        this.settings = new TreeMap<>(settings);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BufferedImage getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Map<String, String> getSettings() {
        return Collections.unmodifiableMap(settings);
    }

    public void setSettings(Map<String, String> settings) {
        // defensive copy
        this.settings = new TreeMap<>(settings);
    }
}
