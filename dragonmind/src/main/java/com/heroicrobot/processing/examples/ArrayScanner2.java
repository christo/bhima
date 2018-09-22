package com.heroicrobot.processing.examples;

// Automatic mapping generator for PixelPusher
// by jas@heroicrobot.com
// reworked by christo

import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.Mapper;
import com.chromosundrift.bhima.dragonmind.Palette;
import com.chromosundrift.bhima.dragonmind.ProcessingBase;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Point;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Capture;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;


public class ArrayScanner2 extends DragonMind {

    private boolean drawAllImages = true;
    private Strip previousPixelStrip = null;
    private int previousPixel = 0;
    private int brightnessThreshold = 150;
    private boolean drawAllPoints = true;
    private boolean drawStatusMap = true;
    private long scanId;

    private PFont statusFont;

    private int fontSize;

    private Capture video;

    private volatile PImage darkframe;
    private volatile PImage lightframe;
    private volatile PImage difference;
    private volatile PImage bgImage;
    private volatile PImage currentFrame;
    private volatile PImage cumulative;
    private boolean camReady;
    private String camera = null;
    private Mapper.Mode mode = Mapper.Mode.WAIT;

    private int nStrip;
    private int nPixel;
    private Palette palette;

    private ArrayList<PixelPoint> displayMap;

    private String statusText = "";
    private String statusText2 = "";

    private ArrayList<Point> scanForLights(PImage deltaImage, float brightnessThreshold) {

        ArrayList<Point> hits = new ArrayList<>();
        for (int y = 0; y < deltaImage.height; y++) {
            for (int x = 0; x < deltaImage.width; x++) {

                float brightness = brightness(deltaImage.get(x, y));

                if (brightness > brightnessThreshold) {

                    hits.add(new Point(x, y));
                }
            }
        }
        return hits;
    }

    public void setup() {
        super.setup();

        background(0);
        colorMode(RGB, 255, 255, 255, 255);
        palette = new Palette(color(255, 255, 255), color(0, 0, 0));
        rectMode(CORNER);
        camReady = false;

        doSplashScreen();
        statusText = "ready to initialise camera";
        nPixel = 0;
        nStrip = 0;
        displayMap = new ArrayList<>();
        fontSize = 20;
        statusFont = createFont("Helvetica", fontSize, true);

        bgImage = createImage(width, height, RGB);
    }

    private void camList() {
        camera = Mapper.highestBandwidthCamera(Capture.list());
        video = new Capture(this, camera);
        video.start();
        camInit();
    }

    private void camInit() {
        if (video.available()) {
            video.read();
            currentFrame = video.get();

            println("camera frame: " + currentFrame.width + "x" + currentFrame.height + " @ " + video.frameRate + "fps");
            println("window dimensions: " + width + "x" + height);

            setMainImage(currentFrame, "current frame");
            renderMainImage();
            darkframe = createImage(currentFrame.width, currentFrame.height, RGB);
            lightframe = createImage(currentFrame.width, currentFrame.height, RGB);
            difference = createImage(currentFrame.width, currentFrame.height, RGB);
            cumulative = createImage(currentFrame.width, currentFrame.height, RGB);
            camReady = true;
        }
    }

    private void setMainImage(PImage image, String statusText) {
        bgImage = image;
        this.statusText = statusText;
    }

    public void draw() {

        clear();
        textAlign(LEFT);
        getPusherMan().ensureReady();

        if (camera == null) {
            camList();
        }
        if (!camReady) {
            camInit();
        } else {
            if (mode == Mapper.Mode.CAM_SCAN) {
                doCamScan();
            } else if (mode == Mapper.Mode.PATTERN) {
                drawPattern(bgImage);
                renderMainImage();
                if (displayMap.size() > 0) {
                    mapSurfaceToPixels(bgImage);
                    return;
                }
            } else {
                if (video.available()) {
                    video.read();
                    currentFrame = video.get();
                    setMainImage(currentFrame, "current frame");
                }
                if (mode != Mapper.Mode.WAIT) {
                    drawTestMode();
                }
            }
            renderMainImage();
            if (displayMap.size() > 0) {
                if (drawAllPoints) {
                    PixelPoint prev = null;
                    for (PixelPoint pixelPoint : displayMap) {
                        if (prev != null && prev.getStrip() == pixelPoint.getStrip()) {
                            // draw wire from previous to current
                            stroke(10, 230, 170, 200);
                            line(prev.getX(), prev.getY(), pixelPoint.getX(), pixelPoint.getY());
                        }
                        prev = pixelPoint;
                        drawCrossHair(pixelPoint.getPoint());
                    }
                } else {
                    drawCrossHair(displayMap.get(displayMap.size() - 1).getPoint());
                }
            }

        }
        drawStatus();
        if (drawAllImages && currentFrame != null && mode != Mapper.Mode.PATTERN) {
            renderAllImges();
        }
        if (getPusherMan().isReady() && getPusherMan().getStrips().size() > nStrip && mode == Mapper.Mode.CAM_SCAN) {
            drawProgressBar();
        }
        if (drawStatusMap) {
            drawStatusMap();
        }
    }


    private void mapSurfaceToPixels(PImage pImage) {
        if (getPusherMan().isReady()) {
            List<Strip> strips = getPusherMan().getStrips();
            for (PixelPoint pp : displayMap) {
                Strip strip = strips.get(pp.getStrip());
                int targetColour = pImage.get(pp.getX(), pp.getY());
                strip.setPixel(targetColour, pp.getPixel());
            }
        }
    }

    private void drawTestMode() {
        switch (mode) {
            case TEST1:
                testPixels(1);
                break;
            case TEST2:
                testPixels(2);
                break;
            case TEST3:
                testPixels(3);
                break;
            case TEST4:
                testPixels(4);
                break;
            case TEST5:
                testPixels(5);
                break;
            case TEST6:
                testPixels(6);
                break;
        }
    }

    private void renderAllImges() {
        float wid = (float) width / 5; // divide screen by number of images
        float scaleF = wid / currentFrame.width;
        int imgNum = 0;
        labelledImage("current", currentFrame, wid * imgNum++, 0, wid, currentFrame.height * scaleF);
        labelledImage("light", lightframe, wid * imgNum++, 0, wid, lightframe.height * scaleF);
        labelledImage("difference", difference, wid * imgNum++, 0, wid, difference.height * scaleF);
        labelledImage("dark", darkframe, wid * imgNum++, 0, wid, darkframe.height * scaleF);

        PImage cumImg = imageOrNoise(cumulative);
        labelledImage("cumulative", cumImg, wid * imgNum++, 0, wid, cumImg.height * scaleF);
    }

    private void renderMainImage() {
        renderScaled(imageOrNoise(bgImage), 1);
    }

    private void testPixels(int stripToTurnOn) {
        log("testing strip " + stripToTurnOn);
        List<Strip> strips = getPusherMan().getStrips();
        // pixelpusher strips are numbered on the PCB starting from 1
        int sNum = 1;
        for (Strip strip : strips) {
            int c = palette.getDark();
            if (sNum == stripToTurnOn) {
                c = palette.getLight();
            }
            // pixels numbered from a 0 base
            for (int i = 0; i < strip.getLength(); i++) {
                // only light every 10th to reduce power

                // 1 in 10 chaser changing each 200 millis
                long sec = System.currentTimeMillis() / 200;
                if (i % 10 == sec % 10) {
                    strip.setPixel(c, i);
                } else {
                    strip.setPixel(palette.getDark(), i);
                }

            }
            sNum++;
        }
    }

    private void drawStatus() {
        // dark semi-transparent background box at bottom of window with light text on it
        noStroke();
        fill(0, 0, 0, 127);
        rect(0, height - 60, width, 60);
        stroke(255, 255, 255, 255);
        fill(255, 255, 255, 255);
        textFont(statusFont, fontSize);
        textSize(fontSize);
        textAlign(LEFT);
        String extendedStatus = mode + " | " + (camReady ? "CAM READY | " : "CAM NOT READY | ")
                + palette.getColourName() + " | lights found: " + displayMap.size() + "\n" + statusText2;

        String ppStatus = getPusherMan().report();
        outlinedText(statusText + " | " + extendedStatus + " | " + ppStatus, 20, height - 37);
        // right hand detail panel
        drawStatusMap();
    }

    private void doCamScan() {

        // scrape for the strips
        if (getPusherMan().isReady()) {

            List<Strip> strips = getPusherMan().getStrips();
            int number_of_strips = strips.size();
            if (nStrip >= number_of_strips) {
                nStrip = 0;
                log("Done last strip.");
            }
            Strip strip = strips.get(nStrip);
            int length_of_strip = strip.getLength();
            if (nPixel >= length_of_strip) {
                log("finished strip " + nStrip + " at pixel " + nPixel);
                nPixel = 0;
            }

            if (atBeginning()) {
                log("clearing all pixels");
                getPusherMan().turnOffAllPixels();
                scanId = System.currentTimeMillis();
                doDarkFrame();
            }

            // turn off the pixel we just did
            if (previousPixelStrip != null) {
                previousPixelStrip.setPixel(color(0, 0, 0), previousPixel);
                waitForPixelUpdate();
            }

            palette.setLight();
            log("setting pixel " + nStrip + "/" + nPixel + " to " + (palette.getColourName()));
            strip.setPixel(palette.getColour(), nPixel);

            waitForPixelUpdate();

            doLightFrame();

            previousPixelStrip = strip;
            previousPixel = nPixel;

            // move on to the next
            nPixel++;
            if (nPixel >= length_of_strip) {
                nPixel = 0;
                nStrip++;

                if (nStrip >= number_of_strips) {
                    writeMapping();
                }
            }
        } else {
            log("pusherman not ready");
        }

    }

    private void waitForPixelUpdate() {
        // NASTY HACK TODO FIX
        long bedTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - bedTime) < 10) {
            Thread.yield();
        }
    }

    private void log(String message) {
        println(message);
        statusText2 = message;
    }

    private boolean atBeginning() {
        return nStrip == 0 && nPixel == 0;
    }

    private void doDarkFrame() {
        // we were doing the dark frame
        while (!video.available()) {
            Thread.yield(); // TODO get rid of this shit
        }
        video.read();
        log("updating dark frame");
        darkframe.set(0, 0, video);
        saveScanImage(darkframe, "darkframe", nStrip, nPixel);
    }

    private void saveScanImage(PImage image, String frameName, int strip, int pixel) {
        offload(() -> {
            try {
                String prefix = "mappings/Mapping";
                image.save(format("%s-%d-%s-%02d-%04d%s", prefix, scanId, frameName, strip, pixel, ".png"));
            } catch (RuntimeException e) {
                log(format("thread %s error %s", currentThread().getName(), e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private void writeMapping() {
        log("finished all strips, checking for mappings");
        if (displayMap.size() == 0) {
            log("did not capture any points :(");
        } else {

            String filename = "mappings/Mapping" + scanId + ".csv";
            log("saving Mapping file " + filename);
            OutputStream saveout = createOutput(filename);
            PrintStream ps = new PrintStream(saveout, true);
            ps.println(PixelPoint.columnHeaders());
            for (PixelPoint m : displayMap) {
                String x = m.toString();
                ps.println(x);
            }

            ps.close();

            mode = Mapper.Mode.PATTERN;
        }
    }

    private void doLightFrame() {
        while (!video.available()) {
            Thread.yield(); // TODO get rid of this shit
        }
        log("about to read light video frame");

        video.read();

        lightframe.set(0, 0, video);
        saveScanImage(lightframe.copy(), "lightframe", nStrip, nPixel);
        if (cumulative == null) {
            cumulative = lightframe.copy();
        }

        difference.set(0, 0, video);
        difference.blend(darkframe, 0, 0, lightframe.width, lightframe.height,
                0, 0, darkframe.width, darkframe.height, SUBTRACT);
        // blend only above a brightness threshold to ensure ambient light noise doesn't build up
        PImage threshold = new PImage(difference.width, difference.height, RGB);
        threshold.set(0, 0, difference);
        threshold.filter(THRESHOLD, 0.3f);
        cumulative.blend(threshold, 0, 0, cumulative.width, cumulative.height,
                0, 0, darkframe.width, darkframe.height, ADD);

        List<Point> hits = scanForLights(difference, brightnessThreshold);

        if (hits.size() > 0) {
            Point centroid = Point.centroid(hits);
            log("For strip (" + nStrip + "," + nPixel + ") centroid is (" + centroid + ")");
            displayMap.add(new PixelPoint(centroid, nStrip, nPixel));

            if (displayMap.size() > 3 && mapVariance() < 20) {
                log(format("Low map variance: %.2f", mapVariance()));
            }

        } else {
            log("Scan did not find any lights");
        }
    }

    private void drawStatusMap() {
        textAlign(RIGHT);
        int margin = 5;
        int w = 140;
        int x = width - w - 10;
        int lh = 20;
        int y = 300;

        int pos = 0;

        fill(0, 0, 0, 127);
        stroke(255);
        rect(x, y, width - x, 3 * lh);

        // create the map of stuff to show
        fill(255);
        textBoxPair("Strips", Integer.toString(getPusherMan().numStrips()), x, y + (lh * pos++), w, margin, lh);
        textBoxPair("Strip #", Integer.toString(nStrip), x, y + (lh * pos++), w, margin, lh);
        textBoxPair("Pixel #", Integer.toString(nPixel), x, y + (lh * pos++), w, margin, lh);

        noFill();
    }

    private void drawProgressBar() {

        int x = width - 130;
        int y = height - 40;
        int w = 100;
        int h = 20;
        int c = color(230, 40, 40);

        noFill();
        stroke(c);
        strokeWeight(1);
        rect(x, y, w, h);

        List<Strip> strips = getPusherMan().getStrips();

        int pixelsPerStrip = strips.get(nStrip).getLength();
        float pixelsDone = (nStrip * pixelsPerStrip) + nPixel + 1;
        int totalPixels = strips.size() * pixelsPerStrip;
        float completion = pixelsDone / totalPixels;
        fill(c);
        noStroke();
        rect(x, y, ((float) w * completion), h);

    }

    private void drawCrossHair(Point point) {
        // TODO matrix transform to get this overlay correct under
        stroke(10, 230, 170);
        strokeWeight(1);
        // scale centroid from camera space to screen space
        int screenX = (int) (point.getX() * ((float) bgImage.width / width));
        int screenY = (int) (point.getY() * ((float) bgImage.height / height));
        crossHair(new Point(screenX, screenY), 50f);
    }

    private double mapVariance() {
        List<Point> points = new ArrayList<>();
        for (PixelPoint pixelPoint : displayMap) {
            points.add(pixelPoint.getPoint());
        }
        return Point.variance(points);
    }

    public void keyPressed() {
        if (key == 'q') {
            setMainImage(lightframe, "lightframe");
        }
        if (key == 'w') {
            setMainImage(difference, "difference");
        }
        if (key == 'e') {
            setMainImage(darkframe, "darkframe");
        }
        if (key == 'i') {
            setMainImage(currentFrame, "current frame");
        }
        if (key == 'c') {
            setMainImage(cumulative, "cumulative");
        }
        if (key == '0') {
            statusText = "cam scanning";
            mode = Mapper.Mode.CAM_SCAN;
        }
        if (key == '1') {
            statusText = "testing strip 1";
            mode = Mapper.Mode.TEST1;
        }
        if (key == '2') {
            statusText = "testing strip 2";
            mode = Mapper.Mode.TEST2;
        }
        if (key == '3') {
            statusText = "testing strip 3";
            mode = Mapper.Mode.TEST3;
        }
        if (key == '4') {
            statusText = "testing strip 4";
            mode = Mapper.Mode.TEST4;
        }
        if (key == '5') {
            statusText = "testing strip 5";
            mode = Mapper.Mode.TEST5;
        }
        if (key == '6') {
            statusText = "testing strip 6";
            mode = Mapper.Mode.TEST6;
        }
        if (key == 'z') {
            drawAllImages = !drawAllImages;
        }
        if (key == 'a') {
            drawAllPoints = !drawAllPoints;
        }
        if (key == 'r') {
            restart();
        }
        if (key == 's') {
            drawStatusMap = !drawStatusMap;
        }
        if (key == ' ') {
            if (mode == Mapper.Mode.WAIT) {
                mode = Mapper.Mode.CAM_SCAN;
                setMainImage(lightframe, "lightframe");
            } else {
                mode = Mapper.Mode.WAIT;
            }
        }
        if (key == ';') {
            log("clearing all pixels");
            getPusherMan().turnOffAllPixels();
        }

    }

    private void restart() {
        log("restarting");
        displayMap.clear();
        // reset the cumulative image
        cumulative = null; // TODO really?
        nPixel = 0;
        nStrip = 0;
    }

    /**
     * TODO pull up into {@link ProcessingBase}
     */
    public static void main(String[] args) {
        // TODO fix dependency on Processing native libs
        //System.setProperty("jogl.debug", "true");
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(ArrayScanner2.class, args);
    }

}
