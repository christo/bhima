package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.StickSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static java.lang.Runtime.getRuntime;

/**
 * Plays one or more movies.
 */
public class MoviePlayer extends AbstractDragonProgram implements DragonProgram {

    private static final Logger logger = LoggerFactory.getLogger(MoviePlayer.class);

    private static long MS_MOVIE_CYCLE_PERIOD = 1000 * 900;

    private StickSlurper videoMonitor;

    private int currentVideoIndex = -1;
    private long currentVideoStartMs = 0;
    private Movie movie = null;
    private List<String> builtInVideos;

    @Override
    public void settings(DragonMind mind) {

    }

    @Override
    public void setup(DragonMind mind) {

        builtInVideos = Arrays.asList(
                "video/betterfire400x100.m4v",
                "video/golden-cave.m4v",
                "video/colour-ink.m4v",
                "video/laser-mountain.m4v",
                "video/clouds.m4v",
                "video/quick-threads.m4v",
                "video/dots-waves.m4v",
                "video/diagonal-bars.mp4",
                "video/diamonds.m4v",
                "video/Star Pink Vj ANIMATION FREE FOOTAGE HD-oMM1wsQEU-M.mp4",
                //"video/50x1000 red scales.mov", // video doesn't work
                "video/100x1000 aztec rug.m4v"
        );

        videoMonitor = new StickSlurper();
        videoMonitor.start();
        getRuntime().addShutdownHook(new Thread(() -> videoMonitor.stop(), "StickSlurper Shutdown Hook"));
    }

    @Override
    public void mouseClicked(DragonMind mind) {
        super.mouseClicked(mind);
    }

    @Override
    public PGraphics draw(DragonMind mind, int width, int height) {
        keepFreshMovie(mind);
        if (movie != null) {
            PImage pImage = movie.get();
            PGraphics pg = mind.createGraphics(width, height);
            pg.beginDraw();

            pg.image(pImage, 0, 0);
            pg.endDraw();
            return pg;
        }
        // can't use movie
        return super.draw(mind, width, height);
    }


    void keepFreshMovie(DragonMind mind) {
        long now = System.currentTimeMillis();
        if (currentVideoStartMs + MS_MOVIE_CYCLE_PERIOD < now) {
            logger.info("time to load new movie");
            currentVideoIndex++;
            List<File> media = videoMonitor.getMedia();

            try {
                Movie newMovie = null;
                // TODO figure out if we need to dispose of anything in movieland when we load a new movie
                if (media.size() > 0) {
                    logger.info("Loading new movie from media library");
                    currentVideoIndex %= media.size();
                    String movieFile = media.get(currentVideoIndex).getAbsolutePath();
                    newMovie = setupMovie(mind, movieFile);
                    currentVideoStartMs = now;
                } else {
                    logger.info("Loading new movie from built-ins");
                    currentVideoIndex %= builtInVideos.size();
                    newMovie = setupMovie(mind, builtInVideos.get(currentVideoIndex));
                    currentVideoStartMs = now;
                }
                if (movie != null) {
                    movie.dispose();
                }
                if (newMovie != null) {
                    movie = newMovie;
                    logger.info("New movie loaded OK");
                } else {
                    // need to reload it?
                    movie = setupMovie(mind, movie.filename);
                }
            } catch (NearDeathExperience e) {
                logger.warn("Movie loading failed. Dodging death.");
                videoMonitor.excludeMovieFile(media.get(currentVideoIndex).getName());
            }
        }
    }

    /**
     * Load and start looping movie with the given file name.
     *
     * @param movieFile the movie file name to load.
     * @return the {@link Movie}.
     */
    private Movie setupMovie(DragonMind mind, String movieFile) {
        logger.info("setting up movie " + movieFile);
        Movie movie = new Movie(mind, movieFile);
        movie.speed(1f);
        movie.loop();
        return movie;
    }
}
