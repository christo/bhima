package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.geometry.PixelPoint;
import com.chromosundrift.bhima.geometry.Point;
import com.chromosundrift.bhima.geometry.Rect;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * App for editing layout of segments, loading and saving from config file.
 */
public class MapEditor extends DragonMind {

    private static Logger logger = LoggerFactory.getLogger(MapEditor.class);

    private Config config;
    private final CachingImageLoader loader = new CachingImageLoader(300);
    private PImage bg;

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

    /**
     * Whether to show strips in different colours.
     */
    private boolean rainbow;

    private List<NamedColour> colours = new ArrayList<>();

    /**
     * Whether or not to show scan images if they are available in the usual place on disk.
     */
    private boolean showImage = false;

    /**
     * Global alpha value for lines.
     */
    private int lineAlpha = 255;

    public void settings() {
        //fullScreen(P2D);
        size(1920, 1080, P2D);
        pixelDensity(2);
        smooth();
        try {
            config = loadConfigFromFirstArgOrDefault();
        } catch (IOException e) {
            logger.error("Could not load config", e);
        }
    }

    public void setup() {
        background(220);
        registerColour("Violet", 148, 0, 211);
        registerColour("Indigo", 75, 0, 130);
        registerColour("Blue", 0, 0, 255);
        registerColour("Green", 0, 255, 0);
        registerColour("Yellow", 255, 255, 0);
        registerColour("Orange", 255, 127, 0);
        registerColour("Red", 255, 0, 0);
    }

    private void registerColour(String name, int red, int green, int blue) {
        colours.add(new NamedColour(name, red, green, blue));
    }

    @Override
    public void draw() {
        try {
            fill(190);
            rect(0, 0, width, height);
            pushMatrix();
            applyGlobalTransforms();
            drawBackground();
            drawGlobalBoundingBox();
            drawSegments();

        } catch (RuntimeException e) {
            // TODO fix hack; split causes modification under iteration
            logger.error("got runtime exception while drawing: " + e.getMessage(), e);
            highlight = 0;

        } finally {

            popMatrix();
            drawSegmentInfo();
        }
    }

    private void applyGlobalTransforms() {
        applyTransforms(config.getBackground().getTransforms());
    }

    /**
     * Draws a box around the entire configured map.
     */
    private void drawGlobalBoundingBox() {
        pushStyle();
        fill(255, 255, 0, 20);
        stroke(200, 255, 0);
        strokeWeight(4);
        List<Segment> enabledSegments = config.enabledSegments().collect(Collectors.toList());
        Rect rect = screenspaceBoundingRect(enabledSegments);
        pushMatrix();
        resetMatrix();
        rect(rect);
        popMatrix();
        popStyle();
    }


    private void drawSegmentInfo() {
        if (!config.getPixelMap().isEmpty()) {
            Segment segment = config.getPixelMap().get(selectedSegment);
            int margin = 20;
            pushStyle();
            // draw a callout box with the info in the top right, TODO connect to the segment with a line
            float w = 320;
            float h = 300;
            float screenX = width - w;
            float screenY = 50;

            // TODO reproduce background + segment transform arithmetically to get screen coord of bounding box

            noStroke();
            if (segment.getEnabled()) {
                fill(230);
            } else if (segment.getIgnored()) {
                fill(240, 240, 140);
            } else {
                fill(230, 200, 200);
            }
            // background for text
            rect(screenX, screenY, w, h);
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

            PixelPoint thisPixel = pixels.get(highlight);
            text.append("Strip: ").append(thisPixel.getStrip())
                    .append(" Pixel: ").append(thisPixel.getPixel())
                    .append(" x,y: ").append(thisPixel.getPoint().toString()).append("\n");
            text.append("P# ").append(highlight).append("\n\n");
            text.append("step size: ").append(dt).append("\n");
            text.append("strip num override: ").append(segment.getStripNumberOverride()).append("\n\n");
            text.append("seg xform: ").append(segment.getTransforms().toString());

            text(text.toString(), screenX + margin, screenY + margin, w - margin, 200 - margin);
            popStyle();
        }
    }

    /**
     * Draw only enabled segments, ignored segments are more transparent.
     */
    private void drawSegments() {
        List<Segment> pixelMap = config.getPixelMap();
        for (int i = 0; i < pixelMap.size(); i++) {
            Segment segment = pixelMap.get(i);
            if (segment.getEnabled()) {
                pushMatrix();
                pushStyle();
                noFill();
                if (!segment.getTransforms().isEmpty()) {
                    // restore the segment in its location by applying the transforms to the points
                    List<Transform> transforms = segment.getTransforms();
                    applyTransforms(transforms);
                } else {
                    logger.warn("Transform empty for segment " + i + ": " + segment.getName());
                }

                // draw it
                Stream<PixelPoint> pixels = segment.getPixels().stream();
                Stream<Point> points = pixels.map((PixelPoint pp) -> modelToScreen(pp.getPoint()));
                Rect r = segment.getBoundingBox(points);

                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    stroke(127, 127, 0);
                    strokeWeight(0.5f);
                } else if (i == selectedSegment) {
                    if (r.contains(mouseX, mouseY)) {
                        fill(0, 255, 255, 110);
                        logger.debug("seg " + i + " mouse " + mouseX + ", " + mouseY);
                    }

                    stroke(255, 0, 0);
                    strokeWeight(5);
                    if (showImage) {
                        drawSegmentImage(segment);
                    }
                } else {
                    stroke(255, 100, 0);
                    strokeWeight(1);
                }
                pushMatrix();
                resetMatrix();
                rect(r);
                String name = segment.getName(); // TODO draw segment name just outside bounding box
                popMatrix();
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

                drawPoints(segment.getPixels(), lineAlpha, rainbow, colours, highlight, color(255, 0, 0, lineAlpha), color(120, 70, 120, lineAlpha), color(0, 255));
                popStyle();
                popMatrix();
            }
        }
    }

    private void drawSegmentImage(Segment segment) {
        // TODO add proper metadata for scan id
        Pattern scanIdRx = Pattern.compile("mapping file is mappings/Mapping(\\d+)\\.csv");
        Matcher m = scanIdRx.matcher(segment.getDescription());
        if (m.find()) {
            String scanId = m.group(1);
            // image files look like this:
            // Mapping-1537359798903-lightframe-00-0000.png
            PixelPoint p = segment.getPixels().get(highlight);
            String file = imageFile(scanId, "lightframe", p.getStrip(), p.getPixel());
            try {
                PImage image = loader.loadPimage(file);
                image(image, 0, 0); // should be in transform matrix here
            } catch (IOException e) {
                logger.error("exception loading " + file, e);
                logger.warn("turning off image loading");
                showImage = false;
            }
        }
    }

    private void drawBackground() {
        if (config.getBackgroundImage() != null) {
            if (bg == null) {
                BufferedImage backgroundImage = config.getBackgroundImage();
                bg = new PImage(backgroundImage);
            }
            image(bg, 0, 0);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        pushMatrix();
        resetMatrix();
        char k = event.getKey();
        // only Mac standard key binding for now
        if (event.isMetaDown()) {
            handleMetaKey(event);
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



                // handle segment transform manipulations
                if (k == '=') {
                    segment.scale(1.03);
                } else if (k == '-') {
                    segment.scale(0.97);
                } else if (k == '0') {
                    segment.resetScale();
                }

                int c = event.getKeyCode();

                // handle arrows as segment translations
                if (k == CODED && isArrow(c)) {
                    if (c == RIGHT) {
                        segment.translateX(dt);
                    } else if (c == DOWN) {
                        segment.translateY(dt);
                    } else if (c == LEFT) {
                        segment.translateX(-dt);
                    } else if (c == UP) {
                        segment.translateY(-dt);
                    }
                }

                // segment rotation
                if (k == '.') {
                    segment.rotate(theta * dt);
                }
                if (k == ',') {
                    segment.rotate(-theta * dt);
                }

                if (k == '\\') {
                    segment.flipEnabled();
                }
                if (k == 'i') {
                    segment.flipIgnored();
                }

                if (k == 'I') {
                    showImage = !showImage;
                }
                if (k == 'U') {
                    // make lines more opaque
                    lineAlpha = min(255, lineAlpha + 16);
                }
                if (k == 'u') {
                    // make lines more transparent
                    lineAlpha = max(0, lineAlpha - 16);
                }

                // point operations

                // highlight
                if (k == '\'') {
                    // next (dt) pixel(s)
                    highlight += dt;
                    highlight %= segment.getPixels().size();
                } else if (k == ';') {
                    // previous (dt) pixel(s)
                    highlight -= dt;
                    if (highlight < 0) {
                        // wrap around at bottom
                        highlight = segment.getPixels().size() + highlight;
                    }
                } else if (k == '|') {
                    // split the segment into two at the current pixel
                    Segment split = new Segment();
                    split.setName("split of " + segment.getName());
                    split.setDescription("split of " + segment.getDescription());
                    segment.getTransforms().forEach(split::addTransform);

                    // remove pixels from the segment and put them in the second
                    List<PixelPoint> pixels = segment.getPixels();
                    split.setPixels(pixels.subList(highlight, pixels.size()));
                    segment.setPixels(pixels.subList(0, highlight));

                    config.getPixelMap().add(split);
                } else if (k == '?') {
                    // delete pixel
                    segment.getPixels().remove(highlight);
                    highlight %= segment.getPixels().size();
                }

            }
            if (k == 'r') {
                rainbow = !rainbow;
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
     * Handle key event where the meta key (on mac, command key) is held down, namely global commands as opposed to
     * segment-specific commands.
     *
     * @param event the {@link  KeyEvent}.
     */
    private void handleMetaKey(KeyEvent event) {
        char k = event.getKey();
        if (k == 's') {
            try {
                saveConfigToFirstArgOrDefault(config);
            } catch (IOException e) {
                logger.error("could not save config", e);
            }
            logger.info("config saved");
        }
        if (k == 'o') {
            try {
                config = loadConfigFromFirstArgOrDefault();
            } catch (IOException e) {
                logger.error("could not load config", e);
            }
        }

        // CMD + Arrows translate the whole background
        if (event.getKeyCode() == RIGHT) {
            config.addGlobalTranslateX(-dt);
        }
        if (event.getKeyCode() == LEFT) {
            config.addGlobalTranslateX(dt);
        }
        if (event.getKeyCode() == DOWN) {
            config.addGlobalTranslateY(-dt);
        }
        if (event.getKeyCode() == UP) {
            config.addGlobalTranslateY(dt);
        }

        // aka plus key
        if (k == '=') {
            // zoom in
            config.multiplyGlobalScale(1.01);
        }
        if (k == '-') {
            // zoom out
            config.multiplyGlobalScale(0.99);
        }
        if (k == '0') {
            // reset zoom to identity transform
            config.setGlobalScale(Type.SCALE.id);
        }
        // TODO fit to screen by scale and translate; use modelX etc.
    }

    public static void main(String[] args) {
        System.setProperty("gstreamer.library.path", "/Users/christo/src/christo/processing/libraries/video/library/macosx64");
        System.setProperty("gstreamer.plugin.path", "/Users/christo/src/christo/processing/libraries/video/library//macosx64/plugins/");
        PApplet.main(MapEditor.class, args);
    }
}
