package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.dragonmind.model.Transform;
import com.chromosundrift.bhima.dragonmind.model.Wiring;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type;
import static com.chromosundrift.bhima.geometry.Knapp.ZAG_ZIG;
import static com.chromosundrift.bhima.geometry.Rect.NULL_RECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * App for editing layout of segments, loading and saving from config file.
 */
public class MapEditor extends DragonMind {

    private static Logger logger = LoggerFactory.getLogger(MapEditor.class);

    // TODO UI that doesn't suck by putting this into a JPanel (?): processing.opengl.PSurfaceJOGL

    // KEYBOARD SHORTCUTS:

    private static final char HELP = '?';
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
    private static final char TOGGLE_DRAW_GENERATED_DRAGON = 'g';
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
    private static final char TOGGLE_SEGMENT_LABELS = 'd';
    private static final char META_SAVE = 's';
    private static final char META_LOAD = 'o';
    private static final char META_GLOBAL_SCALE_UP = '=';
    private static final char META_GLOBAL_SCALE_DOWN = '-';
    private static final char META_GLOBAL_SCALE_RESET = '0';
    private static final char SHIFT_RESET_VIEW = ' ';

    private static final Map<String, Character> keys = new TreeMap<>();
    private static final String NUMBERS = "123456789";
    private static final int[] POWERS = new int[NUMBERS.length()];

    static {
        for (int i = 0; i < POWERS.length; i++) {
            POWERS[i] = (int) Math.pow(2, i);
            logger.info("2 to the {} is {}", i, POWERS[i]);
        }
        keys.put("help", HELP);
        keys.put("add point", ADD_POINT);
        keys.put("delete point", DELETE_POINT);
        keys.put("split segment", SPLIT_SEGMENT);
        keys.put("previous dt pixels", PREVIOUS_DT_PIXELS);
        keys.put("next dt pixels", NEXT_DT_PIXELS);
        keys.put("increase line transparency", INCREASE_LINE_TRANSPARENCY);
        keys.put("decrease line transparency", DECREASE_LINE_TRANSPARENCY);
        keys.put("toggle show segment scan image", TOGGLE_SHOW_SEGMENT_SCAN_IMAGE);
        keys.put("toggle ignore segment", TOGGLE_IGNORE_SEGMENT);
        keys.put("toggle disable segment", TOGGLE_DISABLE_SEGMENT);
        keys.put("toggle draw generated dragon", TOGGLE_DRAW_GENERATED_DRAGON);
        keys.put("shift pixel numbering down", SHIFT_PIXEL_NUMBERING_DOWN);
        keys.put("shift pixel numbering up", SHIFT_PIXEL_NUMBERING_UP);
        keys.put("rotate segment ccw", ROTATE_SEGMENT_CCW);
        keys.put("rotate segment cw", ROTATE_SEGMENT_CW);
        keys.put("reset segment scale", RESET_SEGMENT_SCALE);
        keys.put("segment scale down", SEGMENT_SCALE_DOWN);
        keys.put("segment scale up", SEGMENT_SCALE_UP);
        keys.put("previous segment", PREVIOUS_SEGMENT);
        keys.put("next segment", NEXT_SEGMENT);
        keys.put("toggle rainbow mode", TOGGLE_RAINBOW_MODE);
        keys.put("toggle segment labels", TOGGLE_SEGMENT_LABELS);
        keys.put("meta save", META_SAVE);
        keys.put("meta load", META_LOAD);
        keys.put("meta global scale up", META_GLOBAL_SCALE_UP);
        keys.put("meta global scale down", META_GLOBAL_SCALE_DOWN);
        keys.put("meta global scale reset", META_GLOBAL_SCALE_RESET);
        keys.put("shift reset view", SHIFT_RESET_VIEW);
    }

    private static final int FRAMES_PER_UI_REDRAW = 10;

    private static final float MOUSE_ZOOM_SPEED = 0.001f;

    /**
     * Pixel distance to snap-to selection
     */
    private static final int SELECT_DIST = 10;

    /**
     * Configuration file to load/save
     */
    private Config config;
    private Wiring wiring;

    /** Whether or not to draw the generated grid/dragon */
    private boolean drawGeneratedDragon = false;

    /** Holds a cache of the per-pixel background images to reduce IO */
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
    private final float THETA = PI / 200;

    /**
     * The LED to highlight in the editor.
     */
    private int pixelIndex;

    /**
     * Whether to show strips in different colours.
     */
    private boolean rainbow;

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
    private boolean showSegmentLabels = true;
    private RainbowPalette rainbowPalette;

    public void settings() {
        fullScreen(JAVA2D); // P2D doesn't support setMatrix() which we need for point dragging
        // size(1920, 1080, P2D);
        pixelDensity(2);
        smooth();
        try {
            config = loadConfigFromFirstArgOrDefault();
        } catch (IOException e) {
            logger.error("Could not load config", e);
        }
        initDragonBuilder();
    }

    private void initDragonBuilder() {
        int margin = 2;
        DragonBuilder dragonBuilder = new DragonBuilder(1430, 560, margin);
        int ph = 85 - margin;
        int pw = 70 - margin;
        Function<DragonBuilder.PanelPoint, Boolean> exceptions = pp -> {
            if (pp.panelNumber == 10) {
                if (pp.x == 9 && pp.y == 9) {
                    return false;
                }
            } else if (pp.panelNumber == 11) {
                // TODO add exceptions for panel 11
                return true;
            } else if (pp.panelNumber == 12) {
                return pp.y <= 8 || pp.y == 9 && pp.x == 8;
            } else if (pp.panelNumber == 13) {
                return pp.y <= 7;
            } else if (pp.panelNumber == 14) {
                return pp.y <= 6;
            }
            return true;
        };
        dragonBuilder
                .addSegment(3, pw, ph)
                .addSegment(3, pw, ph)
                .addSegment(3, pw, ph, ZAG_ZIG)
                .addSegment(3, exceptions, pw, ph, ZAG_ZIG)
                .addSegment(3, exceptions, pw, ph, ZAG_ZIG)
                //                .addSegment(3, pw, ph, ZAG_ZIG)
                //                .addSegment(2, pw, ph, ZAG_ZIG)
                //.addSegment(2, pw, ph, ZAG_ZIG)
                .build();
        generatedDragon = dragonBuilder.build();
    }

    public void setup() {
        super.setup();
        background(220);
        rainbowPalette = new RainbowPalette();
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
            drawSegments(config.getPixelMap());

        } catch (RuntimeException e) {
            // TODO fix hack; split causes modification under iteration
            logger.error("got runtime exception while drawing: " + e.getMessage(), e);
            pixelIndex = 0;

        } finally {

            popMatrix();
            if (drawGeneratedDragon) {
                drawGeneratedDragon();
            }
            popMatrix();
            drawUi();
        }
    }

    private void drawGeneratedDragon() {
        drawSegments(generatedDragon.getPixelMap());
    }

    /**
     * Elastic band from the centre of the view to the rendered model in case haywire and not find.
     */
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

    /**
     * Summary of important mapping info across the segments, e.g. strip number clashes
     */
    private PGraphics drawSegmentSummary() {
        PGraphics graphics = createGraphics(width, height);
        graphics.beginDraw();

        // Draw summary of clashing segments, but only non-ignored ones which have no stripNumberOverride which we use
        // to indicate that the strip number is to be used as written regardless of a "clash". e.g. the strip for the
        // tail is broken into two segments which must have the same strip number as each other.
        Map<ImmutablePair<String, String>, Set<Integer>> clashes = config.calculateClashes(s ->
                !s.getIgnored() && s.getStripNumberOverride() == null);

        if (!clashes.isEmpty()) {
            graphics.fill(0);
            String clashesDesc = Config.describeClashes(clashes);
            String unusedStripNums = config.getUnusedStripNumbers(0, 23).toString();
            graphics.text(clashesDesc + "\n\nunused strip nums:\n" + unusedStripNums, 20, 20);
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

    protected void applyGlobalTransforms() {
        applyGlobalTransforms(config);
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

            if (pixels.size() > 0) {
                PixelPoint thisPixel = pixels.get(pixelIndex);
                text.append("Strip: ").append(thisPixel.getStrip())
                        .append(" Pixel number: ").append(thisPixel.getPixel() - segment.getPixelIndexBase())
                        .append(" x,y: ").append(thisPixel.getPoint().toString()).append("\n");
            }
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
     * @param pixelMap the list of {@link Segment Segments} to draw.
     */
    private void drawSegments(List<Segment> pixelMap) {
        for (int i = 0; i < pixelMap.size(); i++) {
            Segment segment = pixelMap.get(i);
            if (segment.getEnabled()) {
                pushMatrix();
                pushStyle();
                noFill();
                segment.getTransforms().stream().filter(Transform::isUnbaked).forEach(this::applyTransform);
                // draw it
                Rect r = segment.getPixels().size() > 0 ? calculateScreenBox(segment) : NULL_RECT;

                int metaColour;
                if (segment.getIgnored()) {
                    // ignored stuff is feinter
                    metaColour = color(127, 127, 0);
                    strokeWeight(0.25f);
                } else if (i == selectedSegment) {
                    if (r.contains(mouseX, mouseY)) {
                        fill(255, 255, 255, 200);
                    }

                    metaColour = color(255, 0, 0);
                    strokeWeight(2);
                    if (showImage) {
                        drawSegmentImage(segment);
                    }
                } else {
                    metaColour = color(255, 100, 0);
                    strokeWeight(0.5f);
                }
                stroke(metaColour);
                pushMatrix();
                resetMatrix();
                rect(r);
                fill(metaColour);
                if (showSegmentLabels) {
                    drawLabel(segment, r);
                }
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
                drawModelPoints(segment.getPixels(), lineAlpha, rainbow, rainbowPalette.getColours(), highlightedPixel, brightHighlight, wire, fg, true, segment.getPixelIndexBase());
                popStyle();
                popMatrix();
            }
        }
    }

    /**
     * Draws the name of the segment.
     *
     * @param segment     the segment whose name is to be drawn
     * @param boundingBox the box into which the name is to be drawn.
     */
    private void drawLabel(Segment segment, Rect boundingBox) {
        if (segment.getName() != null) {
            int padding = 4;
            Point topLeftCorner = boundingBox.getMinMin();
            int textAreaHeight = 20;
            int x1 = topLeftCorner.getX();
            int y1 = topLeftCorner.getY() - textAreaHeight;
            int x2 = boundingBox.getMaxMax().getX();
            int y2 = topLeftCorner.getY();
            PGraphics pg = createGraphics(x2 - x1, y2 - y1);
            pg.beginDraw();
            pg.background(200, 200, 155, 0);
            pg.stroke(255, 100, 0);
            pg.strokeWeight(2);
            pg.fill(200, 200, 155);
            pg.textFont(getDefaultFont());
            pg.textSize((float) (getDefaultFont().getSize() * 0.7));
            pg.rect(0, 0, Math.min(pg.width, pg.textWidth(segment.getName()) + (padding * 2)), pg.height, padding, padding, 0, 0); // note reusing padding as corner radius
            pg.fill(255, 100, 0);
            pg.text(segment.getName(), padding, padding, pg.width - padding, pg.height - padding);
            pg.endDraw();
            image(pg, x1, y1);
        }
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
                logger.error("can't load image file " + file + " " + e.getMessage());
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
            // we are shifting the whole view
            float newViewShiftX = viewShiftX + mouseX - pmouseX;
            float newViewShiftY = viewShiftY + mouseY - pmouseY;
            // TODO range checking for zoom and shift
            viewShiftX = newViewShiftX;
            viewShiftY = newViewShiftY;
        } else if (event.isControlDown()) {
            // are we already dragging a pixel?
            if (draggingPixelPoint != null) {
                logger.debug("continuing to drag pixelPoint " + draggingPixelPoint);
                // note we assume the draggingPixelPoint is in the selected segment (upheld elsewhere)
                withAllTransforms(getSelectedSegment(), s -> {

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
                handlePointAt(mouseX, mouseY, pp -> {
                    logger.debug("started dragging a point {} {} {}", mouseX, mouseY, pp);
                    draggingPixelPoint = pp;
                });
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
        segment.getTransforms().stream().filter(Transform::isUnbaked).forEach(this::applyTransform);
        go.accept(segment);
    }

    @Override
    public final void mouseReleased(MouseEvent event) {
        logger.info("not dragging a pixelPoint");
        draggingPixelPoint = null;
    }

    @Override
    public final void mouseMoved(MouseEvent event) {
        // don't change the selected point if the control key is down, we might be moving a point
        if (!event.isControlDown()) {
            handlePointAt(event.getX(), event.getY(), pp -> pixelIndex = getIndexFor(getSelectedSegment(), pp));
        }
    }

    private void handlePointAt(final int sx, final int sy, final Consumer<PixelPoint> pointConsumer) {
        withAllTransforms(getSelectedSegment(), s -> {
            // now only calculate pointer distances for current segment points if the pointer is in the bounding box
            if (calculateScreenBox(s).contains(sx, sy)) {
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
     * Finds in the segment the given pixelpoint and returns its zero-based ordinal or -1 if absent.
     *
     * @param segment the segment.
     * @param pp      the pixel point.
     */
    private static int getIndexFor(Segment segment, PixelPoint pp) {
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
            if (k == TOGGLE_DRAW_GENERATED_DRAGON) {
                drawGeneratedDragon = !drawGeneratedDragon;
            }
            if (k == TOGGLE_SEGMENT_LABELS) {
                showSegmentLabels = !showSegmentLabels;
            }
            if (k == SHIFT_RESET_VIEW && event.isShiftDown()) {
                resetView();
            }
            if (k == HELP) {
                printHelp();
            }
            // movement step size choices
            handleStepSizeKey(k);
        }
        popMatrix();
    }

    private void handleStepSizeKey(char k) {
        int i = NUMBERS.indexOf(k);
        if (i >= 0) {
            dt = POWERS[i];
            logger.info("setting dt to {}", dt);
        }
    }

    private void handleSegmentKey(KeyEvent event) {
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
            segment.rotate(THETA * dt);
        }
        if (k == ROTATE_SEGMENT_CCW) {
            segment.rotate(-THETA * dt);
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

        if (k == META_GLOBAL_SCALE_UP) {
            config.multiplyGlobalScale(1.01);
        }
        if (k == META_GLOBAL_SCALE_DOWN) {
            config.multiplyGlobalScale(0.99);
        }
        if (k == META_GLOBAL_SCALE_RESET) {
            config.setGlobalScale(Type.SCALE.id);
        }
    }

    private void printHelp() {
        logger.info("Keyboard Shortcuts:");
        keys.forEach((key, val) -> logger.info(key + " '" + val + "'"));
    }

    public static void main(String[] args) {
        logger.info("Map Editor system startup");
        // TODO replace OS-specific paths with app-relative paths based on OS
        setNativeLibraryPaths();
        PApplet.main(MapEditor.class, args);
    }

}
