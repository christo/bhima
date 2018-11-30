package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.geometry.Point;
import com.chromosundrift.bhima.geometry.Rect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * App for editing layout of segments, loading and saving from config file.
 */
public class MapEditor extends DragonMind {

    // TODO add right wing back
    // TODO LHS manual mirroring


    // KEYBOARD SHORTCUTS:

    private static final char ADD_POINT = '`';
    private static final char DELETE_POINT = '!';
    private static final char SPLIT_SEGMENT = '|';
    private static final char PREVIOUS_DT_PIXELS = ';';
    private static final char NEXT_DT_PIXELS = '\'';
    private static final char INCREASE_LINE_TRANSPARENCY = 'u';
    private static final char DECREASE_LINE_TRANSPARENCY = 'U';
    private static final char TOGGLE_SHOW_SEGMENT_SCAN_IMAGE = 'I';
    private static final char TOGGLE_IGNORE_SEGMENT = 'i';
    private static final char TOGGLE_DISABLE_SEGMENT = '\\';
    private static final char SHIFT_PIXEL_NUMBERING_DOWN = '(';
    private static final char SHIFT_PIXEL_NUMBERING_UP = ')';
    private static final char ROTATE_SEGMENT_CCW = ',';
    private static final char ROTATE_SEGMENT_CW = '.';
    private static final char RESET_SEGMENT_SCALE = '0';
    private static final char SEGMENT_SCALE_DOWN = '-';
    private static final char SEGMENT_SCALE_UP = '=';
    private static final char PREVIOUS_SEGMENT = '[';
    private static final char NEXT_SEGMENT = ']';
    private static final char TOGGLE_RAINBOW_MODE = 'r';
    private static final char META_SAVE = 's';
    private static final char META_LOAD = 'o';
    private static final char META_GLOBAL_SCALE_UP = '=';
    private static final char META_GLOBAL_SCALE_DOWN = '-';
    private static final char META_GLOBAL_SCALE_RESET = '0';
    private static final char SHIFT_RESET_VIEW = ' ';

    private static final boolean DRAW_GENERATED_DRAGON = true;
    public static final int FRAMES_PER_UI_REDRAW = 10;


    private static Logger logger = LoggerFactory.getLogger(MapEditor.class);
    private static final float MOUSE_ZOOM_SPEED = 0.001f;
    private static final int SELECT_DIST = 10;

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
    private int pixelIndex;

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

    private float viewShiftX = 0;
    private float viewShiftY = 0;
    private float viewZoom = 1;
    private PixelPoint draggingPixelPoint = null;
    private Config generatedDragon;
    private PGraphics segmentInfo;
    private PGraphics segmentSummary;
    private PGraphics viewInfo;

    public void settings() {
        //        fullScreen(P2D);
        size(1920, 1080, P2D);
        pixelDensity(2);
        smooth();
        try {
            config = loadConfigFromFirstArgOrDefault();
        } catch (IOException e) {
            logger.error("Could not load config", e);
        }
        DragonBuilder dragonBuilder = new DragonBuilder(1730, 800, 4);
        int ph = 80 - dragonBuilder.margin;
        int pw = 80 - dragonBuilder.margin;
        dragonBuilder.addSegment(3, pw, ph)
                .addSegment(3, pw, ph)
                .addSegment(3, pw, ph)
                .addSegment(1, pw, ph)
                //.addPanel()
                .build(); // TODO finish the 3 panel segment with a panelpoint function
        generatedDragon = dragonBuilder.build();
    }

    public void setup() {
        super.setup();
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
            // draw white screen background
            fill(255);
            rect(0, 0, width, height);
            pushMatrix();
            applyZoom();

            // draw model viewport
            fill(190);
            // draw line from centre of screen to centre of model viewport to help locate scrolled-off shiz
            drawLineFromViewToModel();
            rect(0, 0, width, height);
            pushMatrix();
            applyGlobalTransforms();
            drawBackground();
            drawGlobalBoundingBox();
            drawSegments(config);

        } catch (RuntimeException e) {
            // TODO fix hack; split causes modification under iteration
            logger.error("got runtime exception while drawing: " + e.getMessage(), e);
            pixelIndex = 0;

        } finally {

            popMatrix();
            if (DRAW_GENERATED_DRAGON) {
                drawGeneratedDragon();
            }
            popMatrix();
            drawUi();
        }
    }

    private void drawGeneratedDragon() {
        drawSegments(generatedDragon);
    }

    private void drawLineFromViewToModel() {
        pushStyle();
        strokeWeight(50);
        stroke(0, 90);
        float cx = width / 2.0f;
        float cy = height / 2.0f;
        float screenX = screenX(cx, cy);
        float screenY = screenY(cx, cy);
        pushMatrix();
        resetMatrix();
        line(cx, cy, screenX, screenY);
        popMatrix();
        popStyle();
    }

    private void drawUi() {
        // don't redraw UI every frame
        if (frameCount % FRAMES_PER_UI_REDRAW == 0 || segmentInfo == null || segmentSummary == null || viewInfo == null) {
            segmentInfo = drawSegmentInfo();
            segmentSummary = drawSegmentSummary();
            viewInfo = drawViewInfo();
        }
        image(segmentInfo, 0, 0);
        image(segmentSummary, 0, 0);
        image(viewInfo, 0, 0);
    }

    private PGraphics drawSegmentSummary() {
        PGraphics graphics = createGraphics(width, height);
        graphics.beginDraw();
        // Draw summary of clashing segments
        Map<ImmutablePair<String, String>, Set<Integer>> clashes = config.checkForSegmentNumberClashes(s -> !s.getIgnored());
        if (!clashes.isEmpty()) {
            graphics.fill(0);
            StringBuilder clashMessage = new StringBuilder("Strip Number Clashes:\n");
            for (ImmutablePair<String, String> pair : clashes.keySet()) {
                clashMessage.append(pair).append(": ").append(clashes.get(pair)).append("\n");
            }
            graphics.text(clashMessage.toString(), 20, 20);
        }

        graphics.endDraw();
        return graphics;
    }

    private PGraphics drawViewInfo() {
        PGraphics graphics = createGraphics(width, height);
        graphics.beginDraw();
        graphics.pushStyle();
        graphics.fill(255, 100);
        graphics.noStroke();
        float textHeight = 18;
        float padding = textHeight * 0.5f;
        graphics.rect(0, height - padding - textHeight, width, padding + textHeight);
        graphics.fill(0, 0, 0, 128);
        graphics.textSize(textHeight);
        String info = format("zoom: %.2f%% x: %.2f y: %.2f   (zoom: mouse wheel, move: SHIFT + drag, reset: SHIFT + SPACE)",
                viewZoom * 100, viewShiftX, viewShiftY);
        graphics.textFont(getDefaultFont());
        graphics.text(info, padding, height - padding - textHeight / 2);
        graphics.popStyle();
        graphics.endDraw();
        return graphics;
    }

    /**
     * View-only transform that affects video, background, everything but which is never saved to the model.
     */
    private void applyZoom() {
        translate(viewShiftX, viewShiftY);
        scale(viewZoom);
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

    /**
     * Draws the GUI box with text in that describes the current segment.
     */
    private PGraphics drawSegmentInfo() {
        PGraphics g = createGraphics(width, height);
        g.beginDraw();
        if (!config.getPixelMap().isEmpty()) {
            Segment segment = config.getPixelMap().get(selectedSegment);
            int margin = 20;
            g.pushStyle();
            float w = 320;
            float h = 300;
            float screenX = width - w;
            float screenY = 50;

            g.noStroke();
            if (segment.getEnabled()) {
                g.fill(230, 200);
            } else if (segment.getIgnored()) {
                g.fill(240, 240, 140, 200);
            } else {
                g.fill(230, 200, 200, 200);
            }
            // background for text
            g.rect(screenX, screenY, w, h);
            g.fill(0);
            g.stroke(255, 0, 0);
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
            text.append(" strip #s: ").append(StringUtils.join(strips, ", ")).append("\n\n");

            PixelPoint thisPixel = pixels.get(pixelIndex);
            text.append("Strip: ").append(thisPixel.getStrip())
                    .append(" Pixel number: ").append(thisPixel.getPixel() - segment.getPixelIndexBase())
                    .append(" x,y: ").append(thisPixel.getPoint().toString()).append("\n");
            text.append("pixelIndex: ").append(pixelIndex).append("\n");
            text.append("pixel index base: ").append(segment.getPixelIndexBase()).append("\n\n");
            text.append("step size: ").append(dt).append("\n");
            text.append("strip num override: ").append(segment.getStripNumberOverride()).append("\n\n");
            text.append("seg xform: ").append(segment.getTransforms().toString());
            g.textFont(getDefaultFont());
            g.textSize(12);
            g.text(text.toString(), screenX + margin, screenY + margin, w - margin, 200 - margin);
            g.popStyle();
        }
        g.endDraw();
        return g;
    }

    /**
     * Draw only enabled segments, ignored segments are more transparent.
     *
     * @param config
     */
    private void drawSegments(Config config) {
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
                }

                // draw it
                Rect r = calculateScreenBox(segment);

                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    stroke(127, 127, 0);
                    strokeWeight(0.25f);
                } else if (i == selectedSegment) {
                    if (r.contains(mouseX, mouseY)) {
                        fill(255, 0, 0, 80);
                    }

                    stroke(255, 0, 0);
                    strokeWeight(2);
                    if (showImage) {
                        drawSegmentImage(segment);
                    }
                } else {
                    stroke(255, 100, 0);
                    strokeWeight(0.5f);
                }
                pushMatrix();
                resetMatrix();
                rect(r);
                popMatrix();
                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    stroke(127, 127, 0);
                    strokeWeight(0.25f);
                } else if (i == selectedSegment) {
                    // style for points of selected segement
                    stroke(0);
                    strokeWeight(2);
                } else {
                    stroke(127);
                    strokeWeight(1);
                }

                // only highlight the pixel if we are drawing at the selected segment
                int highlightedPixel = i == selectedSegment ? pixelIndex : -1;

                int wire = color(120, 70, 120, lineAlpha);
                int brightHighlight = color(255, 0, 0, lineAlpha);
                int fg = color(0, 255);
                drawPoints(segment.getPixels(), lineAlpha, rainbow, colours, highlightedPixel, brightHighlight, wire, fg, true, segment.getPixelIndexBase());
                popStyle();
                popMatrix();
            }
        }
    }

    private Rect calculateScreenBox(Segment segment) {
        Stream<PixelPoint> pixels = segment.getPixels().stream();
        Stream<Point> screenPoints = pixels.map((PixelPoint pp) -> modelToScreen(pp.getPoint()));
        return segment.getBoundingBox(screenPoints);
    }

    private void drawSegmentImage(Segment segment) {
        Optional<String> scanId = segment.getMappingId();
        scanId.ifPresent(sId -> {
            PixelPoint p = segment.getPixels().get(pixelIndex);
            // the file uses the segment's nominal pixel index (not pixelIndexBase)
            String file = Segment.mappedImageFile(sId, "lightframe", p.getStrip(), p.getPixel());
            try {
                PImage image = loader.loadPimage(file);
                image(image, 0, 0); // should be in transform matrix here
            } catch (IOException e) {
                logger.error("exception loading " + file, e);
                logger.warn("turning off image loading");
                showImage = false;
            }
        });
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

    private boolean shiftInRange(float newShiftX, float newShiftY) {
        float pixelX = newShiftX / viewZoom;
        float pixelY = newShiftY / viewZoom;
        // leave a little margin
        int w = (int) (width * 0.9);
        int h = (int) (height * 0.9);
        boolean xInRange = pixelX > -w && pixelX < w;
        boolean yInRange = pixelY > -h && pixelY < h;
        if (!xInRange) {
            logger.info("x not in range: " + pixelX);
        }
        if (!yInRange) {
            logger.info("y not in range: " + pixelY);
        }
        return xInRange && yInRange;
    }

    private void resetView() {
        viewShiftX = 0;
        viewShiftY = 0;
        viewZoom = 1;
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        if (event.isShiftDown()) {
            // change view zoom
            float zoomStep = (float) event.getCount() * MOUSE_ZOOM_SPEED;
            float newShiftX = viewShiftX - mouseX * zoomStep;
            float newShiftY = viewShiftY - mouseY * zoomStep;
            float newZoom = viewZoom + zoomStep;
            float scaledModelSize = width * viewZoom;
            // attempt to decide if the zoom level is too extreme relative to screen size
            boolean zoomInRange = scaledModelSize > 5 && scaledModelSize < (width * 100);
            // TODO fix range checking for zoom and shift
            if (scaledModelSize > 10 || zoomInRange && shiftInRange(newShiftX, newShiftY)) {
                viewShiftX = newShiftX;
                viewShiftY = newShiftY;
                viewZoom = newZoom;
            } else {
                viewZoom += 0.001; // TODO fix this hack, sometimes gets stuck at minimum zoom edge
                String explain = (!zoomInRange ? "zoom" : "") + (!shiftInRange(newShiftX, newShiftY) ? "shift" : "");
                String more = format("zoomstep %.2f newzoom %.2f scaledModelSize %.2f", zoomStep, newZoom, scaledModelSize);
                logger.info("not zooming because " + explain + " out of range " + more);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (event.isShiftDown()) {
            float newViewShiftX = viewShiftX + mouseX - pmouseX;
            float newViewShiftY = viewShiftY + mouseY - pmouseY;
            // TODO fix range checking for zoom and shift
            //            boolean shiftInRange = shiftInRange(newViewShiftX, newViewShiftY);
            boolean shiftInRange = true;
            if (shiftInRange) {
                viewShiftX = newViewShiftX;
                viewShiftY = newViewShiftY;
            }
        } else if (event.isControlDown()) {
            // maybe dragging a pixel
            if (draggingPixelPoint != null) {
                logger.debug("continuing to drag a pixelPoint " + draggingPixelPoint);
                // note we assume the draggingPixelPoint is in the selected segment (upheld elsewhere)
                withAllTransforms(getSelectedSegment(), s -> {
                    // FIXME this is still not tracking the mouse correctly
                    // (word on the street is that getMatrix() only grabs the top of the matrix stack)

                    // invert the matrix so we can apply it to the mouse position and get target in-model points
                    PMatrix m = getMatrix().get();
                    if (!m.invert()) {
                        // allegedly matrix transforms can fail to invert due to being "non-injective"
                        // see https://en.wikipedia.org/wiki/Bijection,_injection_and_surjection
                        logger.warn("couldn't invert the matrix");
                    }
                    setMatrix(m);
                    int modelX = (int) screenX(mouseX, mouseY);
                    int modelY = (int) screenY(mouseX, mouseY);
                    draggingPixelPoint.getPoint().moveTo(modelX, modelY);
                    resetMatrix();
                });

            } else {
                // we have started shiffting a point
                logger.info("started dragging a point");
                handlePointAt(mouseX, mouseY, pp -> draggingPixelPoint = pp);
            }
        }
    }

    /**
     * Performs the given work on the given segment after view, global and segment transforms have all been done. This
     * means the points in the segment are renderable from a reset matrix starting point or their model and screen
     * space coordinates can be meaningfully interacted with.
     */
    private void withAllTransforms(Segment segment, Consumer<Segment> go) {
        applyZoom();
        applyGlobalTransforms();
        applyTransforms(segment.getTransforms()); // assuming draggingPixelPoint is in selected segment
        go.accept(segment);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        logger.info("not dragging a pixelPoint");
        draggingPixelPoint = null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        // don't change the selected point if the control key is down, we might be moving a point
        if (!event.isControlDown()) {
            handlePointAt(event.getX(), event.getY(), pp -> pixelIndex = getIndexFor(getSelectedSegment(), pp));
        }
    }

    private void handlePointAt(int sx, int sy, Consumer<PixelPoint> pointConsumer) {

        withAllTransforms(getSelectedSegment(), s -> {
            // now only calculate pointer distances for current segment points if the pointer is in the bounding box
            Rect rect = calculateScreenBox(s);
            if (rect.contains(sx, sy)) {
                final Point sPoint = new Point(sx, sy);
                final Comparator<PixelPoint> distanceToMouse = (o1, o2) ->
                        Float.compare(
                                dist(modelToScreen(o1.getPoint()), sPoint),
                                dist(modelToScreen(o2.getPoint()), sPoint));

                s.getPixels().stream()
                        .min(distanceToMouse)
                        .filter(pp -> dist(modelToScreen(pp.getPoint()), sPoint) < SELECT_DIST)
                        .ifPresent(pointConsumer);
            }
        });
    }

    /**
     * Finds the pixelIndex for the given segment-pixelpoint pair
     *
     * @param segment
     * @param pp
     */
    private int getIndexFor(Segment segment, PixelPoint pp) {
        int index = 0;
        for (PixelPoint pixel : segment.getPixels()) {
            if (pixel.equals(pp)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private Segment getSelectedSegment() {
        return config.getPixelMap().get(selectedSegment);
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
                handleSegmentKey(event);
            }

            if (k == TOGGLE_RAINBOW_MODE) {
                rainbow = !rainbow;
            }
            if (k == SHIFT_RESET_VIEW && event.isShiftDown()) {
                resetView();
            }

            // movement step size choices
            handleStepSizeKey(k);
        }
        popMatrix();
    }

    private void handleStepSizeKey(char k) {
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

    private void handleSegmentKey(KeyEvent event) {
        // NO META KEY DOWN; segment operations
        char k = event.getKey();
        if (k == NEXT_SEGMENT) {
            selectedSegment++;
            selectedSegment %= config.getPixelMap().size();
            pixelIndex = 0;
        } else if (k == PREVIOUS_SEGMENT) {
            if (selectedSegment <= 0) {
                selectedSegment = config.getPixelMap().size() - 1;
            } else {
                selectedSegment--;
            }
            pixelIndex = 0;
        }

        Segment segment = getSelectedSegment();


        // handle segment transform manipulations
        if (k == SEGMENT_SCALE_UP) {
            segment.scale(1.03);
        } else if (k == SEGMENT_SCALE_DOWN) {
            segment.scale(0.97);
        } else if (k == RESET_SEGMENT_SCALE) {
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
        if (k == ROTATE_SEGMENT_CW) {
            segment.rotate(theta * dt);
        }
        if (k == ROTATE_SEGMENT_CCW) {
            segment.rotate(-theta * dt);
        }
        if (k == SHIFT_PIXEL_NUMBERING_UP) {
            segment.setPixelIndexBase(segment.getPixelIndexBase() + 1);
        }
        if (k == SHIFT_PIXEL_NUMBERING_DOWN) {
            segment.setPixelIndexBase(segment.getPixelIndexBase() - 1);
        }

        if (k == TOGGLE_DISABLE_SEGMENT) {
            segment.flipEnabled();
        }
        if (k == TOGGLE_IGNORE_SEGMENT) {
            segment.flipIgnored();
        }

        if (k == TOGGLE_SHOW_SEGMENT_SCAN_IMAGE) {
            showImage = !showImage;
        }
        if (k == DECREASE_LINE_TRANSPARENCY) {
            // make lines more opaque
            lineAlpha = min(255, lineAlpha + 16);
        }
        if (k == INCREASE_LINE_TRANSPARENCY) {
            // make lines more transparent
            lineAlpha = max(0, lineAlpha - 16);
        }

        // point operations

        // highlight
        if (k == NEXT_DT_PIXELS) {
            // next (dt) pixel(s)
            pixelIndex += dt;
            pixelIndex %= segment.getPixels().size();
        } else if (k == PREVIOUS_DT_PIXELS) {
            // previous (dt) pixel(s)
            pixelIndex -= dt;
            if (pixelIndex < 0) {
                // wrap around at bottom
                pixelIndex = segment.getPixels().size() + pixelIndex;
            }
        } else if (k == SPLIT_SEGMENT) {
            // split the segment into two at the current pixel
            Segment split = new Segment();
            split.setName("split of " + segment.getName());
            split.setDescription("split of " + segment.getDescription());
            segment.getTransforms().forEach(split::addTransform);

            // remove pixels from the segment and put them in the second
            List<PixelPoint> pixels = segment.getPixels();
            split.setPixels(pixels.subList(pixelIndex, pixels.size()));
            segment.setPixels(pixels.subList(0, pixelIndex));

            config.getPixelMap().add(split);
        } else if (k == DELETE_POINT) {
            // delete pixel
            PixelPoint deleted = segment.getPixels().remove(pixelIndex);
            turnOffLed(deleted, segment.getPixelIndexBase());
            pixelIndex %= segment.getPixels().size();
        } else if (k == ADD_POINT) {
            List<PixelPoint> pixels = segment.getPixels();
            int i = pixelIndex;
            PixelPoint thisPixel = pixels.get(i);
            // is there a next pixel?
            if (i + 1 < pixels.size()) {
                PixelPoint nextPixel = pixels.get(i + 1);
                int nextPixelNumber = thisPixel.getPixel() + 1;
                // TODO this assumes pixels are in order! enforce this invariant in Segment
                boolean nextPixelLeavesNumberSpace = nextPixel.getPixel() != nextPixelNumber;
                boolean nextPixelDifferentStrip = nextPixel.getStrip() != thisPixel.getStrip();
                if (nextPixelLeavesNumberSpace || nextPixelDifferentStrip) {
                    // put the new pixel at the midpoint between this pixel and next pixel
                    int newX = thisPixel.getX() + (thisPixel.getX() - nextPixel.getX()) / 2;
                    int newY = thisPixel.getY() + (thisPixel.getY() - nextPixel.getY()) / 2;
                    PixelPoint newPixel = new PixelPoint(thisPixel.getStrip(), nextPixelNumber, newX, newY);
                    pixels.add(i + 1, newPixel);
                } else {
                    logger.warn("can't create pixel at this point because no spare pixel number");
                }
            }
            // search forward for an unused pixel number
        }
    }

    /**
     * Handle key event where the meta key (on mac, command key) is held down, namely global commands as opposed to
     * segment-specific commands.
     *
     * @param event the {@link  KeyEvent}.
     */
    private void handleMetaKey(KeyEvent event) {
        char k = event.getKey();
        if (k == META_SAVE) {
            try {
                saveConfigToFirstArgOrDefault(config);
            } catch (IOException e) {
                logger.error("could not save config", e);
            }
            logger.info("config saved");
        }
        if (k == META_LOAD) {
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
        if (k == META_GLOBAL_SCALE_UP) {
            config.multiplyGlobalScale(1.01);
        }
        if (k == META_GLOBAL_SCALE_DOWN) {
            config.multiplyGlobalScale(0.99);
        }
        if (k == META_GLOBAL_SCALE_RESET) {
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
