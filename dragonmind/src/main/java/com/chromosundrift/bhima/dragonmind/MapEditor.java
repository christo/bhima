package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type;
import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class MapEditor extends DragonMind {

    private static Logger logger = LoggerFactory.getLogger(MapEditor.class);
    private static final String IMAGES_DIR = "mappings";

    private Config config;
    private PImage bg;
    private float bgX = 0;
    private float bgY = 0;
    private float bgScaleX = 1f;
    private float bgScaleY = 1f;
    private int selectedSegment = 0;

    /**
     * Translation step / rotation factor.
     */
    private int dt = 1;

    /**
     * The rotation step in radians
     */
    private float theta = PI / 200;

    /**
     * The LED to highlight in the editor.
     */
    private int highlight;

    public void settings() {
        fullScreen(P3D);
        pixelDensity(2);
        smooth();
        try {
            restoreConfig();
        } catch (IOException e) {
            logger.error("Could not load config", e);
        }
    }

    private void restoreConfig() throws IOException {
        config = Config.load();
        List<Transform> transforms = config.getBackground().getTransforms();
        slurpBackgroundTransforms(transforms);
    }

    private void slurpBackgroundTransforms(List<Transform> transforms) {
        for (Transform transform : transforms) {
            logger.debug("background transform: " + transform.toString());
            // TODO need to model inheritance in JSON
            Map<String, Float> params = transform.getParameters();
            if (transform.is(TRANSLATE)) {
                logger.info("loading translate transform");
                bgX = params.get("x");
                bgY = params.get("y");
            } else if (transform.is(SCALE)) {
                logger.info("loading scale transform");
                bgScaleX = params.get("x");
                bgScaleY = params.get("y");
            }
            // don't bother with background rotation for now
        }
    }

    public void setup() {
        background(190);
    }

    @Override
    public void draw() {
        fill(190);
        rect(0, 0, width, height);
        pushMatrix();
        translate(bgX, bgY);
        scale(bgScaleX, bgScaleY);
        drawBackground();
        drawSegments();
        popMatrix();
        drawSegmentInfo();
    }

    private void drawSegmentInfo() {
        if (!config.getPixelMap().isEmpty()) {
            Segment segment = config.getPixelMap().get(selectedSegment);
            int margin = 10;
            pushStyle();
            // draw a callout box with the info in the top right, TODO connect to the segment with a line
            float screenX = width - 300;
            float screenY = 50;

            // TODO reproduce background + segment transform arithmetically to get screen coord of bounding box

            noStroke();
            if (segment.getEnabled()) {
                fill(230);
            } else if (segment.getIgnored()) {
                fill(230, 230, 160);
            } else {
                fill(230, 200, 200);
            }
            // background for text
            rect(screenX, screenY, 290, 200);
            fill(0);
            stroke(255, 0, 0);
            // label for segment
            StringBuilder text = new StringBuilder();
            List<PixelPoint> pixels = segment.getPixels();
            if (!segment.getEnabled()) {
                text.append("DISABLED ");
            }
            if (segment.getIgnored()) {
                text.append("IGNORED ");
            }
            text.append(selectedSegment).append(": ").append(segment.getName()).append("\n\n")
                    .append(defaultString(segment.getDescription(), ""))
                    .append("\n\n").append("pixels: ").append(pixels.size());
            Set<Integer> strips = pixels.stream().map(PixelPoint::getStrip).collect(toSet());
            text.append(" strips: ").append(StringUtils.join(strips, ", ")).append("\n\n");
            text.append("Pixel ").append(highlight);

            text(text.toString(), screenX + margin, 50, 290, 200);
            popStyle();
        }
    }

    private void drawSegments() {
        List<Segment> pixelMap = config.getPixelMap();
        for (int i = 0; i < pixelMap.size(); i++) {
            Segment segment = pixelMap.get(i);
            if (segment.getEnabled()) {
                pushMatrix();
                pushStyle();
                if (!segment.getTransforms().isEmpty()) {
                    // restore the segment in its location by applying the transforms to the points
                    applyTransforms(segment);
                }

                // draw it
                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    stroke(127, 127, 0);
                    strokeWeight(0.5f);
                } else if (i == selectedSegment) {
                    stroke(255, 0, 0);
                    strokeWeight(2);
                } else {
                    stroke(255, 100, 0);
                    strokeWeight(1);
                }
                noFill();
                rect(segment.getBoundingBox());
                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    stroke(127, 127, 0);
                    strokeWeight(0.5f);
                } else if (i == selectedSegment) {
                    // style for points of selected segement
                    stroke(0);
                    strokeWeight(5);
                } else {
                    stroke(127);
                    strokeWeight(3);
                }
                drawPoints(segment.getPixels());
                popStyle();
                popMatrix();
            }
        }
    }

    private void applyTransforms(Segment segment) {
        List<Transform> transforms = segment.getTransforms();
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

    private void drawPoints(List<PixelPoint> pixels) {
        pushStyle();
        ellipseMode(CENTER);
        for (int i = 0; i < pixels.size(); i++) {
            PixelPoint pixel = pixels.get(i);
            if (i > 0) {
                pushStyle();
                PixelPoint prev = pixels.get(i - 1);
                line(prev.getX(), prev.getY(), pixel.getX(), pixel.getY());
                stroke(170, 0, 0);
                popStyle();
            }
            // now draw the actual point
            if (highlight == i) {
                pushStyle();
                fill(255, 0, 0, 127);
                stroke(255, 0, 0);
                ellipse(pixel.getX(), pixel.getY(), 12, 12);
                popStyle();
                ellipse(pixel.getX(), pixel.getY(), 6, 6);
            } else {
                noFill();
                ellipse(pixel.getX(), pixel.getY(), 6, 6);
            }
        }
        popStyle();
    }

    private void drawBackground() {
        if (bg == null) {
            BufferedImage backgroundImage = config.getBackgroundImage();
            bg = new PImage(backgroundImage);
        }
        image(bg, 0, 0);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        pushMatrix();
        resetMatrix();
        char k = event.getKey();
        // only Mac standard key binding for now
        if (event.isMetaDown()) {
            if (k == 's') {
                try {
                    config.getBackground().setTransforms(makeBackgroundTransforms());
                    config.save();
                } catch (IOException e) {
                    logger.error("could not save config", e);
                }
                logger.info("saved");
            }
            if (k == 'o') {
                try {
                    restoreConfig();
                } catch (IOException e) {
                    logger.error("could not load config", e);
                }
                logger.info("loaded");
            }

            // CMD + Arrows translate the whole background
            if (event.getKeyCode() == RIGHT) {
                bgX -= dt;
            }
            if (event.getKeyCode() == LEFT) {
                bgX += dt;
            }
            if (event.getKeyCode() == DOWN) {
                bgY -= dt;
            }
            if (event.getKeyCode() == UP) {
                bgY += dt;
            }

            // aka plus key
            if (k == '=') {
                // zoom in
                bgScaleX *= 1.01;
                bgScaleY *= 1.01;
            }
            if (k == '-') {
                // zoom out
                bgScaleX *= 0.99;
                bgScaleY *= 0.99;
            }
            if (k == '0') {
                // reset zoom
                bgScaleX = 1f;
                bgScaleY = 1f;
            }
        } else {
            if (!config.getPixelMap().isEmpty()) {

                // NO META KEY DOWN; segment operations

                if (k == ']') {
                    selectedSegment++;
                    selectedSegment %= config.getPixelMap().size();
                    highlight = 0;
                } else if (k == '[') {
                    if (selectedSegment <= 0) {
                        selectedSegment = config.getPixelMap().size() - 1;
                    } else {
                        selectedSegment--;
                    }
                    highlight = 0;
                }

                Segment segment = config.getPixelMap().get(selectedSegment);
                List<Transform> ts = segment.getTransforms();

                if (k == '=' || k == '-' || k == '0') {
                    // change scale of currently selected segment
                    Transform t = ts.stream().filter(tt -> tt.is(Type.SCALE)).findFirst()
                            .orElseGet(() -> segment.addTransform(Transform.scale(1)));
                    if (k == '=') {
                        t.set("x", (float) (t.get("x") * 1.03));
                        t.set("y", (float) (t.get("y") * 1.03));
                    } else if (k == '-') {
                        t.set("x", (float) (t.get("x") * 0.97));
                        t.set("y", (float) (t.get("y") * 0.97));
                    } else {//noinspection ConstantConditions
                        if (k == '0') {
                            t.set("x", 1f);
                            t.set("y", 1f);
                        }
                    }
                }
                int c = event.getKeyCode();

                if (k == CODED && isArrow(c)) {
                    Transform t = ts.stream().filter(tr -> tr.is(Type.TRANSLATE)).findFirst()
                            .orElseGet(() -> segment.addTransform(Transform.translate(0f, 0f)));

                    if (c == RIGHT) {
                        t.set("x", (t.get("x") + dt));
                    } else if (c == DOWN) {
                        t.set("y", (t.get("y") + dt));
                    } else if (c == LEFT) {
                        t.set("x", (t.get("x") - dt));
                    } else if (c == UP) {
                        t.set("y", (t.get("y") - dt));
                    }

                }
                // Rotate transform may not exist, in which case create identity rotation transform
                Transform rotate = ts.stream().filter(t -> t.is(Type.ROTATE)).findFirst()
                        .orElseGet(() -> segment.addTransform(Transform.rotate(0f)));
                if (k == '.') {
                    rotate.set("z", rotate.get("z") + theta * dt);
                }
                if (k == ',') {
                    rotate.set("z", rotate.get("z") - theta * dt);
                }
                if (k == '\\') {
                    segment.flipEnabled();
                }
                if (k == 'i') {
                    segment.flipIgnored();
                }

                // point operations

                // highlight
                if (k == '\'') {
                    highlight++;
                    highlight %= segment.getPixels().size();
                } else if (k == ';') {
                    highlight--;
                    if (highlight < 0) {
                        highlight = segment.getPixels().size() - 1;
                    }
                } else if (k == '|') {
                    // split the segment
                    Segment second = new Segment();
                    second.setName("split of " + segment.getName());
                    second.setDescription("split of " + segment.getDescription());
                    for (Transform t : ts) {
                        second.addTransform(new Transform(t));
                    }

                    // remove pixels from the segment and put them in the second
                    List<PixelPoint> pixels = segment.getPixels();
                    second.setPixels(pixels.subList(highlight, pixels.size()));
                    segment.setPixels(pixels.subList(0, highlight));

                    config.getPixelMap().add(second);
                } else if (k == '?') {
                    // delete pixel
                    segment.getPixels().remove(highlight);
                    highlight %= segment.getPixels().size();
                }

            }

            // movement step size choices
            if (k == '1') {
                dt = 1;
            }
            if (k == '2') {
                dt = 2;
            }
            if (k == '3') {
                dt = 4;
            }
            if (k == '4') {
                dt = 8;
            }
            if (k == '5') {
                dt = 16;
            }
            if (k == '6') {
                dt = 32;
            }
            if (k == '7') {
                dt = 64;
            }
            if (k == '8') {
                dt = 128;
            }
            if (k == '9') {
                dt = 256;
            }
        }
        popMatrix();
    }

    /**
     * Create a list of {@link Transform}s corresponding to the current orientation of the background.
     *
     * @return transforms suitable for serialisation.
     */
    private List<Transform> makeBackgroundTransforms() {
        Transform offset = Transform.translate(bgX, bgY);
        Transform scale = Transform.scale(bgScaleX, bgScaleY);
        return asList(offset, scale);
    }

    public static void main(String[] args) {
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(MapEditor.class, args);
    }
}
