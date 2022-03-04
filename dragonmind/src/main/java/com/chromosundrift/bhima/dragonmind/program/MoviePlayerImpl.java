package com.chromosundrift.bhima.dragonmind.program;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;

import com.chromosundrift.bhima.api.ImageDeserializer;
import com.chromosundrift.bhima.api.ImageSerializer;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.api.ProgramType;
import com.chromosundrift.bhima.dragonmind.CompositeMedia;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.LocalVideos;
import com.chromosundrift.bhima.dragonmind.MediaSource;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.VideoLurker;
import static com.chromosundrift.bhima.api.ProgramInfo.getNullProgramInfo;
import static com.chromosundrift.bhima.dragonmind.OsUtils.getMediaBaseDir;

/**
 * Plays one or more movies.
 */
public class MoviePlayerImpl extends AbstractDragonProgram implements DragonProgram {

    private static final Logger logger = LoggerFactory.getLogger(MoviePlayerImpl.class);

    public static final int THUMBNAIL_WIDTH = 400;
    public static final int THUMBNAIL_HEIGHT = 100;

    private static final String VIDEO_DIR_NAME = "video";
    /** Time offset from beginning of video to use as thumbnail */
    private static final float THUMBNAIL_TIME_OFFSET = 24f;
    private static final boolean CACHE_INFOS = true;

    /** Default time to loop short videos for */
    private long movieCyclePeriodMs = 1000 * 60 * 5;
    private MediaSource mediaSource;
    private ObjectMapper objectmapper;
    private boolean mute = false;
    private int currentVideoIndex = -1;
    private long currentVideoStartMs = 0;
    private Movie movie = null;
    private DragonMind mind;

    /**
     * The derived frame rate of the currently running movie.
     */
    private float fps;

    public MoviePlayerImpl() {
        logger.debug("constructor");
    }

    @Override
    public void settings(DragonMind mind) {

    }

    public ProgramInfo getCurrentProgramInfo(int x, int y, int w, int h) {
        if (movie == null) {
            return getNullProgramInfo();
        }
        final String filename = movie.filename;
        logger.debug("getting movie image for current movie {}", filename);
        return getMovieProgramInfoPlease(filename, x, y, w, h, movie);
    }

    /** Gets image from the movie if possible. */
    Optional<BufferedImage> getMovieImage(Movie m, int x, int y, int w, int h) {
        if (m == null) {
            throw new NullPointerException();
        }
        if (x < 0 || y < 0 || w < 0 || h < 0) {
            throw new IllegalArgumentException(format("invalid dimensions: %d, %d, %d, %d", x, y, w, h));
        }
        try {
            if (m.pixelWidth == 0 || m.pixelHeight == 0) {
                logger.warn("Movie pixel dimensions are unset: not good to capture image.");
            } else {
                logger.debug("Movie pixel dimensions are good!");
            }
            return Optional.of(getImageFrameFromMovie(m, x, y, w, h));
        } catch (RuntimeException e) {
            logger.error("unable to get image (may be race condition in processing-video/gstreamer): {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Just gets the image from the movie frame without fallback to the null placeholder image. */
    BufferedImage getImageFrameFromMovie(Movie m, int x, int y, int w, int h) {
        Image image = m.getImage();
        if (image == null) {
            throw new NullPointerException("movie image is null!");
        }
        return imageToBufferedImage(image, x, y, w, h);
    }

    private ProgramInfo getMovieProgramInfo(BufferedImage bi, String f) {
        return getMovieProgramInfo(bi, f, ProgramType.MOVIE);
    }

    private ProgramInfo getMovieProgramInfo(BufferedImage bi, String f, ProgramType type) {
        String niceName = makeNice(f);
        Map<String, String> settings = new HashMap<>();
        settings.put("FPS", Float.toString(fps));
        settings.put("muted", Boolean.toString(mute));
        return new ProgramInfo(f, niceName, type, bi, settings);
    }

    @Override
    public void setup(DragonMind mind) {
        if (mind == null) {
            throw new NullPointerException();
        }
        this.mind = mind;
        setupObjectMapper();
        LocalVideos localVideos = null;
        try {
            localVideos = new LocalVideos(VIDEO_DIR_NAME);
            final List<String> media = localVideos.getMedia();
            generateInfos(media);
        } catch (IOException e) {
            logger.error("Cannot initialise local video list", e);
        }

        VideoLurker videoLurker = new VideoLurker(getMediaBaseDir(), "bhima");

        videoLurker.start();
        getRuntime().addShutdownHook(new Thread(videoLurker::stop, "VideoLurker Shutdown Hook"));

        mediaSource = (localVideos == null)
                ? videoLurker
                : new CompositeMedia(videoLurker, localVideos);
    }

    private void setupObjectMapper() {
        objectmapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ImageSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(BufferedImage.class, new ImageSerializer());
        module.addDeserializer(BufferedImage.class, new ImageDeserializer());
        objectmapper.registerModule(module);
    }

    private void generateInfos(List<String> videoFiles) {
        videoFiles.forEach(s -> {
            // TODO fallback to matching precalculated .info.json files in resources/precalc
            File thumb = new File(s + ".info.json");
            if (!thumb.exists() || !CACHE_INFOS) {
                generateInfo(s, thumb);
            } else {
                logger.debug("reusing cached info for {}", s);
            }
        });
    }

    private void generateInfo(String s, File thumbnail) {
        try {
            objectmapper.writeValue(thumbnail, toProgramInfo(s, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        } catch (IOException e) {
            logger.warn("Can't generate file {} due to IOException {}", thumbnail.getName(), e.getMessage());
        }
    }

    @Override
    public PGraphics draw(DragonMind mind, int width, int height) {
        keepFreshMovie(mind);

        try {
            if (movie != null) {
                PImage pImage = movie.get();
                PGraphics pg = mind.createGraphics(width, height);
                pg.beginDraw();
                pg.image(pImage, 0, 0);
                pg.endDraw();
                return pg;
            }
        } catch (Exception e) {
            logger.error("something blew up during drawing", e);
        }
        // can't use movie
        return super.draw(mind, width, height);
    }

    /**
     * Changes the currently running movie if it's time.
     *
     * @param mind the DragonMind.
     */
    void keepFreshMovie(DragonMind mind) {
        boolean loopMovie = movie == null || (movie.duration() * 1000) < movieCyclePeriodMs;
        if (currentVideoStartMs + movieCyclePeriodMs < currentTimeMillis() && loopMovie) {
            logger.debug("time to load new movie");
            currentVideoIndex++;

            List<String> media = mediaSource.getMedia();
            if (media.size() != 0) {
                try {

                    currentVideoIndex %= media.size();

                    final String movieFile = media.get(currentVideoIndex);
                    Movie newMovie = setupMovie(mind, movieFile);
                    currentVideoStartMs = currentTimeMillis();
                    if (movie != null) {
                        movie.dispose();
                    }
                    movie = newMovie;
                    logger.debug("New movie loaded OK");
                } catch (NearDeathExperience e) {
                    logger.warn("Movie loading failed. Dodging death.");

                    if (!mediaSource.getMedia().isEmpty()) {
                        mediaSource.excludeMovieFile(media.get(currentVideoIndex));
                    } else {
                        final String msg = "No videos available";
                        logger.error(msg);
                        mind.fail(msg);
                    }
                }
            } else {
                final String msg = "No videos available";
                logger.error(msg);
                mind.fail(msg);
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
        logger.info("setting up movie {}", movieFile);

        Movie movie = constructMovie(mind, movieFile);
        fps = min(movie.frameRate, 30);

        logger.info("playing back {} at {} FPS (native framerate is {})", movie.filename, fps, movie.frameRate);

        movie.frameRate(fps);
        movie.volume(mute ? 0f : 1f);
        movie.loop();
        if (!movie.available()) {
            logger.warn("movie not available() in setupMovie...");
        }
        return movie;
    }

    private Movie constructMovie(DragonMind mind, String movieFile) {
        Movie movie = null;
        try {
            movie = new Movie(mind, movieFile);
            if (!movie.available()) {
                logger.warn("Movie not 'available()': {}", movieFile);
            }
        } catch (UnsatisfiedLinkError e) {
            logger.error("ULE: Probably native libraries or lib paths are missing or wrong.");
            throw e;
        }
        return movie;
    }

    /**
     * Gets movies as {@link ProgramInfo} instances with a thumbnail based on the requested rectangle.
     * The movies are the loaded movies or built-in ones if there are no other media loaded.
     *
     * @return the list of movie infos.
     */
    public List<ProgramInfo> getProgramInfos(int x, int y, int w, int h) {
        return mediaSource.getMedia().stream().map(this::loadProgramInfo).collect(toList());
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
            // generate thumbnail
            thisMovie = setupMovie(mind, filename);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // choose time offset for thumbnail
            float secs = min(thisMovie.duration() / 2, THUMBNAIL_TIME_OFFSET);
            thisMovie.jump(secs); // take thumbnail from secs into the movie
            thisMovie.loadPixels();
            logger.info("getting movie image from filename '{}'", filename);
            return getMovieProgramInfoPlease(filename, x, y, w, h, thisMovie);
        } catch (RuntimeException re) {
            logger.error("cannot generate thumbnail for {} because {}", filename, re.getMessage());
            return new ProgramInfo(filename, makeNice(filename), ProgramType.NULL, getNullProgramInfo().getThumbnail());
        } finally {
            if (thisMovie != null) {
                // clean up any native resources
                thisMovie.dispose();
            }
        }
    }

    private String makeNice(String filename) {
        return filename.substring(filename.lastIndexOf('/') + 1, filename.length() - 4);
    }

    private ProgramInfo getMovieProgramInfoPlease(String filename, int x, int y, int w, int h, Movie thisMovie) {
        Optional<BufferedImage> obi = getMovieImage(thisMovie, x, y, w, h);
        if (obi.isPresent()) {
            return getMovieProgramInfo(obi.get(), filename);
        } else {
            final BufferedImage thumb = getNullProgramInfo().getThumbnail();
            return getMovieProgramInfo(thumb, filename, ProgramType.NULL);
        }
    }

    @Override
    public ProgramInfo runProgram(String id) {
        // note in our case id is a file name
        Stream<ProgramInfo> stream = getProgramInfos(0, 0, 400, 100).stream();
        Optional<ProgramInfo> opi = stream.filter(pi -> pi.getId().equals(id)).findFirst();
        if (opi.isPresent()) {
            try {
                switchToMovie(id);
                return opi.get();
            } catch (MovieException e) {
                logger.warn("Can't run movie {} because something blew up", id);
            }
        } else {
            logger.warn("Can't find program with id {}", id);
            return getCurrentProgramInfo(0, 0, 400, 100);
        }
        return getNullProgramInfo();
    }

    private void switchToMovie(String file) throws MovieException {
        Movie newMovie = setupMovie(mind, file);
        if (movie != null) {
            movie.dispose();
        }
        movie = newMovie;
        currentVideoStartMs = currentTimeMillis();
    }
}


