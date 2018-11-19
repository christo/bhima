package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.video.Movie;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;

// TODO fix filesystem monitoring and movie cycling
// TODO receive NDI stream for display

/**
 * Loads and plays video on Bhima configured by file.
 */
public class BhimaSup extends DragonMind {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);
    private static final String DEFAULT_MOVIE = "video/laser-mountain.m4v";

    private static long MS_POLLING_INTERVAL = 1000 * 30;
    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private StickSlurper ss;
    private int current = 0;
    private long lastFileShowed = 0;
    private Movie movie;
    private Config config;
    private boolean movieMode = true;

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
//        movie = new Movie(this, "video/golden-cave.m4v");
//        movie = new Movie(this, "video/quick-threads.m4v");
//        movie = new Movie(this, "video/dots-waves.m4v");
//        movie = new Movie(this, "video/diagonal-bars.mp4");
//         movie = new Movie(this, "video/fire-low.m4v");
//         movie = new Movie(this, "video/diamonds.m4v");
//         movie = new Movie(this, "video/Star Pink Vj ANIMATION FREE FOOTAGE HD-oMM1wsQEU-M.mp4");
//         movie = new Movie(this, "video/50x1000 red scales.mov");
//         movie = new Movie(this, "video/100x1000 aztec rug.m4v");
//         movie = new Movie(this, "video/slomo-ink.m4v");
//         movie = new Movie(this, "video/colour-ink.m4v");
//         movie = new Movie(this, "video/clouds.m4v");
         movie = new Movie(this, DEFAULT_MOVIE);
        movie.speed(1f);
        movie.loop();
        try {
            config = loadConfigFromFirstArgOrDefault();
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
            // TODO: dynamically perform this scaling function into the video frame (full width, vertically centred)

            // manual trasform fixup for getting the whole dragon in-frame with the video panel
            translate(-2.45f * width, -0.96f * height);
            scale((float) width / 1920);

            getPusherMan().ensureReady();
            config.getPixelMap().forEach(segment -> {
                if (segment.getEnabled() && !segment.getIgnored()) {
                    pushMatrix();
                    applyTransforms(segment.getTransforms());
                    mapSurfaceToPixels(pImage, segment);

                    int bright = color(255, 0, 0, 90);
                    int color = color(170, 170, 170, 90);
                    int strongFg = color(255, 90);
                    // draw the pixelpoints into the current view
                    drawPoints(segment.getPixels(), 255, false, emptyList(), -1, bright, color, strongFg, false, segment.getPixelIndexBase());
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
            String movieFile = DEFAULT_MOVIE;
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

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        char key = event.getKey();
        if (key == ' ') {
            movieMode = !movieMode;
        }
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
        long l1y = (System.currentTimeMillis() / 30) % height;
        long l2x = (System.currentTimeMillis() / 100) % width;
        return fullCrossHair(papp, width - l2x, l1y, width, height);
    }

    private static PImage fullCrossHair(PApplet papp, long l2x, long l1y, int width, int height) {
        PGraphics pg = papp.createGraphics(width, height);
        pg.colorMode(RGB, 255);
        pg.beginDraw();
        pg.background(0, 0, 0);
        pg.noStroke();
        pg.fill(0, 0, 0);
        pg.rect(0, 0, width, height);
        pg.strokeWeight(2);
        pg.stroke(0, 0, 255);
        pg.line(0, l1y, width, l1y);
        pg.strokeWeight(6);
        pg.stroke(255, 255, 0);
        pg.line(l2x, 0, l2x, height);
        pg.strokeWeight(2);
        pg.stroke(255, 0, 0);
        pg.line(l2x, 0, l2x, height);
        pg.endDraw();
        return pg;
    }

}
