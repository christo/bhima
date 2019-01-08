package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;
import processing.core.PImage;

public abstract class AbstractDragonProgram implements DragonProgram {
    /**
     * Does nothing, override me.
     */
    @Override
    public void settings(DragonMind mind) {
    }

    /**
     * Does nothing, override me.
     */
    @Override
    public void setup(DragonMind mind) {

    }

    /**
     * Draws full-frame static.
     */
    @Override
    public PGraphics draw(DragonMind mind, int width, int height) {
        PGraphics pg = mind.createGraphics(width, height);
        pg.beginDraw();
        PImage pImage = mind.generateImageNoise(width, height);
        pg.image(pImage, 0, 0);
        pg.endDraw();
        return pg;
    }

    /**
     * Does nothing, override me.
     *
     * @param mind
     */
    @Override
    public void mouseClicked(DragonMind mind) {

    }
}
