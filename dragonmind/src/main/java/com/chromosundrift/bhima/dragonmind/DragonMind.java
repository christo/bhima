package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.geometry.PixelPoint;
import com.chromosundrift.bhima.geometry.Point;
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

    private PFont defaultFont;

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
        defaultFont = loadFont(getResourceFileOrUrl("HelveticaNeue-BoldItalic-18.vlw"));
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
        textFont(defaultFont, 18);
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

            for (PixelPoint pp : pixelPoints) {
                Strip strip = getActualStrip(pp);
                int ppx = (int) screenX(pp.getX(), pp.getY());
                int ppy = (int) screenY(pp.getX(), pp.getY());
                int targetColour = pImage.get(ppx, ppy);
                //logger.info(String.format("(%d,%d)@s%dp%d: %h", ppx, ppy, pp.getActualStrip(), pp.getPixel(), targetColour));
                if (!(ppx >= 0 && ppx < width) || !(ppy >= 0 && ppy < height)) {
                    logger.error(String.format("out of bounds pixel: %d, %d", ppx, ppy));
                }
                strip.setPixel(targetColour, pp.getPixel());
            }
        }
    }

    private int getActualStripNum(int mappedStripNum) {
        // TODO fix this HACK, need to edit mapped strip nums in config
        int actualStripNum = mappedStripNum;
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

        if (actualStripNum != mappedStripNum && logger.isDebugEnabled()) {
            logger.debug(String.format("hackmap: strip %d -> %d", mappedStripNum, actualStripNum));
        }
        return actualStripNum;
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
     * @param runLights
     */
    protected void drawPoints(List<PixelPoint> pixels,
                              int lineAlpha,
                              boolean rainbow,
                              List<NamedColour> colours,
                              int highlight,
                              int brightHighlight,
                              int wire,
                              int strongForeground, boolean runLights) {

        pushStyle();
        ellipseMode(CENTER);
        Point prevPoint = null;
        for (int i = 0; i < pixels.size(); i++) {
            PixelPoint pixel = pixels.get(i);
            Point sPoint = modelToScreen(pixel.getPoint());
            pushMatrix();
            resetMatrix();
            if (prevPoint != null) {
                pushStyle();
                stroke(wire);
                line(prevPoint.getX(), prevPoint.getY(), sPoint.getX(), sPoint.getY());
                popStyle();
            }
            // now draw the actual point
            if (highlight == i) {
                if (runLights) {
                    turnOnLed(pixel);
                }
                pushStyle();
                stroke(brightHighlight);
                noFill();
                ellipse(sPoint.getX(), sPoint.getY(), 15, 15);
                popStyle();
            } else {
                if (runLights) {
                    turnOffLed(pixel);
                }
                noFill();
                if (rainbow) {
                    NamedColour c = colours.get(pixel.getStrip() % colours.size());
                    stroke(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha);
                    fill(c.getRed(), c.getGreen(), c.getBlue(), lineAlpha);
                } else {
                    stroke(strongForeground);
                    fill(strongForeground);
                }
                ellipse(sPoint.getX(), sPoint.getY(), 4, 4);
            }
            popMatrix();
            prevPoint = sPoint;
        }
        popStyle();
    }

    private void turnOnLed(PixelPoint pp) {
        setLed(pp, color(255));
    }

    private void turnOffLed(PixelPoint pp) {
        setLed(pp, color(0));
    }

    private void setLed(PixelPoint pp, int colour) {
        PusherMan pusherMan = getPusherMan();
        pusherMan.ensureReady();
        if (pusherMan.isReady()) {
            Strip strip = getActualStrip(pp);
            if (strip != null && pp.getPixel() >= 0) {
                strip.setPixel(colour, pp.getPixel());
            }
        }
    }

    private Strip getActualStrip(PixelPoint pp) {
        List<Strip> strips = pusherMan.getStrips();
        int actualStripNum = getActualStripNum(pp.getStrip());
        if (strips.size() > actualStripNum && actualStripNum >= 0) {
            Strip strip = strips.get(actualStripNum);
            return strip;
        } else {
            return null;
        }
    }

    protected Config saveConfigToFirstArgOrDefault(Config config) throws IOException {
        return (args != null && args.length > 0) ? config.save(args[0]) : config.save();
    }

    protected Config loadConfigFromFirstArgOrDefault() throws IOException {
        return (args != null && args.length > 0) ? Config.load(args[0]) : Config.load();
    }

    protected PFont getDefaultFont() {
        return defaultFont;
    }

    @Override
    public void pushMatrix() {
        debugIfNotAnimationThread("pushing matrix");
        super.pushMatrix();
    }

    @Override
    public void popMatrix() {
        debugIfNotAnimationThread("popping matrix");
        super.popMatrix();
    }

    @Override
    public void pushStyle() {
        debugIfNotAnimationThread("pushing style");
        super.pushStyle();
    }

    @Override
    public void popStyle() {
        debugIfNotAnimationThread("popping style");
        super.popStyle();
    }

    private void debugIfNotAnimationThread(final String mesg) {
        if (!Thread.currentThread().getName().equals("main-FPSAWTAnimator#00-Timer0")) {
            logger.debug(mesg);
        }
    }
}
