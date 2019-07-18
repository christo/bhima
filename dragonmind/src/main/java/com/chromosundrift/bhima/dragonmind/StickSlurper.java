package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Watches for specific mounted filesystem and slurps everything in /bhima/ reading the contents as
 * animations to play.
 */
public class StickSlurper {

    private static final Logger logger = LoggerFactory.getLogger(StickSlurper.class);
    private static final String EXCLUDE_DIRS = "(Macintosh HD|Time Machine Backups|com.apple.TimeMachine.localsnapshots)";
    private static final String VIDEO_DIR = "bhima";

    private final File baseDir = new File("/Volumes");

    private final ScheduledExecutorService dirWatcher = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "Dir Watcher"));

    private List<File> dirs = Collections.synchronizedList(new ArrayList<>());

    List<String> dudMovies = new ArrayList<>();

    private final FileFilter okMovies = f -> !dudMovies.contains(f.getName()) && f.isFile() && !f.getName().matches("(^\\..*|.*\\.mov$)");

    /**
     * Gets a fresh list of directories called {@link #VIDEO_DIR} in USB drives wherein we expect to find video files.
     *
     * @return the list of directories.
     */
    private List<File> getMediaDirs() {
        List<File> newDirs = new ArrayList<>();
        if (baseDir.isDirectory()) {
            File[] drives = baseDir.listFiles(file -> file.isDirectory() && !file.getName().matches(EXCLUDE_DIRS));
            if (drives != null) {
                for (File drive : drives) {
                    File[] bhimas = drive.listFiles(f -> f.isDirectory() && f.getName().equalsIgnoreCase(VIDEO_DIR));
                    if (bhimas != null && bhimas.length > 0) {
                        newDirs.addAll(Arrays.asList(bhimas));
                    }
                }
            }
        } else {
            logger.error("can't find video dirs in {} dir", baseDir.getName());
        }
        // FIXME inelegant
        dirs.clear();
        dirs.addAll(newDirs);
        if (dirs.size() == 0) {
            logger.info("Media dirs: None");
        } else {
            logger.info("Media dirs: {}", dirs.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        }
        return dirs;
    }

    public void start() {
        logger.info("starting stick slurper");
        dirWatcher.schedule(this::getMediaDirs, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.info("shutting down stick slurper");
        dirWatcher.shutdown();
    }

    public List<File> getMedia() {
        List<File> media = new ArrayList<>();
        for (File dir : dirs) {
            if (dir.exists() && dir.isDirectory()) {
                // TODO should filter out non-video files. Don't know right now what file extensions work.
                List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles(okMovies)));
                media.addAll(files);
                logger.info("added {} files from {}", files.size(), dir.getAbsolutePath());
            } else {
                logger.warn("dir not good: {}", dir.getPath());
            }
        }
        return media;
    }

    public void excludeMovieFile(String filename) {
        logger.info("excluding movie {}", filename);
        dudMovies.add(filename);
    }

}
