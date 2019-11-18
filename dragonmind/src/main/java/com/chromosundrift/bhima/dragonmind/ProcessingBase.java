package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.geometry.Point;
import com.chromosundrift.bhima.geometry.Rect;
import mouse.transformed2d.MouseTransformed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.*;

public class ProcessingBase extends PApplet {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingBase.class);

    private final ExecutorService offloader;
    protected MouseTransformed mouseTransformed;

    public ProcessingBase(int offloaderThreads) {
        offloader = Executors.newFixedThreadPool(offloaderThreads);
        Runtime.getRuntime().addShutdownHook(new Thread(offloader::shutdown));
    }

    /**
     * Sets system properties for native libraries that processing depends on.
     */
    protected static void setNativeLibraryPaths() {
        // on osx, the native libraries
        String base = "/Users/christo/src/christo/processing/libraries";
        System.setProperty("gstreamer.library.path", base + "/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", base + "/video/library/macosx64/plugins/");
    }

    @Override
    public void setup() {
        super.setup();
        logger.info("surface implementation: {}", surface.getClass());
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

    @Override
    public PGraphics createGraphics(int w, int h) {
        // prevent exception on creating graphics of width or height <= 0
        return super.createGraphics(max(1, w), max(1, h));
    }

    @Override
    public PGraphics createGraphics(int w, int h, String renderer) {
        // prevent exception on creating graphics of width or height <= 0
        return super.createGraphics(max(1, w), max(1, h), renderer);
    }

    @Override
    public PGraphics createGraphics(int w, int h, String renderer, String path) {
        // prevent exception on creating graphics of width or height <= 0
        return super.createGraphics(max(1, w), max(1, h), renderer, path);
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

    public PImage generateImageNoise(int w, int h) {
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

    protected static float dist(Point p1, Point p2) {
        return dist(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    final boolean isArrow(int keyCode) {
        return keyCode == UP || keyCode == DOWN || keyCode == LEFT || keyCode == RIGHT;
    }

    /**
     * Draw the given Rect.
     *
     * @param r rectangle to draw.
     */
    protected void rect(Rect r) {
        Point minMin = r.getMinMin();
        Point maxMax = r.getMaxMax();
        rect(minMin.getX(), minMin.getY(), maxMax.getX() - minMin.getX(), maxMax.getY() - minMin.getY());
    }

    /**
     * Returns the bound rectangle for the given list of segments in screen space.
     *
     * @param pixelMap the segments.
     * @return a bounding {@link Rect} in screen pixels.
     */
    protected final Rect screenspaceBoundingRect(List<Segment> pixelMap) {
        // TODO flatmap this shit!
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        boolean gotPixels = false;
        for (Segment segment : pixelMap) {
            pushMatrix();
            applyTransforms(segment.getTransforms());
            for (PixelPoint pixel : segment.getPixels()) {
                int x = (int) screenX(pixel.getX(), pixel.getY());
                int y = (int) screenY(pixel.getX(), pixel.getY());
                minx = min(minx, x);
                miny = min(miny, y);
                maxx = max(maxx, x);
                maxy = max(maxy, y);
                gotPixels = true;
            }
            popMatrix();
        }
        if (!gotPixels) {
            // a bit heavy handed perhaps
            throw new IllegalStateException("No pixels found for config!");
        }
        return new Rect(minx, miny, maxx, maxy);
    }

    /**
     * Invokes Processing matrix transforms that correspond to the given list such
     * that subsequent drawing operations occur in the new coordinate space.
     *
     * @param transforms
     */
    protected void applyTransforms(List<Transform> transforms) {
        for (Transform t : transforms) {
            applyTransform(t);
        }
    }

    protected void applyTransform(Transform t) {
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

    protected final Rect modelToScreen(Rect r) {
        return new Rect(modelToScreen(r.getMinMin()), modelToScreen(r.getMaxMax()));
    }

    /**
     * Returns the screen coordinates of the given modelspace point.
     *
     * @param p the point in model space.
     * @return the screenspace projection of p.
     */
    protected final Point modelToScreen(Point p) {
        return modelToScreen(p.getX(), p.getY());
    }

    protected final Point modelToScreen(int x, int y) {
        return new Point((int) screenX(x, y), (int) screenY(x, y));
    }

    protected final Rect calculateScreenBox(Segment segment) {
        Stream<PixelPoint> pixels = segment.getPixels().stream();
        Stream<Point> screenPoints = pixels.map((PixelPoint pp) -> modelToScreen(pp.getPoint()));
        return segment.getBoundingBox(screenPoints);
    }

    protected void applyGlobalTransforms(Config c) {
        applyTransforms(c.getBackground().getTransforms());
    }
}
