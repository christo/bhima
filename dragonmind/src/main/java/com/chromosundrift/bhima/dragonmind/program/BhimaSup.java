package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.web.DragonmindServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.video.Movie;

import java.io.IOException;

import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyList;

// TODO receive NDI stream for display
// TODO make window bigger and doing view scaling to fit the correct aspect ratio centred and fit.

/**
 * Loads and plays video on Bhima configured by file.
 */
public class BhimaSup extends DragonMind {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);

    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private boolean movieMode = true; // TODO convert to curerntProgram
    private boolean mouseMode = false;


    private Config config;

    private int inx = 0;
    private int iny = 0;

    private DragonmindServer server = null;

    private float xpos = 0;
    private PFont mesgFont;
    private String mesg;
    private MoviePlayer moviePlayer;

    @Override
    public void settings() {
        size(INNER_WIDTH, INNER_HEIGHT);
    }

    @Override
    public void setup() {
        super.setup();
        xpos = width;

        if (args.length > 0 && args[0].equals("-server") || args.length > 1 && args[1].equals("-server")) {
            server = new DragonmindServer();
            server.start(8888);
            getRuntime().addShutdownHook(new Thread(() -> server.stop(), "Dragonmind Server Shutdown Hook"));
        }

        mesgFont = loadFont("HelveticaNeue-CondensedBlack-16.vlw");

        background(0);

        moviePlayer = new MoviePlayer();
        moviePlayer.setup(this);

        try {
            config = loadConfigFromFirstArgOrDefault();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void draw() {
        try {
            // main feed
            PImage mainSrc;
            if (movieMode) {
                mainSrc = moviePlayer.draw(this, width, height);
            } else {
                mainSrc = getPImage();
            }
            // scroll text
            PImage comp = scrollText(mainSrc);

            // display preview in on-screen viewport
            image(comp, inx, iny, INNER_WIDTH, INNER_HEIGHT);


            // BHIMA DRAGON MAPPING BULLSHIT FROM HERE ON
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
                    mapSurfaceToPixels(mainSrc, segment);

                    int bright = color(255, 0, 0, 30);
                    int color = color(170, 170, 170, 30);
                    int strongFg = color(255, 30);
                    // draw the pixelpoints into the current view
                    drawModelPoints(segment.getPixels(), 20, false, emptyList(), -1, bright, color, strongFg, false, segment.getPixelIndexBase());
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

    private PImage scrollText(PImage pImage) {
        // text overlay
        xpos -= 0.2;
        if (xpos < -2000) {
            xpos = width + 10;
        }

        PGraphics pg = createGraphics(width, height);
        pg.beginDraw();
        pg.background(255, 255, 255, 0);
        clear();
        pg.fill(0, 0, 0);

        float fontSize = (float) (15 - ((width - xpos) * 0.006));
        pg.textFont(mesgFont, fontSize);
        mesg = "LOVE   OVER   FEAR";
        pg.text(mesg, xpos, 63);
        pg.endDraw();

        pImage.blend(pg, 0, 0, width, height, 0, 0, width, height, PConstants.OVERLAY);
        return pImage;
    }

    private PImage getPImage() {
        if (movieMode) {
            return moviePlayer.draw(this, width, height);
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
