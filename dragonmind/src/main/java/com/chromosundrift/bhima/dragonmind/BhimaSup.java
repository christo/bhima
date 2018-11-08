package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Loads and plays video on Bhima configured by file.
 */
public class BhimaSup extends DragonMind {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);
    private StickSlurper ss;
    private int current = 0;
    private long lastFileShowed = 0;
    private static long MS_POLLING_INTERVAL = 1000 * 30;
    private Movie movie;
    private Config config;
    private boolean movieMode = false;
    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private int inx = 0;
    private int iny = 0;

    @Override
    public void settings() {
        size(INNER_WIDTH, INNER_HEIGHT);
    }

    @Override
    public void setup() {
        super.setup();
        background(0);
        // TODO remove movie load and loop from setup, make "movie" a local variable
//        movie = new Movie(this, "video/diagonal-bars.mp4");
                movie = new Movie(this, "video/fire-ex.m4v");
        //        movie = new Movie(this, "video/100x1000 aztec rug.m4v");
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
        if (false) {
            keepFreshMovie();
        }
        if (movie != null) {
            image(movie, inx, iny, INNER_WIDTH, INNER_HEIGHT);

            pushMatrix();
            applyTransforms(config.getBackground().getTransforms());
            // manual trasform fixup for getting the whole dragon in-frame with the video panel
            translate(-2.38f * width, -0.70f * height);
            scale((float) width / 1920);
            // flip upside down for some weird reason then shift down back into frame
            // scale(1, -1);
            // translate(0, height);

            PImage pImage = getPImage();

            getPusherMan().ensureReady();
            config.getPixelMap().forEach(segment -> {
                if (segment.getEnabled() && !segment.getIgnored()) {
                    pushMatrix();
                    applyTransforms(segment.getTransforms());
                    mapSurfaceToPixels(pImage, segment.getPixels());

                    int bright = color(255, 0, 0, 255);
                    int color = color(170, 170, 170, 255);
                    int strongFg = color(255, 255);
                    drawPoints(segment.getPixels(), 255, false, emptyList(), -1, bright, color, strongFg);
                    popMatrix();
                }
            });
            popMatrix();
        }

    }

    private PImage getPImage() {
        if (movieMode) {
            return movie.get();
        } else {
            return testPattern(this, width, height);
        }
    }

    static PImage testPattern(PApplet papp, int width, int height) {
        PGraphics pg = papp.createGraphics(width, height);
        pg.colorMode(RGB, 255);
        pg.beginDraw();
        pg.background(0);
        pg.noStroke();
        pg.fill(0);
        pg.rect(0, 0, width, height);
        pg.strokeWeight(3);
        pg.stroke(90, 90, 255);
        long timeUnit = System.currentTimeMillis() / 10;
        pg.line(0, timeUnit % height, width, timeUnit % height);
        pg.line(timeUnit % width, 0, timeUnit % width, height);
        pg.endDraw();
        return pg;

    }

    private void keepFreshMovie() {
        long now = System.currentTimeMillis();
        if (lastFileShowed + MS_POLLING_INTERVAL < now) {
            current++;
            List<File> media = ss.getMedia();
            String movieFile = "video/50x1000 red scales.mov"; // default
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
    }

    /**
     * Callback method for movie events from Processing.
     *
     * @param m the movie.
     */
    @SuppressWarnings("unused")
    public void movieEvent(Movie m) {
        m.read();
    }


    /**
     * Standard
     *
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(BhimaSup.class, args);
    }
}
