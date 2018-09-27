package com.chromosundrift.bhima.dragonmind;

import g4p_controls.GAlign;
import g4p_controls.GLabel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static java.lang.String.format;

/**
 * Processing-based app for running patterns.
 */
public class DragonMind extends ProcessingBase {

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
}
