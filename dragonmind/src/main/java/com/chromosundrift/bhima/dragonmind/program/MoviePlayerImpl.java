package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ImageDeserializer;
import com.chromosundrift.bhima.api.ImageSerializer;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.CompositeMedia;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import com.chromosundrift.bhima.dragonmind.LocalVideos;
import com.chromosundrift.bhima.dragonmind.MediaSource;
import com.chromosundrift.bhima.dragonmind.NearDeathExperience;
import com.chromosundrift.bhima.dragonmind.VideoLurker;
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

import static com.chromosundrift.bhima.api.ProgramInfo.NULL_PROGRAM_INFO;
import static java.lang.Runtime.getRuntime;
import static java.util.stream.Collectors.toList;

/**
 * Plays one or more movies.
 */
public class MoviePlayerImpl extends AbstractDragonProgram implements DragonProgram {

    public static final int THUMBNAIL_WIDTH = 400;
    public static final int THUMBNAIL_HEIGHT = 100;

    private static final Logger logger = LoggerFactory.getLogger(MoviePlayerImpl.class);
    private static final String VIDEO_DIR_NAME = "video";

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

    @Override
    public void settings(DragonMind mind) {

    }

    public ProgramInfo getCurrentProgramInfo(int x, int y, int w, int h) {
        return movie == null ? NULL_PROGRAM_INFO : getProgramInfo(movie, x, y, w, h);
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
            logger.warn("unable to get image (may be race condition in processing's native gstreamer stack)");
            return NULL_PROGRAM_INFO.getThumbnail();
        }
    }

    private ProgramInfo getMovieProgramInfo(BufferedImage bi, String f) {
        String niceName = f.substring(f.lastIndexOf('/') + 1, f.length() - 4);
        Map<String, String> settings = new HashMap<>();
        settings.put("FPS", Float.toString(fps));
        settings.put("muted", Boolean.toString(mute));
        return new ProgramInfo(f, niceName, "Movie", bi, settings);
    }

    @Override
    public void setup(DragonMind mind) {
        if (mind == null) {
            throw new NullPointerException();
        }
        this.mind = mind;
        objectmapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ImageSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(BufferedImage.class, new ImageSerializer());
        module.addDeserializer(BufferedImage.class, new ImageDeserializer());
        objectmapper.registerModule(module);
        LocalVideos localVideos = null;
        try {
            localVideos = new LocalVideos(VIDEO_DIR_NAME);
            final List<String> media = localVideos.getMedia();
            generateInfos(media);
        } catch (IOException e) {
            logger.error("Cannot initialise local video list");
        }

        // TODO OSX-specific, fix for linux
        VideoLurker videoLurker = new VideoLurker("/Volumes", "bhima");
        videoLurker.start();
        getRuntime().addShutdownHook(new Thread(videoLurker::stop, "VideoLurker Shutdown Hook"));
        if (localVideos == null) {
            mediaSource = videoLurker;
        } else {
            mediaSource = new CompositeMedia(videoLurker, localVideos);
        }
    }

    private void generateInfos(List<String> videoFiles) {
        videoFiles.forEach(s -> {
            File thumb = new File(s + ".info.json");
            if (!thumb.exists()) {
                generateInfo(s, thumb);
            } else {
                logger.info("reusing cached info for {}", s);
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
     * Changes the currently running movie if it's time.
     *
     * @param mind the DragonMind.
     */
    void keepFreshMovie(DragonMind mind) {
        long now = System.currentTimeMillis();
        boolean loopMovie = movie == null || (movie.duration() * 1000) < movieCyclePeriodMs;
        if (currentVideoStartMs + movieCyclePeriodMs < now && loopMovie) {
            logger.debug("time to load new movie");
            currentVideoIndex++;

            List<String> media = mediaSource.getMedia();

            try {
                currentVideoIndex %= media.size();
                final String movieFile = media.get(currentVideoIndex);
                Movie newMovie = setupMovie(mind, movieFile);
                currentVideoStartMs = now;
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
            //File movieFile = new File(filename);
            thisMovie = setupMovie(mind, filename);
            // either 8s from beginning or if movie is shorter, the half-way point
            float secs = Math.min(thisMovie.duration() / 2, 8f);
            thisMovie.jump(secs); // take thumbnail from secs into the movie
            thisMovie.loadPixels();
            return getProgramInfo(thisMovie, x, y, w, h);
        } catch (RuntimeException re) {
            logger.error("cannot generate thumbnail for {} because {}", filename, re.getMessage());
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
            switchToMovie(id);
        }
        return opi.orElse(NULL_PROGRAM_INFO);
    }

    private void switchToMovie(String id) {
        Movie newMovie = setupMovie(mind, id);
        if (movie != null) {
            movie.dispose();
        }
        movie = newMovie;
        currentVideoStartMs = System.currentTimeMillis();
    }
}


