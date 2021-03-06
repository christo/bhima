package com.chromosundrift.bhima.dragonmind.model;

import com.chromosundrift.bhima.geometry.Point;
import com.chromosundrift.bhima.geometry.Rect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

@SuppressWarnings("WeakerAccess")
@JsonInclude(NON_NULL)
@JsonPropertyOrder({"name", "description", "stripNumberOverride", "enabled", "ignored", "background", "pixelIndexBase", "transforms", "pixels"})
public final class Segment {

    private static final Pattern SCAN_ID_RX = Pattern.compile("mapping file is mappings/Mapping(\\d+)\\.csv");
    @JsonProperty("segment")
    private String name;

    private String description;

    private Background background;

    @JsonInclude(NON_NULL)
    private Integer stripNumberOverride = null;

    @JsonInclude(NON_NULL)
    private String subassembly;

    /**
     * Does not light up nor display in the editor.
     */
    private boolean enabled = true;

    /**
     * Does not light up, usually because hardware mirroring is on.
     */
    private boolean ignored = false;

    private int pixelIndexBase = 0;

    @JsonInclude(NON_NULL)
    private List<Transform> transforms = new ArrayList<>();

    private List<PixelPoint> pixels = new CopyOnWriteArrayList<>();

    public Segment() {
    }

    public Segment(String name, List<PixelPoint> pixels) {
        this.name = name;
        this.pixels = pixels;
    }

    /**
     * Constructs the expected mapped image file name for the given details.
     *
     * @param scanId the id of the scan, typically a unix timestamp.
     * @param frame  normally "lightframe" or "darkframe".
     * @param strip  the strip number as scanned.
     * @param pixel  the pixel number as scanned.
     * @return the expected name of the file from the scanning session.
     */
    public static String mappedImageFile(String scanId, String frame, int strip, int pixel) {
        // image files look like this:
        // Mapping-1537359798903-lightframe-00-0000.png
        return format("mappings/Mapping-%s-%s-%02d-%04d%s", scanId, frame, strip, pixel, ".png");
    }

    /**
     * Get the smallest rectangle that contains all the points.
     *
     * @param points the points.
     * @return a rectangle snugly containing all points (including on the line).
     */
    @JsonIgnore
    public Rect getBoundingBox(Stream<Point> points) {
        final AtomicInteger minX = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger minY = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger maxX = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicInteger maxY = new AtomicInteger(Integer.MIN_VALUE);

        points.forEach((Point point) -> {
            minX.set(min(minX.get(), point.getX()));
            maxX.set(max(maxX.get(), point.getX()));
            minY.set(min(minY.get(), point.getY()));
            maxY.set(max(maxY.get(), point.getY()));
        });
        return new Rect(new Point(minX.get(), minY.get()), new Point(maxX.get(), maxY.get()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public List<PixelPoint> getPixels() {
        return pixels;
    }

    public void setPixels(List<PixelPoint> pixels) {
        this.pixels = new CopyOnWriteArrayList<>(pixels);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public Integer getStripNumberOverride() {
        return stripNumberOverride;
    }

    public void setStripNumberOverride(Integer stripNumberOverride) {
        this.stripNumberOverride = stripNumberOverride;
    }

    public int getPixelIndexBase() {
        return pixelIndexBase;
    }

    public void setPixelIndexBase(int pixelIndexBase) {
        this.pixelIndexBase = pixelIndexBase;
    }

    public String getSubassembly() {
        return subassembly;
    }

    public void setSubassembly(String subassembly) {
        this.subassembly = subassembly;
    }

    public void flipIgnored() {
        ignored = !ignored;
    }

    public void flipEnabled() {
        enabled = !enabled;
    }

    @JsonIgnore
    public boolean isActive() {
        return getEnabled() && !getIgnored();
    }

    @JsonIgnore
    public Transform addTransform(Transform transform) {
        transforms.add(transform);
        return transform;
    }

    public void rotate(float angle) {
        // Rotate transform may not exist, in which case create identity rotation transform
        Transform rotate = firstTransformByType(Transform.Type.ROTATE)
                .orElseGet(() -> addTransform(Transform.Type.ROTATE.id));
        rotate.setParameters(rotate.addRotateZ(angle).getParameters());
    }

    public void translateX(int i) {
        Transform t = firstTransformByType(Transform.Type.TRANSLATE)
                .orElseGet(() -> addTransform(Transform.translate(0f, 0f)));
        t.setParameters(t.addTranslateX(i).getParameters());
    }

    public void translateY(int i) {
        Transform t = firstTransformByType(Transform.Type.TRANSLATE)
                .orElseGet(() -> addTransform(Transform.translate(0f, 0f)));
        t.setParameters(t.addTranslateY(i).getParameters());
    }

    public void scale(double v) {
        Transform t = firstTransformByType(Transform.Type.SCALE)
                .orElseGet(() -> addTransform(Transform.Type.SCALE.id));
        t.setParameters(t.multiplyScale(v).getParameters());
    }

    public void resetScale() {
        Transform t = firstTransformByType(Transform.Type.SCALE)
                .orElseGet(() -> addTransform(Transform.Type.SCALE.id));
        t.set("x", 1f);
        t.set("y", 1f);
    }

    private Optional<Transform> firstTransformByType(Transform.Type type) {
        return getTransforms().stream().filter(type::is).findFirst();
    }

    @Override
    public String toString() {
        return "Segment{" +
                "name='" + name + '\'' +
                (background != null ? ", background=" + background : "") +
                ", pixelIndexBase=" + pixelIndexBase +
                (enabled ? ", enabled" : ", DISABLED") +
                (ignored ? ", IGNORED" : ", nonignored") +
                ", pixelcount=" + pixels.size() +
                '}';
    }

    public void addPixelPoint(PixelPoint pp) {
        pixels.add(pp);
    }

    public void addPixelPoints(Collection<PixelPoint> pps) {
        pixels.addAll(pps);
    }

    @JsonIgnore
    public Optional<String> getMappingId() {
        Matcher m = SCAN_ID_RX.matcher(getDescription());
        if (m.find()) {
            return Optional.of(m.group(1));

        } else {
            return Optional.empty();
        }
    }

    @JsonIgnore
    public Set<Integer> getEffectiveStripNumbers() {
        HashSet<Integer> stripNums = new HashSet<>();
        if (getStripNumberOverride() != null) {
            stripNums.add(getStripNumberOverride());
        } else {
            getPixels().forEach(pp -> stripNums.add(pp.getStrip()));
        }
        return stripNums;
    }
}
