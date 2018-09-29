package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Movie;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BhimaSup extends DragonMind {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);
    private StickSlurper ss;
    private int current = 0;
    private long lastFileShowed = 0;
    private long MS_LOOP_FOR = 1000 * 60 * 5;
    private Movie movie;
    private Config config;

    @Override
    public void settings() {
        size(400, 100);
    }

    @Override
    public void setup() {
        super.setup();
        background(0);
        movie = new Movie(this, "video/fire-low.m4v");
        movie.loop();
        try {
            config = Config.load();
            ss = new StickSlurper();
            ss.start();

        } catch (IOException e) {
            logger.error("Failed to load config " + e.getMessage(), e);
        }
    }

    @Override
    public void draw() {
        image(movie, 0, 0, width, height);
        // TODO apply global transform
        //

        translate(-252, -110);
        scale(0.60655075f);

        PImage pImage = movie.get();

        config.getPixelMap().forEach(segment -> {
            pushMatrix();
            applyTransformsFor(segment);
            mapSurfaceToPixels(pImage, segment.getPixels());

            popMatrix();
        });
        // render the segment pixels
    }

    private void drawWithRotation() {
        long now = System.currentTimeMillis();
        if (lastFileShowed + MS_LOOP_FOR < now) {
            current++;
            List<File> media = ss.getMedia();
            String movieFile = "video/fire-low.m4v"; // default
            if (media.size() > 0) {
                current %= media.size();
                lastFileShowed = System.currentTimeMillis();
                movieFile = media.get(current).getName();
            }
            movie = new Movie(this, movieFile);

            logger.info("looping " + movieFile);
            movie.loop();
        } else {
            logger.error(("movie not loaded and available"));
        }

        if (movie != null) {
            image(movie, 0, height/2 - movie.height/2, width, height * movie.width/width);
        }
    }

    public void movieEvent(Movie m) {
        m.read();
    }

    public static void main(String[] args) {
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(BhimaSup.class, args);
    }
}
