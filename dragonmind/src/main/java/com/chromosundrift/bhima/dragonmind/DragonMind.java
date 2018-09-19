package com.chromosundrift.bhima.dragonmind;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Processing-based app for running patterns.
 */
public class DragonMind extends ProcessingBase {

    public static final String PROP_FILE = "build.properties";
    private float t = 0;
    private List<Ball> balls = new ArrayList<>();
    private Random r = new Random();

    protected void setupPattern() {
        for (int i = 0; i < 10; i++) {
            balls.add(new Ball(r.nextInt(width), r.nextInt(height)));
        }
    }

    protected void drawPattern(PImage img) {
        PGraphics pg = createGraphics(img.width, img.height);
        pg.beginDraw();
        pg.ellipseMode(CENTER);
        t += 1.9;
        pg.noStroke();
        pg.fill(0);
        pg.rect(0, 0, img.width, img.height);
        for (Ball ball : balls) {
            pg.fill(color(255, t % 255, ball.y % 255));
            pg.ellipse(ball.x, ball.y, 100, 100);
            float newX = ball.x + r.nextFloat() * 2 - 1;
            float newY = ball.y + r.nextFloat() * 2 - 1;


            ball.x = (int) newX;
            ball.y = (int) newY;
            ball.move(width, height);
        }
        pg.endDraw();
        // draw the graphics to the bgImage
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
            System.out.println("Could not load " + PROP_FILE);
            e.printStackTrace();
        }
        PFont versionFont = loadFont("HelveticaNeue-BoldItalic-18.vlw");
        textFont(versionFont, 18);
        textAlign(RIGHT);
        text("version: " + version, width - 350, height - 100, 280, 80);
    }
}
