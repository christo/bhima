package com.heroicrobot.processing.examples;

// based on Automatic mapping generator for PixelPusher by jas@heroicrobot.com ; reworked by christo

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
import g4p_controls.GEditableTextControl;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GLabel;
import g4p_controls.GTextField;
import g4p_controls.GValueControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Capture;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static com.chromosundrift.bhima.dragonmind.Mapper.Mode.CAM_SCAN;
import static com.chromosundrift.bhima.dragonmind.Mapper.Mode.TEST;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;

// TODO load bgimage and map editor
@SuppressWarnings("unused")
public class ArrayScanner2 extends DragonMind {

    final static Logger logger = LoggerFactory.getLogger(ArrayScanner2.class);
    private int startStrip = 0;
    private int startPixel = 0;
    private int stopStrip = 0;

    private boolean drawAllImages = true;
    private Strip previousPixelStrip = null;
    private int previousPixel = 0;
    private int brightnessThreshold = 100;
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
    private GKnob brightnessKnob;
    private GLabel brightnessLabel;
    private GLabel nPushersLabel;
    private GLabel nStripsLabel;
    private GLabel nStripLabel;
    private GLabel nPixelLabel;
    private GLabel modeLabel;
    private final int crosshairColour = color(10, 230, 170);
    private GTextField startStripField;
    private GTextField stopStripField;
    private GButton testButton;
    private GButton prevStripButton;
    private GButton nextStripButton;
    private float smallImageHeight;
    private float smallImageWidth;

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
        if (mode == Mapper.Mode.EDIT) {
            if (config.getBackgroundImage() != null) {
                // TODO config transform
                // TODO local zoom
                bgImage.set(0,0,new PImage(config.getBackgroundImage()));
            }
        } else {
            if (camera == null) {
                camList();
            }
            if (!camReady) {
                camInit();
            } else if (mode == Mapper.Mode.CAM_SCAN) {
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
                    setMainImage(bgImage, "model");
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
            renderMap();

        }
        drawStatus();
        if (drawAllImages && currentFrame != null && mode != Mapper.Mode.PATTERN) {
            renderAllImges();
        }
        if (mode == CAM_SCAN) {
            if (getPusherMan().isReady() && getPusherMan().getStrips().size() > nStrip) {
                drawProgressBar();
            }
        }

        updateGui();
    }

    private void renderMap() {
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

    private void updateGui() {
        brightnessLabel.setText("brighness threshold: " + brightnessThreshold);
        nPushersLabel.setText(Integer.toString(getPusherMan().numPixelPushers()));
        nStripsLabel.setText(Integer.toString(getPusherMan().numStrips()));
        nStripLabel.setText(Integer.toString(nStrip));
        nPixelLabel.setText(Integer.toString(nPixel));
        modeLabel.setText(mode.toString());
        newScanButton.setEnabled(scanNameField.getText() != null);
        testButton.setLocalColorScheme(mode == TEST ? 5 : 0); // TODO wtf are the colour schemes?
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
        testPixels();
    }

    private void renderAllImges() {
        // divide screen by number of images
        smallImageWidth = (float) width / 5;
        float scaleF = smallImageWidth / currentFrame.width;
        int imgNum = 0;
        smallImageHeight = currentFrame.height * scaleF;

        labelledImage("current", currentFrame, smallImageWidth * imgNum++, 0, smallImageWidth, smallImageHeight);
        labelledImage("light", lightframe, smallImageWidth * imgNum++, 0, smallImageWidth, lightframe.height * scaleF);
        labelledImage("difference", difference, smallImageWidth * imgNum++, 0, smallImageWidth, difference.height * scaleF);
        labelledImage("dark", darkframe, smallImageWidth * imgNum++, 0, smallImageWidth, darkframe.height * scaleF);

        PImage cumImg = imageOrNoise(cumulative);
        labelledImage("cumulative", cumImg, smallImageWidth * imgNum++, 0, smallImageWidth, cumImg.height * scaleF);
    }

    private void renderMainImage() {
        renderScaled(imageOrNoise(bgImage), 1);
    }

    private void testPixels() {
        log("testing strip " + testStripNum);
        List<Strip> strips = getPusherMan().getStrips();
        // pixelpusher strips are numbered on the PCB starting from 1
        int sNum = 0;
        for (Strip strip : strips) {
            int c = palette.getDark();
            if (sNum == testStripNum) {
                c = palette.getLight();
            }
            // pixels numbered from a 0 base
            for (int i = 0; i < strip.getLength(); i++) {
                // only light every 10th to reduce power

                // 1 in 10 chaser changing each 200 millis
                long sec = System.currentTimeMillis() / 100;
                if (i % 5 == sec % 5) {
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
        String extendedStatus = (camReady ? "CAM READY | " : "CAM NOT READY | ")
                + palette.getColourName()
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
            if (nStrip > finishStrip) {
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

                if (nStrip > finishStrip) {
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
        while ((System.currentTimeMillis() - bedTime) < 45) {
            Thread.yield();
        }
    }

    private void log(String message) {
        logger.info(message);
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
            segment.setName(currentScanName);
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

        float w = 280;
        float margin = 5;
        float x = width - w - 10; // right aligned
        float lh = 22; // line height
        float vPad = 3; // vertical padding
        float sh = lh + vPad; // shift height
        float y = 300;

        float pos = 0;

        float insetWidth = w - margin - margin;

        float labelWidth = insetWidth / 2;

        nPushersLabel = labelPair("PixelPushers", getPusherMan().numPixelPushers(), x, y + sh * pos++, labelWidth, lh).getRight();
        nStripsLabel = labelPair("Strips", getPusherMan().numStrips(), x, y + sh * pos++, labelWidth, lh).getRight();
        nStripLabel = labelPair("Strip", nStrip, x, y + sh * pos++, labelWidth, lh).getRight();
        nPixelLabel = labelPair("Pixel #", nPixel, x, y + sh * pos++, labelWidth, lh).getRight();
        modeLabel = labelPair("Mode", mode.toString(), x, y + sh * pos++, labelWidth, lh).getRight();

        pos++;

        float halfWit = (w - margin) / 2;

        startStripField = new GTextField(this, x, y + sh * pos, halfWit, lh);
        startStripField.setPromptText("start #");
        startStripField.setOpaque(true);
        startStripField.setAlpha(guiAlpha);

        stopStripField = new GTextField(this, x + halfWit, y + sh * pos, halfWit, lh);
        stopStripField.setPromptText("stop #");
        stopStripField.setOpaque(true);
        stopStripField.setAlpha(guiAlpha);

        pos++;

        int bwidth = 90;
        scanNameField = new GTextField(this, x, y + sh * pos, w - bwidth - margin - margin, lh);
        scanNameField.setPromptText("segment name");
        scanNameField.setOpaque(true);
        scanNameField.setAlpha(guiAlpha);
        newScanButton = new GButton(this, x + w - bwidth - margin, y + sh * pos, bwidth, lh, "Scan");
        newScanButton.setEnabled(false);
        newScanButton.setOpaque(true);
        newScanButton.setAlpha(guiAlpha);

        pos += 2;

        // triple line height
        brightnessKnob = new GKnob(this, x + margin, y + sh * pos, w - margin - margin, lh * 3, 0.8f);
        brightnessKnob.setTurnRange(110, 70);
        brightnessKnob.setTurnMode(GKnob.CTRL_VERTICAL);
        brightnessKnob.setSensitivity(0.7f);
        brightnessKnob.setShowArcOnly(false);
        brightnessKnob.setOverArcOnly(false);
        brightnessKnob.setIncludeOverBezel(false);
        brightnessKnob.setShowTrack(true);
        brightnessKnob.setLimits((float) brightnessThreshold, 15f, 255f);
        brightnessKnob.setShowTicks(true);
        brightnessKnob.setAlpha(guiAlpha);


        pos += 3;
        brightnessLabel = label("brighness threshold: " + brightnessThreshold, x + margin, y + sh * pos++, insetWidth, lh, GAlign.CENTER);

        // test buttons
        float buttonY = y + sh * pos;
        float buttonX = x + margin;
        testButton = button(buttonX, buttonY, bwidth, lh, guiAlpha, "Test");
        prevStripButton = button(x + w / 3, y + sh * pos, bwidth, lh, guiAlpha, "Prev");
        nextStripButton = button(x + w * 2 / 3, y + sh * pos, bwidth, lh, guiAlpha, "Next");
    }

    private GButton button(float x, float y, int w, float h, int alpha, String label) {
        GButton button = new GButton(this, x, y, w, h, label);
        button.setEnabled(true);
        button.setOpaque(true);
        button.setAlpha(alpha);
        return button;
    }

    public void handleKnobEvents(GValueControl knob, GEvent event) {
        log("got knob event (huh huh)");
        if (knob == brightnessKnob) {
            brightnessLabel.setText("brighness threshold: " + brightnessThreshold);
            brightnessThreshold = knob.getValueI();
        }
        log("finished knob event");
    }

    public void handleButtonEvents(GButton button, GEvent event) {
        log("got button event for " + button.getText());
        // Folder selection
        if (button == newScanButton) {
            startNewScan();
        } else if (button == testButton) {
            if (mode == TEST) {
                mode = Mapper.Mode.WAIT;
            } else {
                statusText = "testing mode";
                mode = TEST;
            }
        } else if (button == nextStripButton) {
            if (getPusherMan().isReady()) {
                testStripNum++;
                testStripNum %= getPusherMan().numStrips();
            }
        } else if (button == prevStripButton) {
            if (getPusherMan().isReady()) {
                testStripNum--;
                if (testStripNum < 0) {
                    testStripNum = getPusherMan().numStrips() - 1;
                }
            }
        }
        log("finished handle button events");
    }

    public void handleTextEvents(GEditableTextControl textcontrol, GEvent event) {
        log("starting handle text events");
        boolean scanNameGood = true;
        boolean startAndStopGood = false;
        if (textcontrol == scanNameField) {

            String segmentName = textcontrol.getText().trim();
            if (!segmentName.equals("")) {
                currentScanName = segmentName;
                scanNameGood = true;
            } else {
                scanNameGood = false;
                currentScanName = "untitled"; // should never be actually used
            }
        } else if (textcontrol == startStripField || textcontrol == stopStripField) {
            startAndStopGood = checkStripStartStopFields();
        }

        newScanButton.setEnabled(scanNameGood && startAndStopGood);
        log("finished handle text events");
    }

    private int validateStripNum(GTextField field, String fieldName) {
        if (getPusherMan().isReady()) {
            List<Strip> strips = getPusherMan().getStrips();
            try {
                String text = field.getText().trim();
                if (text.length() != 0) {
                    int i = Integer.parseInt(text);
                    if (i < 1 || i > strips.size() - 1) {
                        log("error " + fieldName + " is out of range (1-" + strips.size());
                    }
                    // good to go
                    return i - 1;
                }

            } catch (NumberFormatException e) {
                log("error: " + fieldName + " is not parseable: " + field.getText());
            }
        }
        return -1;
    }

    private boolean checkStripStartStopFields() {
        boolean bothGood = true;
        int startVal = validateStripNum(startStripField, "start strip field");
        if (startVal >= 0) {
            startStrip = startVal;
        } else {
            bothGood = false;
        }
        int stopVal = validateStripNum(stopStripField, "stop strip field");
        if (stopVal >= 0) {
            stopStrip = stopVal;
        } else {
            bothGood = false;
        }
        return bothGood;
    }

    private void startNewScan() {
        // show scan name request
        log("start new scan");
        restart();
        mode = CAM_SCAN;
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
        // TODO matrix transform to get this overlay correct under map transforms
        stroke(crosshairColour);
        strokeWeight(1);
        // scale centroid from camera space to screen space
        int screenX = (int) (point.getX() * ((float) bgImage.width / width));
        int screenY = (int) (point.getY() * ((float) bgImage.height / height));
        crossHair(new Point(screenX, screenY), 30f);
    }

    private double mapVariance() {
        List<Point> points = new ArrayList<>();
        for (PixelPoint pixelPoint : displayMap) {
            points.add(pixelPoint.getPoint());
        }
        return Point.variance(points);
    }

    @Override
    public void mouseClicked() {
        // if the mouse pointe is inside the small image, switch to that image for the main image
        if (drawAllImages) {
            // only if they're visible
            if (mouseY < smallImageHeight) {
                if (mouseX < smallImageWidth) {
                    setMainImage(currentFrame, "current frame");
                } else if (mouseX < smallImageWidth * 2) {
                    setMainImage(lightframe, "lightframe");
                } else if (mouseX < smallImageWidth * 3) {
                    setMainImage(difference, "difference");
                } else if (mouseX < smallImageWidth * 4) {
                    setMainImage(darkframe, "darkframe");
                } else {
                    setMainImage(cumulative, "cumulative");
                }
            }
        }
    }

    @Override
    public void mouseMoved() {
        if (drawAllImages && mouseY < smallImageHeight) {
            cursor(HAND);
        } else {
            cursor(Cursor.DEFAULT_CURSOR);
        }

    }

    public void keyPressed() {
        // TODO make collapsible small images
        //        if (key == 'z') {
        //            drawAllImages = !drawAllImages;
        //        }
        // whether to show all found points
        //        if (key == 'a') {
        //            drawAllPoints = !drawAllPoints;
        //        }
        //        if (key == 's') {
        //            drawStatusMap = !drawStatusMap; // TODO make GUI collapse
        //        }

        // TODO make button to clear all pixels
        if (key == ';') {
            log("clearing all pixels");
            getPusherMan().turnOffAllPixels();
        }
    }

    private void restart() {
        log("restarting");
        setMainImage(lightframe, "lightframe");
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
        logger.warn("starting");
        // TODO fix dependency on Processing native libs
        //System.setProperty("jogl.debug", "true");
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");

        PApplet.main(ArrayScanner2.class, args);
    }

}
