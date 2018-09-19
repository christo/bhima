package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Point;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessingBase extends PApplet {

    private final ExecutorService offloader;

    public ProcessingBase(int offloaderThreads) {
        offloader = Executors.newFixedThreadPool(10);
        Runtime.getRuntime().addShutdownHook(new Thread(offloader::shutdown));
    }

    public ProcessingBase() {
        this(10);
    }

    /**
     * Renders the given image justified top right scaled by the widthScale
     *
     * @param image      the image to render
     * @param widthScale the scale to render it at (e.g. 1.0 full size, 0.5 for half size)
     */
    protected void renderScaled(PImage image, float widthScale) {
        float renderWidth = width * widthScale;
        float renderHeight = (renderWidth / image.width) * image.height;
        image(image, width - renderWidth, 0, renderWidth, renderHeight);
    }

    protected void crossHair(float x, float y, float size) {
        noFill();
        float d = size / 2;
        line(x, y - d, x, y + d);
        line(x - d, y, x + d, y);
        ellipse(x, y, d, d);
    }

    protected void crossHair(Point point, float size) {
        crossHair(point.getX(), point.getY(), size);
    }

    // TODO pull out the duplication here
    protected void outlinedText(String label, float v1, float v2, float v3, float v4) {
        // poor man's outline
        fill(0, 0, 0, 127);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                text(label, v1 + x, v2 + y, v3, v4);
            }
        }
        fill(255);
        text(label, v1, v2, v3, v4);
    }

    protected void outlinedText(String label, float v1, float v2) {
        // poor man's outline
        fill(0, 0, 0, 127);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                text(label, v1 + x, v2 + y);
            }
        }
        fill(255);
        text(label, v1, v2);
    }

    protected void labelledImage(String label, PImage image, float v1, float v2, float v3, float v4) {
        image(image, v1, v2, v3, v4);
        textAlign(LEFT);
        outlinedText(label, v1 + 10, v2 + 20);
    }

    protected PImage generateImageNoise(int w, int h) {
        PImage noise = createImage(w, h, ALPHA);
        Random r = new Random();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                noise.set(x, y, r.nextInt(255));
            }
        }
        return noise;
    }

    protected PImage imageOrNoise(PImage image) {
        return image != null ? image : generateImageNoise(width, height);
    }

    protected void textBoxPair(String text1, String text2, int x, int y, int fullWidth, int margin, int h) {
        // draw the left text
        outlinedText(text1, x, y, fullWidth/2 - margin, h);
        // draw the value
        outlinedText(text2, x + fullWidth/2, y, fullWidth/2, h);
    }


    protected void offload(Runnable runnable) {
        offloader.submit(runnable);
    }
}
