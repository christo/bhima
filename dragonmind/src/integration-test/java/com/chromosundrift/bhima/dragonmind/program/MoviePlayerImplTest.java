package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.ProcessingBase;
import org.junit.Test;
import org.junit.Assert;
import processing.core.PApplet;
import processing.core.ThinkDifferent;
import processing.video.Movie;

import java.awt.image.BufferedImage;

public class MoviePlayerImplTest {

    /**
     * This will pass if we can read the underlying video file to get the dimensions of the video.
     * The specific subsystems checked here include Processing, the Processing Video library, its gstreamer java
     * bindings and corresponding platform-specific native libraries which much be loaded fromF the file system
     * under explicit configuration.
     */
    @Test
    public void testVideoFileReadingSubsystem() {
        PApplet papplet = mockPApplet();
        System.setProperty("gstreamer.library.path", "processing-video-lib/video/library/macosx");
        System.setProperty("gstreamer.plugin.path", "processing-video-lib/video/library/macosx/gstreamer-1.0");
        Movie movie = new Movie(papplet, "video/aztec-rug.m4v");
        MoviePlayerImpl player = new MoviePlayerImpl();

        final BufferedImage movieImage = player.getMovieImage(movie, 0, 0, 0, 0);
        final int width = movieImage.getWidth();
        Assert.assertEquals("movie frame should be 400x100", 400, width);
        final int height = movieImage.getHeight();
        Assert.assertEquals("movie frame should be 400x100", 100, height);
    }

    /**
     * @return a hopefully nonexplosive PApplet instance for testing dependents.
     */
    private static PApplet mockPApplet() {
        class MockSketch extends PApplet {
            public MockSketch() {
                // need to do this or we don't get far
                initSurface();
            }
        }
        PApplet papplet = new MockSketch();
        if (ProcessingBase.getOs().contains("osx")) {
            // call this thing otherwise a later attempt to set icon fails
            ThinkDifferent.init(papplet);
        }
        return papplet;
    }
}
