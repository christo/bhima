package com.chromosundrift.bhima.dragonmind.program;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class TestPattern {


    static PImage fullCrossHair(PApplet papp, long l2x, long l1y, int width, int height) {
        final PGraphics pg = papp.createGraphics(width, height);
        pg.colorMode(PConstants.RGB, 255);
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

    static PImage cycleTestPattern(PApplet papp, int width, int height) {
        // TODO migrate this into a Program
        final long l1y = (System.currentTimeMillis() / 30) % height;
        final long l2x = (System.currentTimeMillis() / 100) % width;
        return fullCrossHair(papp, width - l2x, l1y, width, height);
    }
}
