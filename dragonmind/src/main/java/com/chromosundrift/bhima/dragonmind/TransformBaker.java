package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import processing.core.PApplet;

import java.io.IOException;

import static java.util.stream.Collectors.toList;

/**
 * Wherein the Config's Pixels are transformed and then returned and stored in a new file
 * with equivalent post-transform pixel data values and the list of transforms is empty. The
 * resulting space is no longer registered with the original scan source image. This reflects
 * a temporary project to move the Dragon model into a synthetic geometry, rather than the
 * apparent layout in its warped dragon-winding way. The dragon scales are effectively arrayed
 * as a diamond grid (like cyclone fencing) rectangle albeit with an oft-quirky wiring route.
 * <p>
 * The resulting pixel maps reflect a close approximation of the rectangular diamond grid, all
 * in the same pixel space. Screen space is one measly transform away. The next step is to get
 * all the pixels to snap to their correct position with some expectation of the need to tweak
 * the map editor to ensure it can make it easy to edit aliasing errors in the "snap to" operation.
 * This class is not expected to be needed after that data cleansing operation has completed.
 */
public class TransformBaker extends DragonMind {

    @Override
    public void setup() {

        try {
            Config c = loadConfigFromFirstArgOrDefault();
            c.getPixelMap().forEach(segment -> {
                segment.getTransforms().stream().filter(t1 -> !t1.isBaked()).reduce((f, s) -> s).ifPresent(t -> {
                    applyTransform(t);
                    segment.setPixels(segment.getPixels().stream().map(this::modelToScreen).collect(toList()));
                    t.setBaked(true);
                    resetMatrix();
                });
            });
            c.save("baked-transform.json"); // i know
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        noLoop();
    }

    private PixelPoint modelToScreen(PixelPoint pp) {
        return new PixelPoint(pp.getStrip(), pp.getPixel(), modelToScreen(pp.getX(), pp.getY()));
    }

    @Override
    public void settings() {
        size(400, 400);
    }

    public static void main(String[] args) {
        setNativeLibraryPaths();
        PApplet.main(TransformBaker.class, args);
    }
}
