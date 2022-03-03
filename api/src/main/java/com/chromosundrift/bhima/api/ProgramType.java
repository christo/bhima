package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Arrays;
import java.util.List;

@JsonInclude(NON_EMPTY)
public class ProgramType {

    public static final ProgramType ALGORITHM = new ProgramType("Algorithm", "code module");
    public static final ProgramType MOVIE = new ProgramType("Movie", "looping video");
    public static final ProgramType IMAGE = new ProgramType("Image", "animated image");
    public static final ProgramType STREAM = new ProgramType("Stream", "video stream");
    public static final ProgramType TEXT = new ProgramType("Text", "scrolling message");
    public static final ProgramType NULL = new ProgramType("Borken", "achtung geborken");

    /** All normal types - excludes {@link #NULL} */
    @JsonIgnore
    private static final List<ProgramType> ALL = Arrays.asList(
            ALGORITHM, MOVIE, IMAGE, STREAM, TEXT
    );

    private String name;
    private String description;

    public ProgramType() {
    }

    public ProgramType(String name, String description) {
        this.name = name;
        this.description = description;
    }

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

    @JsonIgnore
    public static List<ProgramType> all() {
        return ALL;
    }

}
