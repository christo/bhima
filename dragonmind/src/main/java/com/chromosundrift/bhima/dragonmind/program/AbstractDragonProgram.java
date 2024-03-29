package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public abstract class AbstractDragonProgram implements DragonProgram {

    /**
     * Does nothing, override me.
     */
    @Override
    public void settings(DragonMind mind) {
    }

    /**
     * Converts an {@link Image} to a {@link BufferedImage} using {@link Graphics2D} to scale it.
     */
    protected static BufferedImage imageToBufferedImage(Image image, int x, int y, int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(image, x, y, w, h, null);
        g2d.dispose();
        return bi;
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

    @Override
    public boolean isMute() {
        return true;
    }

    @Override
    public void setMute(boolean mute) {
        // does nothing by default
    }
}
