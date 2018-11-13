package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.MouseEvent;
import processing.video.Movie;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;

// TODO fix filesystem monitoring and movie cycling

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
    private boolean movieMode = true;
    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private int inx = 0;
    private int iny = 0;
    private boolean mouseMode = false;

    @Override
    public void settings() {
        size(INNER_WIDTH, INNER_HEIGHT);
    }

    @Override
    public void setup() {
        super.setup();
        background(0);
        movie = new Movie(this, "video/diagonal-bars.mp4");
        // movie = new Movie(this, "video/fire-ex.m4v");
        // movie = new Movie(this, "video/100x1000 aztec rug.m4v");
        movie.loop();
        try {
            loadConfigFromFirstArgOrDefault();
            ss = new StickSlurper();
            ss.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void draw() {
        if (false) {
            keepFreshMovie();
        }
        if (movie != null) {
            PImage pImage = getPImage();
            image(pImage, inx, iny, INNER_WIDTH, INNER_HEIGHT);

            pushMatrix();
            applyTransforms(config.getBackground().getTransforms());
            // manual trasform fixup for getting the whole dragon in-frame with the video panel
            translate(-2.38f * width, -0.70f * height);
            scale((float) width / 1920);
            // flip upside down for some weird reason then shift down back into frame
            // scale(1, -1);
            // translate(0, height);

            getPusherMan().ensureReady();
            config.getPixelMap().forEach(segment -> {
                if (segment.getEnabled() && !segment.getIgnored()) {
                    pushMatrix();
                    applyTransforms(segment.getTransforms());
                    mapSurfaceToPixels(pImage, segment.getPixels());

                    int bright = color(255, 0, 0, 255);
                    int color = color(170, 170, 170, 255);
                    int strongFg = color(255, 255);
                    // draw the pixelpoints into the current view
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
            if (mouseMode) {
                return fullCrossHair(this, mouseX, mouseY, width, height);
            }
            return cycleTestPattern(this, width, height);
        }
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

    @Override
    public void mouseClicked(MouseEvent event) {
        mouseMode = !mouseMode;
    }

    /**
     * Standard shiz.
     *
     * @param args relayed to Processing entry point.
     */
    public static void main(String[] args) {
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(BhimaSup.class, args);
    }

    private static PImage cycleTestPattern(PApplet papp, int width, int height) {
        long timeUnit = System.currentTimeMillis() / 100;
        long l1y = timeUnit % height;
        long l2x = timeUnit % width;
        return fullCrossHair(papp, l2x, l1y, width, height);
    }

    private static PImage fullCrossHair(PApplet papp, long l2x, long l1y, int width, int height) {
        PGraphics pg = papp.createGraphics(width, height);
        pg.colorMode(RGB, 255);
        pg.beginDraw();
        pg.background(100, 50, 50);
        pg.noStroke();
        pg.fill(100, 50, 50);
        pg.rect(0, 0, width, height);
        pg.strokeWeight(3);
        pg.stroke(90, 90, 255);
        pg.line(0, l1y, width, l1y);
        pg.line(l2x, 0, l2x, height);
        pg.endDraw();
        return pg;
    }

}
