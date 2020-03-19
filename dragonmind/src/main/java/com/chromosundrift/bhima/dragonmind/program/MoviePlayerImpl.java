package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ImageDeserializer;
import com.chromosundrift.bhima.api.ImageSerializer;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.StickSlurper;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.chromosundrift.bhima.api.ProgramInfo.NULL_PROGRAM_INFO;
import static java.lang.Runtime.getRuntime;
import static java.util.stream.Collectors.toList;

/**
 * Plays one or more movies.
 */
public class MoviePlayerImpl extends AbstractDragonProgram implements DragonProgram, MoviePlayer {

    private static final Logger logger = LoggerFactory.getLogger(MoviePlayerImpl.class);
    public static final int THUMBNAIL_WIDTH = 400;
    public static final int THUMBNAIL_HEIGHT = 100;

    private long movieCyclePeriodMs = 1000 * 60 * 5;

    private StickSlurper videoMonitor;
    private ObjectMapper objectmapper;

    private boolean mute = false;
    private int currentVideoIndex = -1;
    private long currentVideoStartMs = 0;
    private Movie movie = null;
    private List<String> builtInVideos;
    private DragonMind mind;
    private float fps;

    @Override
    public void settings(DragonMind mind) {

    }

    public ProgramInfo getCurrentProgramInfo(int x, int y, int w, int h) {
        if (movie == null) {
            return NULL_PROGRAM_INFO;
        } else {
            return getProgramInfo(movie, x, y, w, h);
        }
    }

    private ProgramInfo getProgramInfo(Movie m, int x, int y, int w, int h) {
        BufferedImage bi = getMovieImage(m, x, y, w, h);
        return getMovieProgramInfo(bi, m.filename);
    }

    private BufferedImage getMovieImage(Movie m, int x, int y, int w, int h) {
        try {
            Image image = m.getImage();
            if (image == null) {
                logger.warn("movie image is null!");
                return NULL_PROGRAM_INFO.getThumbnail();
            }
            if (image.getClass().isAssignableFrom(BufferedImage.class)) {
                return (BufferedImage) image;
            }
            return imageToBufferedImage(image, x, y, w, h);
        } catch (RuntimeException e) {
            logger.warn("unable to get image", e);
            return NULL_PROGRAM_INFO.getThumbnail();
        }
    }

    private ProgramInfo getMovieProgramInfo(BufferedImage bi, String f) {
        String name = f.substring(f.lastIndexOf('/') + 1, f.length() - 4);
        Map<String, String> settings = new HashMap<>();
        settings.put("FPS", Float.toString(fps));
        settings.put("muted", Boolean.toString(mute));
        return new ProgramInfo(f, name, "Movie", bi, settings);
    }

    @Override
    public void setup(DragonMind mind) {
        if (mind == null) {
            throw new NullPointerException();
        }
        this.mind = mind;
        builtInVideos = Arrays.asList(
                "video/400x100/mushroom-moments.m4v",
                "video/400x100/flying-food.m4v",
                "video/400x100/zebra-trippin.m4v",
                "video/400x100/train-mirror.m4v",
                "video/400x100/chemical.m4v",
                "video/400x100/geometrix.m4v",
                "video/400x100/ink-tank.m4v",
                "video/400x100/red-dots.m4v",
                "video/400x100/cosmology.m4v",
                "video/400x100/electric-sheep.m4v",
                "video/400x100/inner-space.m4v",
                "video/400x100/candy-crush.m4v",
                "video/laser-mountain.m4v",
                "video/kaliedoscope.mp4",
                "video/frostyloop.mp4",
                "video/pink-star.mp4",
                "video/betterfire400x100.m4v",
                "video/golden-cave.m4v",
                "video/colour-ink.m4v",
                "video/clouds.m4v",
                "video/quick-threads.m4v",
                "video/dots-waves.m4v",
                "video/candy-stripes.mp4",
                "video/diamonds.m4v",
                //"video/50x1000 red scales.mov", // video doesn't work
                "video/100x1000 aztec rug.m4v"
        );
        objectmapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ImageSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(BufferedImage.class, new ImageSerializer());
        module.addDeserializer(BufferedImage.class, new ImageDeserializer());
        objectmapper.registerModule(module);
        generateInfos(builtInVideos);
        videoMonitor = new StickSlurper();
        videoMonitor.start();
        getRuntime().addShutdownHook(new Thread(() -> videoMonitor.stop(), "StickSlurper Shutdown Hook"));
    }

    private void generateInfos(List<String> videoFiles) {
        videoFiles.forEach(s -> {
            File thumbnailFile = new File(s + ".info.json");
            if (!thumbnailFile.exists()) {
                generateInfo(s, thumbnailFile);
            } else {
                logger.info("reusing disk info for {}", s);
            }
        });
    }

    private void generateInfo(String s, File thumbnailFile) {
        try {
            objectmapper.writeValue(thumbnailFile, toProgramInfo(s, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Changes the cvrrently running movie if it's time.
     *
     * @param mind the DragonMind.
     */
    void keepFreshMovie(DragonMind mind) {
        long now = System.currentTimeMillis();
        boolean loopMovie = movie == null || (movie.duration() * 1000) < movieCyclePeriodMs;
        if (currentVideoStartMs + movieCyclePeriodMs < now && loopMovie) {
            logger.info("time to load new movie");
            currentVideoIndex++;
            List<File> media = videoMonitor.getMedia();

            try {
                Movie newMovie = null;
                if (media.size() > 0) {
                    logger.info("Loading new movie from media library");
                    currentVideoIndex %= media.size();
                    String movieFile = media.get(currentVideoIndex).getAbsolutePath();
                    newMovie = setupMovie(mind, movieFile);
                } else {
                    logger.info("Loading new movie from built-ins");
                    currentVideoIndex %= builtInVideos.size();
                    newMovie = setupMovie(mind, builtInVideos.get(currentVideoIndex));
                }
                currentVideoStartMs = now;
                if (movie != null) {
                    movie.dispose();
                }
                movie = newMovie;
                logger.info("New movie loaded OK");
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
        fps = Math.min(movie.frameRate, 30);
        logger.debug("playing back {} at {} FPS (native framerate is {})", movieFile, fps, movie.frameRate);
        movie.frameRate(fps);
        movie.volume(mute ? 0f : 1f);
        movie.loop();
        return movie;
    }

    /**
     * Gets movies as {@link ProgramInfo} instances with a thumbnail based on the requested rectangle.
     * The movies are the loaded movies or built-in ones if there are no other media loaded.
     *
     * @return the list of movie infos.
     */
    public List<ProgramInfo> getProgramInfos(int x, int y, int w, int h) {
        List<File> media = videoMonitor.getMedia();
        Stream<String> filenames = media.size() > 0
                ? media.stream().map(File::getName)
                : builtInVideos.stream();
        return filenames.map(s -> loadProgramInfo(s)).collect(toList());
    }

    private ProgramInfo loadProgramInfo(String filename) {
        try {
            File src = new File(filename + ".info.json");
            return objectmapper.readValue(src, ProgramInfo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thumbnail based info about the given movie filename.
     */
    private ProgramInfo toProgramInfo(String filename, int x, int y, int w, int h) {
        Movie thisMovie = null;
        try {
            thisMovie = setupMovie(mind, filename);
            float secs = Math.min(thisMovie.duration()/2, 8f);
            thisMovie.jump(secs); // take thumbnail from secs into the movie
            thisMovie.loadPixels();
            return getProgramInfo(thisMovie, x, y, w, h);
        } catch (RuntimeException re) {
            // TODO maybe catch NearDeathExperience and exclude the movie like above
            logger.error("cannot generate thumbnail for {}", filename, re);
            return getMovieProgramInfo(NULL_PROGRAM_INFO.getThumbnail(), filename);
        } finally {
            if (thisMovie != null) {
                thisMovie.dispose();
            }
        }
    }

    @Override
    public ProgramInfo runProgram(String id) {
        Stream<ProgramInfo> stream = getProgramInfos(0, 0, 400, 100).stream();
        Optional<ProgramInfo> opi = stream.filter(pi -> pi.getId().equals(id)).findFirst();
        if (opi.isPresent()) {
            Movie newMovie = setupMovie(mind, id);
            if (movie != null) {
                movie.dispose();
            }
            movie = newMovie;
            currentVideoStartMs = System.currentTimeMillis();
        }
        return opi.orElse(NULL_PROGRAM_INFO);

    }
}


