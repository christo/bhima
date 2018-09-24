package com.heroicrobot.processing.examples;

// Automatic mapping generator for PixelPusher
// by jas@heroicrobot.com
// reworked by christo

import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.Mapper;
import com.chromosundrift.bhima.dragonmind.Palette;
import com.chromosundrift.bhima.dragonmind.ProcessingBase;
import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Point;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import g4p_controls.GAlign;
import g4p_controls.GButton;
import g4p_controls.GDropList;
import g4p_controls.GEditableTextControl;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GLabel;
import g4p_controls.GTextField;
import g4p_controls.GValueControl;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Capture;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;

// TODO load bgimage and map editor
@SuppressWarnings("unused")
public class ArrayScanner2 extends DragonMind {

    private int startStrip = 0;
    private int startPixel = 0;
    private int stopStrip = 0;

    private boolean drawAllImages = true;
    private Strip previousPixelStrip = null;
    private int previousPixel = 0;
    private int brightnessThreshold = 170;
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
    private int testStripNum = 0;

    private int nStrip = startStrip;
    private int nPixel = startPixel;
    private Palette palette;

    private ArrayList<PixelPoint> displayMap;

    private String statusText = "";
    private String statusText2 = "";
    private Config config;
    private GButton newScanButton;
    private GTextField scanNameField;
    private String currentScanName;
    private GDropList startStripSelect;
    private GDropList stopStripSelect;
    private GKnob brightnessKnob;
    private GLabel brightnessLabel;

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
        palette = new Palette(color(127, 127, 127), color(0, 0, 0));
        rectMode(CORNER);
        camReady = false;

        doSplashScreen();
        createGui();
        statusText = "ready to initialise camera";
        nPixel = startPixel;
        nStrip = startStrip;
        displayMap = new ArrayList<>();
        fontSize = 20;
        statusFont = createFont("Helvetica", fontSize, true);

        bgImage = createImage(width, height, RGB);
        try {
            config = Config.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (mode == Mapper.Mode.WAIT) {
                    // TODO load model backgrounds

                    setMainImage(currentFrame, "model");
                }
                if (video.available()) {
                    video.read();
                    currentFrame = video.get();
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
        brightnessLabel.setText("brighness threshold: " + brightnessThreshold);
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
        testPixels(testStripNum);
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
                + palette.getColourName() + " S:P:" + nStrip + ":" + nPixel
                + " | lights found: " + displayMap.size() + "\n" + statusText2;

        String ppStatus = getPusherMan().report();
        outlinedText(statusText + " | " + extendedStatus + " | " + ppStatus, 20, height - 37);

    }

    private void doCamScan() {

        // scrape for the strips
        if (getPusherMan().isReady()) {

            List<Strip> strips = getPusherMan().getStrips();
            // TODO allow to use strips.size() again
            int finishStrip = stopStrip;
            if (nStrip >= finishStrip) {
                nStrip = startStrip;
                log("Done last strip.");
            }
            Strip strip = strips.get(nStrip);
            int length_of_strip = strip.getLength();
            if (nPixel >= length_of_strip) {
                log("finished strip " + nStrip + " at pixel " + nPixel);
                nPixel = startPixel;
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

                if (nStrip >= finishStrip) {
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
        while ((System.currentTimeMillis() - bedTime) < 15) {
            Thread.yield();
        }
    }

    private void log(String message) {
        println(message);
        statusText2 = message;
    }

    private boolean atBeginning() {
        return nStrip == startStrip && nPixel == startPixel;
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
            // writing a fresh config too
            Segment segment = new Segment();
            String filename = "mappings/Mapping" + scanId + ".csv";
            segment.setName("mapping " + scanId);
            segment.setDescription("mapping file is " + filename);
            segment.setPixels(displayMap);
            config.addSegment(segment);
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void createGui() {
        log("creating GUI");

        int guiAlpha = 205;

        float w = 240;
        float margin = 5;
        float x = width - w - 10;
        float lh = 22;
        float vPad = 3;
        float sh = lh + vPad;
        float y = 300;

        float pos = 0;

        float insetWidth = w - margin - margin;

        // create the map of stuff to show

        float labelWidth = insetWidth / 2;

        labelPair("PixelPushers", getPusherMan().numPixelPushers(), x, y + sh * pos++, labelWidth, lh);
        labelPair("Strips", getPusherMan().numStrips(), x, y + sh * pos++, labelWidth, lh);
        labelPair("Strip", nStrip, x, y + sh * pos++, labelWidth, lh);
        labelPair("Pixel #", nPixel, x, y + sh * pos++, labelWidth, lh);

        pos++;

        float halfWit = (w - margin) / 2;
        label("Start:", x, y + sh * pos, halfWit, lh, GAlign.RIGHT);

        startStripSelect = new GDropList(this, x + halfWit + margin, y + sh * pos, halfWit, lh);

        pos ++;

        label("Stop:", x, y + sh * pos, halfWit, lh, GAlign.RIGHT);
        stopStripSelect = new GDropList(this, x + halfWit + margin, y + sh * pos, halfWit, lh);

        // Disable until we get info about what strips there are
        startStripSelect.setItems(singletonList("nothing"), 0);
        startStripSelect.setEnabled(false);
        stopStripSelect.setItems(singletonList("nothing"), 0);
        stopStripSelect.setEnabled(false);

        pos++;

        int buttonWidth = 90;
        scanNameField = new GTextField(this, x, y + sh * pos, w - buttonWidth - margin - margin, lh);
        scanNameField.setPromptText("segment name");
        scanNameField.setOpaque(true);
        scanNameField.setAlpha(guiAlpha);
        newScanButton = new GButton(this, x + w - buttonWidth - margin, y + sh * pos, buttonWidth, lh, "Scan");
        newScanButton.setEnabled(false);
        newScanButton.setOpaque(true);
        newScanButton.setAlpha(guiAlpha);
        getPusherMan().addObserver((o, arg) -> updateStripChoicesGui());

        pos+=2;

        // triple line height
        brightnessKnob = new GKnob(this, x + margin, y + sh * pos, w - margin - margin, lh*3, 0.8f);
        brightnessKnob.setTurnRange(110, 70);
        brightnessKnob.setTurnMode(GKnob.CTRL_VERTICAL);
        brightnessKnob.setSensitivity(0.7f);
        brightnessKnob.setShowArcOnly(false);
        brightnessKnob.setOverArcOnly(false);
        brightnessKnob.setIncludeOverBezel(false);
        brightnessKnob.setShowTrack(true);
        brightnessKnob.setLimits((float)brightnessThreshold, 15f, 255f);
        brightnessKnob.setShowTicks(true);
        brightnessKnob.setAlpha(guiAlpha);


        pos +=3;
        brightnessLabel = label("brighness threshold: " + brightnessThreshold, x+ margin, y + sh * pos++, insetWidth, lh, GAlign.CENTER);
    }

    public void handleDropListEvents(GDropList list, GEvent event) {
        log("drop list event: " + list.getSelectedText());
    }

    public void handleKnobEvents(GValueControl knob, GEvent event) {
        if (knob == brightnessKnob) {
            brightnessLabel.setText("brighness threshold: " + brightnessThreshold);
            brightnessThreshold = knob.getValueI();
        }

    }

    private void updateStripChoicesGui() {
        if (getPusherMan().isReady()) {
            List<Strip> strips = getPusherMan().getStrips();
            List<String> stripChoices = new ArrayList<>(strips.size());
            for (int i = 0; i < strips.size(); i++) {
                Strip strip = strips.get(i);
                stripChoices.add("strip " + strip.getStripNumber());
            }
            if (stripChoices.isEmpty()) {
                startStripSelect.setEnabled(false);
                stopStripSelect.setEnabled(false);
            }
            startStripSelect.setItems(stripChoices, 0);
            stopStripSelect.setItems(stripChoices, 0);
        }
    }

    public void handleButtonEvents(GButton button, GEvent event) {
        // Folder selection
        if (button == newScanButton) {
            startNewScan();
        }
    }

    public void handleTextEvents(GEditableTextControl textcontrol, GEvent event) {
        String segmentName = textcontrol.getText().trim();
        if (!segmentName.equals("")) {
            currentScanName = segmentName;
            newScanButton.setEnabled(true);
        } else {
            currentScanName = "untitled"; // should never be used
            newScanButton.setEnabled(false);
        }
    }

    private void startNewScan() {
        // show scan name request
        log("TODO start new scan");
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
        int totalPixels = 1 + startStrip - stopStrip * pixelsPerStrip;
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
            statusText = "testing mode";
            mode = Mapper.Mode.TEST;
            testStripNum = 1;
        }
        if (key == ']') {
            testStripNum++;
            testStripNum %= getPusherMan().numStrips();
        }
        if (key == '[') {
            testStripNum--;
            if (testStripNum < 0) {
                testStripNum = getPusherMan().numStrips() - 1;
            }
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
        nPixel = startPixel;
        nStrip = startStrip;
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
