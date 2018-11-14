package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.geometry.PixelPoint;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import g4p_controls.GAlign;
import g4p_controls.GLabel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Processing-based app for running patterns.
 */
public class DragonMind extends ProcessingBase {

    private static final Logger logger = LoggerFactory.getLogger(DragonMind.class);
    public static final String PROP_FILE = "build.properties";
    private static final boolean DEBUG_NOISY = false;

    private BallsProgram balls = new BallsProgram();
    private PusherMan pusherMan;

    protected static String imageFile(String scanId, String frame, int strip, int pixel) {
        return format("mappings/Mapping-%s-%s-%02d-%04d%s", scanId, frame, strip, pixel, ".png");
    }

    public void settings() {
        size(1920, 1080);
        pixelDensity(1);
    }

    @Override
    public void setup() {
        balls.setup(this);
        pusherMan = new PusherMan(DEBUG_NOISY);
        pusherMan.init();
    }

    protected void drawPattern(PImage img) {
        PGraphics pg = balls.draw(this, img.width, img.height);
        // absorb the graphics pixels into the image, Processing manual says do it like this:
        img.loadPixels();
        img.pixels = pg.pixels;
        img.updatePixels();
    }

    protected void doSplashScreen() {
        background(145, 71, 67, 255);
        String urlString = getResourceFileOrUrl("dragon-logo.png");
        PImage logo = loadImage(urlString);
        image(logo, width / 2 - logo.width / 2, height / 2 - logo.height / 2, logo.width, logo.height);
        String brandText = "Bhima Dragonmind";
        textAlign(CENTER);
        textSize(48);
        fill(255, 255, 255, 200);
        PFont brandFont = loadFont(getResourceFileOrUrl("BlackmoorLetPlain-48.vlw"));
        textFont(brandFont);

        int textMarginTop = 100;
        String version = "unknown";
        text(brandText, width / 2, height / 2 + logo.height / 2 + textMarginTop);
        try {
            Properties p = new Properties();
            InputStream propFile = getClass().getClassLoader().getResourceAsStream(PROP_FILE);
            if (propFile != null) {
                p.load(propFile);
                version = p.getProperty("version").trim();
            } else {
                System.out.println(PROP_FILE + " missing");
            }
        } catch (IOException e) {
            // NOTE This indicates a build problem, but we'd rather not explode here
            System.out.println("Could not load " + PROP_FILE + " because " + e.getMessage());
            //e.printStackTrace();
        }
        PFont versionFont = loadFont(getResourceFileOrUrl("HelveticaNeue-BoldItalic-18.vlw"));
        textFont(versionFont, 18);
        textAlign(RIGHT);
        text("version: " + version, width - 350, height - 100, 280, 80);
    }

    static String getResourceFileOrUrl(String resourceName) {
        URL resource = DragonMind.class.getClassLoader().getResource(resourceName);
        String extUrl = resource.toExternalForm();
        if (extUrl.startsWith("file:/")) {
            extUrl = extUrl.substring("file:".length());
        }
        return extUrl;
    }

    public PusherMan getPusherMan() {
        return pusherMan;
    }

    protected GLabel label(String text, float x, float y, float w, float h) {
        return label(text, x, y, w, h, GAlign.LEFT);
    }

    protected Pair<GLabel, GLabel> labelPair(String key, Integer val, float x, float y, float w, float h) {
        return labelPair(key, Integer.toString(val), x, y, w, h);
    }

    protected Pair<GLabel, GLabel> labelPair(String key, String val, float x, float y, float w, float h) {
        GLabel left = label(key, x, y, w, h, GAlign.RIGHT);
        GLabel right = label(val, x + w, y, w, h, GAlign.LEFT);
        return new ImmutablePair<>(left, right);
    }

    protected GLabel label(String title, float x, float y, float w, float h, GAlign halign) {
        GLabel glabel = new GLabel(this, x, y, w, h);
        glabel.setOpaque(true);
        glabel.setAlpha(200);
        glabel.setText(title, halign, GAlign.MIDDLE);
        return glabel;
    }

    protected void mapSurfaceToPixels(PImage pImage, List<PixelPoint> pixelPoints) {
        if (getPusherMan().isReady()) {
            List<Strip> strips = getPusherMan().getStrips();
            for (PixelPoint pp : pixelPoints) {
                // TODO fix this HACK, need to edit mapped strip nums in config
                int mappedStripNum = pp.getStrip();
                int actualStripNum= mappedStripNum;
                switch (mappedStripNum) {
                    case 5:
                        actualStripNum = 2;
                        break;
                    case 6:
                        actualStripNum = 10;
                        break;
                    case 7:
                        actualStripNum = 1;
                        break;
                    case 8:
                        actualStripNum = 3;
                        break;
                    case 9:
                        actualStripNum = 20;
                        break;
                    case 12:
                        actualStripNum = 8;
                        break;
                    case 17:
                        actualStripNum = 19;
                        break;
                }

                if (actualStripNum != mappedStripNum) {
                    logger.debug(String.format("hackmap: strip %d -> %d", mappedStripNum, actualStripNum));
                }
                Strip strip = strips.get(actualStripNum);
                // translate local strip number into global

                // TODO big fat wrong: check this out - trying to get the screen x,y for the transformed x and y of the model
                int ppx = (int) screenX(pp.getX(),pp.getY());
                int ppy = (int) screenY(pp.getX(), pp.getY());
                int targetColour = pImage.get(ppx, ppy);
                logger.info(String.format("(%d,%d)@s%dp%d: %h", ppx, ppy, pp.getStrip(), pp.getPixel(), targetColour));
                if (!(ppx >= 0 && ppx < width) || !(ppy >= 0 && ppy < height)) {
                    logger.error(String.format("out of bounds pixel: %d, %d", ppx, ppy));
                }
                strip.setPixel(targetColour, pp.getPixel());
            }
        }
    }

    /**
     * Draws the LED points and connecting wires in configured model space.
     *
     * @param pixels
     * @param lineAlpha
     * @param rainbow
     * @param colours
     * @param highlight
     * @param brightHighlight
     * @param wire
     * @param strongForeground
     */
    protected void drawPoints(List<PixelPoint> pixels,
                              int lineAlpha,
                              boolean rainbow,
                              List<NamedColour> colours,
                              int highlight,
                              int brightHighlight,
                              int wire,
                              int strongForeground) {

        pushStyle();
        ellipseMode(CENTER);
        for (int i = 0; i < pixels.size(); i++) {
            PixelPoint pixel = pixels.get(i);
            if (i > 0) {
                pushStyle();
                PixelPoint prev = pixels.get(i - 1);
                stroke(wire);
                line(prev.getX(), prev.getY(), pixel.getX(), pixel.getY());
                popStyle();
            }
            // now draw the actual point
            if (highlight == i) {
                pushStyle();

                stroke(brightHighlight);
                noFill();
                ellipse(pixel.getX(), pixel.getY(), 30, 30);

                stroke(brightHighlight);
                ellipse(pixel.getX(), pixel.getY(), 5, 5);
                popStyle();
            } else {
                noFill();
                if (rainbow) {
                    NamedColour c = colours.get(pixel.getStrip() % colours.size());
                    stroke(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha);
                    fill(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha);
                } else {
                    stroke(strongForeground);
                    fill(strongForeground);
                }
                ellipse(pixel.getX(), pixel.getY(), 8, 8);
            }
        }
        popStyle();
    }

    protected Config saveConfigToFirstArgOrDefault(Config config) throws IOException {
        return (args != null && args.length > 0)? config.save(args[0]): config.save();
    }
    protected Config loadConfigFromFirstArgOrDefault() throws IOException {
        return (args != null && args.length > 0)? Config.load(args[0]): Config.load();
    }
}
