package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.web.DragonmindServer;
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
import java.util.Arrays;
import java.util.List;

import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyList;

// TODO receive NDI stream for display
// TODO make window bigger and doing view scaling to fit the correct aspect ratio centred and fit.

/**
 * Loads and plays video on Bhima configured by file.
 */
public class BhimaSup extends DragonMind {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);

    private static long MS_MOVIE_CYCLE_PERIOD = 1000 * 120;
    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private StickSlurper ss;
    private int current = -1;
    private long lastFileShowed = 0;
    private Movie movie = null;
    private Config config;
    private boolean movieMode = false;

    private int inx = 0;
    private int iny = 0;
    private boolean mouseMode = false;

    private List<String> builtInVideos;
    private DragonmindServer server = null;

    @Override
    public void settings() {
        size(INNER_WIDTH, INNER_HEIGHT);
    }

    @Override
    public void setup() {
        super.setup();


        if (args.length > 0 && args[0].equals("-server") || args.length > 1 && args[1].equals("-server")) {
            server = new DragonmindServer();
            server.start(8888);
            getRuntime().addShutdownHook(new Thread(() -> server.stop(), "Dragonmind Server Shutdown Hook"));
        }


        background(0);

        builtInVideos = Arrays.asList(
                "video/laser-mountain.m4v",
                "video/golden-cave.m4v",
                "video/quick-threads.m4v",
                "video/dots-waves.m4v",
                "video/diagonal-bars.mp4",
                "video/fire-ex.m4v",
                "video/diamonds.m4v",
                "video/Star Pink Vj ANIMATION FREE FOOTAGE HD-oMM1wsQEU-M.mp4",
                //"video/50x1000 red scales.mov", // FIXME
                "video/100x1000 aztec rug.m4v",
                "video/colour-ink.m4v",
                "video/clouds.m4v");

        try {
            config = loadConfigFromFirstArgOrDefault();
            ss = new StickSlurper();
            ss.start();
            getRuntime().addShutdownHook(new Thread(() -> ss.stop(), "StickSlurper Shutdown Hook"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        try {
            keepFreshMovie();
            PImage pImage = getPImage();
            image(pImage, inx, iny, INNER_WIDTH, INNER_HEIGHT);

            pushMatrix();
            applyTransforms(config.getBackground().getTransforms());
            // TODO: dynamically perform this scaling function into the video frame (full width, vertically centred)

            // manual trasform fixup for getting the whole dragon in-frame with the video panel
            translate(-2.35f * width, -2.56f * height);
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
        } catch (NearDeathExperience e) {
            // we might want to die a normal death here, while we intercept deaths in this class due to
            // braindead video load failures, other cases of die() should be reinstated
            super.die(e.getMessage(), e);
        }

    }

    private PImage getPImage() {
        if (movieMode && movie != null) {
            return movie.get();
        } else {
            if (mouseMode) {
                return fullCrossHair(this, mouseX, mouseY, width, height);
            }
            return cycleTestPattern(this, width, height);
        }
    }

    @Override
    public void die(String what) {
        // OMG WTF failing to load a movie file shuts down the sketch. No recovery options.
        throw new NearDeathExperience(what);
    }

    @Override
    public void die(String what, Exception e) {
        throw new NearDeathExperience(what, e);
    }

    private void keepFreshMovie() {
        long now = System.currentTimeMillis();
        if (lastFileShowed + MS_MOVIE_CYCLE_PERIOD < now) {
            logger.info("time to load new movie");
            current++;
            List<File> media = ss.getMedia();

            try {
                Movie newMovie = null;
                // TODO figure out if we need to dispose of anything in movieland when we load a new movie
                if (media.size() > 0) {
                    logger.info("Loading new movie from media library");
                    current %= media.size();
                    String movieFile = media.get(current).getAbsolutePath();
                    newMovie = setupMovie(movieFile);
                    lastFileShowed = now;
                } else {
                    logger.info("Loading new movie from built-ins");
                    current %= builtInVideos.size();
                    newMovie = setupMovie(builtInVideos.get(current));
                    lastFileShowed = now;
                }
                if (movie != null) {
                    movie.dispose();
                }
                if (newMovie != null) {
                    movie = newMovie;
                    logger.info("New movie loaded OK");
                } else {
                    // need to reload it?
                    movie = setupMovie(movie.filename);
                }
            } catch (NearDeathExperience e) {
                logger.warn("Movie loading failed. Dodging death.");
                ss.excludeMovieFile(media.get(current).getName());
            }
        }
    }

    /**
     * Load and start looping movie with the given file name.
     *
     * @param movieFile the movie file name to load.
     * @return the {@link Movie}.
     */
    private Movie setupMovie(String movieFile) {
        logger.info("setting up movie " + movieFile);
        Movie movie = new Movie(this, movieFile);
        movie.speed(1f);
        movie.loop();
        return movie;
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
