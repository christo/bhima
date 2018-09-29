package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Background;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Point;
import com.chromosundrift.bhima.dragonmind.model.Rect;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.ROTATE;
import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.SCALE;
import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.TRANSLATE;

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
        pushStyle();
        // poor man's outline
        pushStyle();
        fill(0, 0, 0, 127);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                text(label, v1 + x, v2 + y, v3, v4);
            }
        }
        popStyle();
        fill(255);
        text(label, v1, v2, v3, v4);
        popStyle();
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

    protected void textBoxPair(String text1, String text2, float x, float y, float fullWidth, float margin, float h) {
        // draw the left text
        textAlign(RIGHT);
        outlinedText(text1, x, y, fullWidth / 2 - margin, h);
        // draw the value
        textAlign(LEFT);
        outlinedText(text2, x + fullWidth / 2, y, fullWidth / 2, h);
    }

    protected void offload(Runnable runnable) {
        offloader.submit(runnable);
    }

    final boolean isArrow(int keyCode) {
        return keyCode == UP || keyCode == DOWN || keyCode == LEFT  || keyCode == RIGHT;
    }

    protected void rect(Rect b) {
        rect(b.getMinMin().getX(), b.getMinMin().getY(), b.getMaxMax().getX(), b.getMaxMax().getY());
    }

    protected Rect boundingRect(Config config) {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        for (Segment segment : config.getPixelMap()) {
            for (PixelPoint pixel : segment.getPixels()) {
                int x = pixel.getX();
                int y = pixel.getY();
                minx = min(minx, x);
                miny = min(miny, y);
                maxx = max(maxx, x);
                maxy = max(maxy, y);
            }
        }
        return new Rect(new Point(minx, miny), new Point(maxx, maxy));
    }

    protected void applyTransforms(List<Transform> transforms) {
        for (Transform t : transforms) {
            Map<String, Float> params = t.getParameters();
            if (t.is(TRANSLATE)) {
                translate(params.get("x"), params.get("y"));
            } else if (t.is(SCALE)) {
                scale(params.get("x"), params.get("y"));
            } else if (t.is(ROTATE)) {
                // radians
                rotate(params.get("z"));
            }
        }
    }
}
