package com.chromosundrift.bhima.dragonmind;

import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import processing.core.PApplet;

import java.util.List;

/**
 * Runs a test pattern regardless of mapping.
 */
public class TestPattern extends DragonMind {

    long t = 0;

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void draw() {

        // update the pixelpushers with current test pattern

        fill(0);
        rect(0, 0, width, height);
        fill(128);

        if (getPusherMan().isReady()) {
            drawPattern();
        }
        text(getPusherMan().report(), width / 3, height / 2);

    }

    private void drawPattern() {

        long changeEvery = millis() / 15000;
        long animationFrame = millis() / 80;
        int numLights = getPusherMan().numTotalLights();

        List<Strip> strips = getPusherMan().getStrips();

        switch ((int) (changeEvery % 5)) {
            case 0:
                // make every 5th light white
                for (Strip strip : strips) {
                    for (int i = 0; i < strip.getLength(); i++) {
                        if (animationFrame % (i + 1) == 0) {
                            if (numLights > 0 && t % 5 == 0) {
                                strip.setPixel(color(255, 255, 255), i);
                            } else {
                                strip.setPixel(color(0, 0, 0), i);
                            }
                        }
                    }
                }
                break;
            case 1:
                modFrame(animationFrame, color(255, 0, 255));
                break;

            case 2:
                modFrame(animationFrame, color(255, 255, 0));
                break;
            case 3:
                modFrame(animationFrame, color(0, 255, 255));
                break;

            case 4:
                for (Strip strip : strips) {
                    for (int i = 0; i < strip.getLength(); i++) {
                        if (animationFrame % (i + 1) == 0) {
                            if (numLights > 0 && t % numLights == 0) {
                                strip.setPixel(color(255, 255, 255), i);
                            } else {
                                strip.setPixel(color(0, 0, 0), i);
                            }
                        }
                    }
                }
                break;

        }
        t++;
    }

    private void modFrame(long animationFrame, int color) {

        List<Strip> strips = getPusherMan().getStrips();
        for (Strip strip : strips) {
            for (int i = 0; i < strip.getLength(); i++) {
                if (animationFrame % (i + 1) == 0) {
                    strip.setPixel(color, i);
                } else {
                    strip.setPixel(color(0, 0, 0), i);
                }
            }
        }
    }

    public static void main(String[] args) {
        // TODO fix dependency on Processing native libs
        //System.setProperty("jogl.debug", "true");
        setNativeLibraryPaths();
        PApplet.main(TestPattern.class, args);
    }
}
