package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Watches for specific mounted filesystem and slurps everything in the configured directory, reading the contents as
 * animations to play.
 */
public class VideoLurker {

    private static final Logger logger = LoggerFactory.getLogger(VideoLurker.class);
    private static final String OSX_EXCLUDES = "(Macintosh HD|Time Machine Backups|com.apple.TimeMachine.localsnapshots)";
    private final String usbStickDir;

    public VideoLurker(String baseDirName, String videoDirName) {
        baseDir = new File(baseDirName);
        usbStickDir = videoDirName;
        dirWatcher = newSingleThreadScheduledExecutor(r -> new Thread(r, "Video Lurker"));
    }

    private final File baseDir;

    private final ScheduledExecutorService dirWatcher;

    private List<File> dirs = Collections.synchronizedList(new ArrayList<>());

    private Set<String> duds = new HashSet<>();

    /**
     * Matches only movie files we think we can play, not on the duds list.
     */
    private final FileFilter okMovies = f -> !duds.contains(f.getName()) &&
            f.isFile() &&
            f.canRead() &&
            !f.getName().startsWith(".") &&
            !f.getName().toLowerCase().endsWith(".json") &&
            !f.getName().toLowerCase().endsWith(".mov");

    /**
     * Gets a fresh list of directories called {@link #usbStickDir} in USB drives wherein we expect to find video files.
     *
     * @return the list of directories.
     */
    private List<File> getMediaDirs() {
        List<File> newDirs = new ArrayList<>();
        if (baseDir.isDirectory()) {
            File[] drives = baseDir.listFiles(file -> file.isDirectory() && !file.getName().matches(OSX_EXCLUDES));
            if (drives != null) {
                for (File drive : drives) {
                    File[] bhimas = drive.listFiles(f -> f.isDirectory() && f.getName().equalsIgnoreCase(usbStickDir));
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
        duds.add(filename);
    }

}
