package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.Dragon;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.api.Settings;
import com.chromosundrift.bhima.api.SystemInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.dragonmind.model.Wiring;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Runtime.getRuntime;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

/**
 * Loads and plays video on Bhima configured by file.
 */
public final class BhimaSup extends DragonMind implements Dragon {

    private static final Logger logger = LoggerFactory.getLogger(BhimaSup.class);
    private static final int CABINET_PORT_INDEX_BASE = 1;

    private static int INNER_WIDTH = 400;
    private static int INNER_HEIGHT = 100;

    private boolean movieMode = true; // TODO convert to currentProgram
    private boolean mouseMode = false;

    private Config config;
    private Wiring wiring;

    private int inx = 0;
    private int iny = 0;

    private DragonmindServer server = null;

    private float xpos = 0;
    private PFont mesgFont;
    private String mesg;
    private DragonProgram moviePlayer;

    @Override
    public String getStatus() {
        return "running"; // TODO implement status
    }

    @Override
    public SystemInfo getSystemInfo() {
        return null;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Wiring getWiring() {
        return wiring;
    }

    @Override
    public Settings getSettings() {
        getPusherMan().ensureReady();
        return getPusherMan().getSettings();
    }

    @Override
    public Settings setSettings(Settings settings) {
        getPusherMan().ensureReady();
        return this.getPusherMan().setSettings(settings);
    }

    @Override
    public ProgramInfo getCurrentProgram() {
        if (!movieMode || moviePlayer == null) {
            return ProgramInfo.NULL_PROGRAM_INFO;
        } else {
            return moviePlayer.getCurrentProgramInfo(inx, iny, INNER_WIDTH, INNER_HEIGHT);
        }
    }

    @Override
    public List<ProgramInfo> getPrograms() {
        return moviePlayer.getProgramInfos(inx, iny, INNER_WIDTH, INNER_HEIGHT);
    }

    @Override
    public ProgramInfo runProgram(String id) {
        return moviePlayer.runProgram(id);
    }

    @Override
    public Map<String, Set<Integer>> getEffectiveWiring() {
        Map<String, Set<Integer>> effective = new HashMap<>();
        config.getPixelMap().forEach(s -> {
            // cabinet ports are 1-based indexed
            effective.put(s.getName(), s.getPixels().stream().map(
                    pp -> pp.getStrip() + CABINET_PORT_INDEX_BASE).collect(toSet())
            );
        });
        return effective;
    }

    @Override
    public void settings() {
        size(INNER_WIDTH, INNER_HEIGHT);
        mesg = "LOVE   OVER   FEAR";
    }

    @Override
    public void setup() {
        super.setup();
        xpos = width;
        AtomicBoolean doVideo = new AtomicBoolean(true);
        AtomicBoolean doServer = new AtomicBoolean(true);
        if (args != null) {
            Arrays.stream(args).filter(s -> s.startsWith("-")).forEach(arg -> {
                if (arg.equals("-noserver")) {
                    logger.info("Server disabled");
                    doServer.set(false);
                } else if (arg.equals("-novideo")) {
                    logger.info("Video disabled");
                    doVideo.set(false);
                }
            });
        } else {
            logger.info("No command line args available from Processing");
        }

        if (doServer.get()) {
            server = new DragonmindServer();
            server.start(this);
            getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
                logger.info("Server shutdown complete");
            }, "Dragonmind Server Shutdown Hook"));
        }

        mesgFont = loadFont("HelveticaNeue-CondensedBlack-16.vlw");

        background(0);

        if (doVideo.get()) {
            moviePlayer = new MoviePlayerImpl();
            moviePlayer.setup(this); // FIXME this composition linkage causes ambiguous state in lifecycle
        } else {
            moviePlayer = new NullProgram();
        }
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
            applyGlobalTransforms(config);

            // manual trasform fixup for getting the whole dragon in-frame with the video panel
            translate(-2.28f * width, -2.40f * height);
            scale((float) width / 1982);

            getPusherMan().ensureReady();
            config.getPixelMap().forEach(segment -> {
                if (segment.getEnabled() && !segment.getIgnored()) {
                    drawSegment(mainSrc, segment);
                }
            });
            popMatrix();
        } catch (NearDeathExperience e) {
            // we might want to die a normal death here, while we intercept deaths in this class due to
            // braindead video load failures, other cases of die() should be reinstated
            logger.info("Near death experience " + e.getMessage());
            super.die(e.getMessage(), e);
        }

    }

    private void drawSegment(PImage mainSrc, Segment segment) {
        pushMatrix();
        final List<Transform> transforms = segment.getTransforms();
        transforms.stream().filter(t -> !t.isBaked()).forEach(this::applyTransform);
        mapSurfaceToPixels(mainSrc, segment);

        int bright = color(255, 0, 0, 30);
        int color = color(170, 170, 170, 30);
        int strongFg = color(255, 30);

        // draw the pixelpoints into the current view
        drawModelPoints(segment.getPixels(), 20, false, emptyList(), -1, bright, color, strongFg, false, segment.getPixelIndexBase());
        popMatrix();
    }

    private PImage scrollText(PImage pImage) {
        // text overlay
        xpos -= 0.1;
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
        pg.text(mesg, xpos, 63);
        pg.endDraw();

        pImage.blend(pg, 0, 0, width, height, 0, 0, width, height, PConstants.DARKEST);
        return pImage;
    }

    private PImage getPImage() {
        PImage image;
        if (movieMode) {
            image = moviePlayer.draw(this, width, height);
        } else if (mouseMode) {
            image = TestPattern.fullCrossHair(this, mouseX, mouseY, width, height);
        } else {
            image = TestPattern.cycleTestPattern(this, width, height);
        }
        return image;
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

    @Override
    public void fail(String msg) {
        logger.error("Turning off movie mode because: {}", msg);
        movieMode = false;
    }

    /**
     * Standard shiz.
     *
     * @param args relayed to Processing entry point.
     */
    public static void main(String[] args) {
        logger.info("starting {} with args {}", BhimaSup.class.getName(), args);
        setNativeLibraryPaths();
        PApplet.main(BhimaSup.class, args);
    }

}
