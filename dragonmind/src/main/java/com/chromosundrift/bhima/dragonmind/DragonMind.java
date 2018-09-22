package com.chromosundrift.bhima.dragonmind;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.IOException;
import java.util.Properties;

/**
 * Processing-based app for running patterns.
 */
public class DragonMind extends ProcessingBase {

    public static final String PROP_FILE = "build.properties";

    BallsProgram balls = new BallsProgram();

    @Override
    public void setup() {
        balls.setup(this);
    }

    protected void drawPattern(PImage img) {
        PGraphics pg = balls.draw(this, img.width, img.height);;
        // absorb the graphics pixels into the image, Processing manual says do it like this:
        img.loadPixels();
        img.pixels = pg.pixels;
        img.updatePixels();
    }

    protected void doSplashScreen() {
        background(145, 71, 67, 255);
        PImage logo = loadImage("dragon-logo.png");
        image(logo, width / 2 - logo.width / 2, height / 2 - logo.height / 2, logo.width, logo.height);
        String brandText = "Bhima Dragonmind";
        Properties p = new Properties();
        textAlign(CENTER);
        textSize(48);
        fill(255, 255, 255, 200);
        PFont brandFont = loadFont("BlackmoorLetPlain-48.vlw");
        textFont(brandFont);

        int textMarginTop = 100;
        String version = "unknown";
        text(brandText, width / 2, height / 2 + logo.height / 2 + textMarginTop);
        try {
            p.load(getClass().getClassLoader().getResourceAsStream(PROP_FILE));
            version = p.getProperty("version").trim();
        } catch (IOException e) {
            // NOTE This indicates a build problem, but we'd rather not explode
            System.out.println("Could not load " + PROP_FILE);
            e.printStackTrace();
        }
        PFont versionFont = loadFont("HelveticaNeue-BoldItalic-18.vlw");
        textFont(versionFont, 18);
        textAlign(RIGHT);
        text("version: " + version, width - 350, height - 100, 280, 80);
    }
}
