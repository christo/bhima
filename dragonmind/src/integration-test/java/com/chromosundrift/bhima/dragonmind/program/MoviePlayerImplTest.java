package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.ProcessingBase;
import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.ThinkDifferent;
import processing.video.Movie;

import java.awt.image.BufferedImage;

public class MoviePlayerImplTest {

    private static final Logger logger = LoggerFactory.getLogger(MoviePlayerImplTest.class);

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
        Movie movie = new Movie(papplet, "video/candy-crush.m4v");
        movie.frameRate(30);
        movie.volume(0f);
        movie.loop();

        MoviePlayerImpl player = new MoviePlayerImpl();

        final BufferedImage movieImage = player.getImageFrameFromMovie(movie, 0, 0, 400, 100);
        final int width = movieImage.getWidth();
        Assert.assertEquals("movie frame should be 400x100", 400, width);
        final int height = movieImage.getHeight();
        Assert.assertEquals("movie frame should be 400x100", 100, height);
    }

    /**
     * @return a hopefully nonexplosive PApplet instance for testing dependents.
     */
    private static PApplet mockPApplet() {
        try {
            class MockSketch extends PApplet {
                void doInitSurface() {
                    super.initSurface();
                }
            }
            MockSketch papplet = new MockSketch();

            if (ProcessingBase.getOs().contains("mac")) {
                // call this thing otherwise a later attempt to set icon fails
                ThinkDifferent.init(papplet);
            }
            papplet.doInitSurface();
            return papplet;
        } catch (ExceptionInInitializerError e) {
            logger.error("probably running on wrong java version (>1.8), cannot mock PApplet ffs", e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("hacky mock blew up! hack more, hack different, hack better");
            throw e;
        }
    }
}
