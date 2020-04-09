package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import processing.core.PApplet;

import java.io.IOException;

import static java.util.stream.Collectors.toList;

/**
 * Transitional utility to get Config's pixels into their post-transform space by applying
 * the last transform for each segment and saving back the screenspace values for the pixels
 * into the pixel map. The modelToScreen() implementation in Processing seems to only apply
 * the inverse transform from the top of the matrix stack in order to get from model space to
 * screen space so sucessive baking operations on only one transform per segment are required
 * to complete the baking.
 * <p>
 * This thing will probably be deleted soon.
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
