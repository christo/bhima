package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.geometry.Point;
import com.chromosundrift.bhima.geometry.Rect;
import static com.chromosundrift.bhima.dragonmind.SystemUtils.checkIsDir;
import static com.chromosundrift.bhima.dragonmind.SystemUtils.sysPropDefault;
import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.*;

/**
 * Somewhat dirty bag of generic drawing and system methods suitable for sharing between any applications, aiming to
 * reduce repetitive cruft in subclasses.
 */
public class ProcessingBase extends PApplet {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingBase.class);
    private static final String GSTREAMER_LIBRARY_PATH = "gstreamer.library.path";
    private static final String GSTREAMER_PLUGIN_PATH = "gstreamer.plugin.path";
    private static final String MD5_FILE = "md5sum.txt";

    /** MD5 to filename */
    private static Map<String, String> nativeLibSums = new HashMap<>(); // default empty in case not recreated later

    private final ExecutorService offloader;

    public ProcessingBase(int offloaderThreads) {
        offloader = Executors.newFixedThreadPool(offloaderThreads);
        Runtime.getRuntime().addShutdownHook(new Thread(offloader::shutdown));
    }

    public ProcessingBase() {
        this(10);
    }

    /**
     * Sets system properties for native libraries that processing depends on.
     * Set these for the Java runtime with the -D option
     */
    protected static void setNativeLibraryPaths() {
        logger.info("setting gstreamer native library paths based on system properties");
        logger.info("operating system name is {}", SystemUtils.getOs());
        if (SystemUtils.isMac()) {
            // TODO fix this hard-coded nonsense
            final String library = "/Users/christo/src/christo/bhima/dragonmind/processing-video-lib/video/library/macosx";
            final String plugins = library + "/gstreamer-1.0";
            logger.info("running on OSX using fixed native library paths at " + library);

            String gsLibPath = sysPropDefault(GSTREAMER_LIBRARY_PATH, library);
            String gsPluginPath = sysPropDefault(GSTREAMER_PLUGIN_PATH, plugins);
            // check these paths exist
            if (checkIsDir(gsLibPath) && checkIsDir(gsPluginPath)) {
                try {
                    final PrintWriter pw = new PrintWriter(MD5_FILE);
                    nativeLibSums = new HashMap<>();
                    writeMd5Sums(pw, new File(library), nativeLibSums);
                    pw.close();

                    compareBuildTimeNativeLibSums();

                } catch (FileNotFoundException e) {
                    logger.error("can't write md5 file", e);
                }
            } else {
                logger.warn("Native library paths are wrong. Video will probably not work correctly");
            }
        } else {
            logger.warn("Not setting native library paths, relying on os");
        }
    }

    private static void compareBuildTimeNativeLibSums() {
        logger.info("starting detecting native library discrepancies");
        // TODO load resource md5sum.txt into map and compare with given map, include missings
        Map<String, String> buildTimeMap = new HashMap<>();
        final InputStream buildTme = ProcessingBase.class.getClassLoader().getResourceAsStream(MD5_FILE);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(buildTme))) {
            Stream<String> lines = br.lines();
            lines.forEach(l -> {
                final String[] s = l.split("  ");
                if (s.length == 2) {
                    buildTimeMap.put(s[0], s[1]);
                } else {
                    throw new RuntimeException("md5 file format problem");
                }
            });
        } catch (Exception e) {
            logger.error("could not load md5 resource", e);
        }
        buildTimeMap.entrySet().stream()
                .filter(entry -> !nativeLibSums.get(entry.getKey()).equals(nativeLibSums.get(entry.getKey())))
                        .forEach(e -> logger.warn("bad native lib: {} {}", e.getKey(), e.getValue()));

        nativeLibSums.entrySet().stream()
                .filter(entry -> !buildTimeMap.get(entry.getKey()).equals(buildTimeMap.get(entry.getKey())))
                .forEach(e -> logger.warn("missing native lib: {} {}", e.getKey(), e.getValue()));
        logger.info("finished detecting native library discrepancies");

    }

    /**
     * Writes MD5 sums for all files under root to pw and also stores them in md5ToFilename. Writes the file
     * in the precise format that gnu coreutils md5sum does it (capitalised-MD5sum two-spaces filename). Note
     * that only the file name is stored and not the path. Do not use the md5 command on macos, instead
     * brew install coreutils
     */
    private static void writeMd5Sums(PrintWriter pw, File root, Map<String, String> md5ToFilename) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("must be called on a dir only");
        } else {
            //noinspection ConstantConditions
            for (File dir : root.listFiles(File::isDirectory)) {
                writeMd5Sums(pw, dir, md5ToFilename);
            }
            //noinspection ConstantConditions
            for (File file : root.listFiles(f -> !f.isDirectory())) {
                try {
                    String md5 = fileToMd5(file);
                    md5ToFilename.put(md5, file.getName());
                    pw.println(md5 + "  " + file.getName());
                } catch (NoSuchAlgorithmException | IOException e) {
                    logger.error("can't generate md5 for file " + file.getName(), e);
                }
            }
        }
    }

    private static String fileToMd5(File file) throws NoSuchAlgorithmException, IOException {
        return pathToMd5(file.toPath());
    }

    private static String pathToMd5(Path path) {
        try {
            logger.info("generating md5 for " + path);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(path));
            return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
        } catch (IOException | NoSuchAlgorithmException e) {
            // can only happen if given a non-file or md5 algo is absent
            // indicates a config error or critical bug
            throw new RuntimeException(e);
        }
    }

    protected static float dist(Point p1, Point p2) {
        return dist(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    @Override
    public void setup() {
        super.setup();
        logger.info("surface implementation: {}", surface.getClass().getName());
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

    // TODO move these sorts of things into a drawing helper which takes an instance of a PApplet at construction
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
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        boolean gotPixels = false;
        for (Segment segment : pixelMap) {
            pushMatrix();
            segment.getTransforms().stream().filter(Transform::isUnbaked).forEach(this::applyTransform);
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

    protected final void applyTransform(Transform t) {
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
        c.getBackground().getTransforms().forEach(this::applyTransform);
    }
}
