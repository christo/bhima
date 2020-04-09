package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.stream.Collectors.toList;

/**
 * Watches for specific mounted filesystems and slurps everything in the configured directory, reading the contents as
 * animations to play.
 */
public class VideoLurker extends MediaSource {

    private static final Logger logger = LoggerFactory.getLogger(VideoLurker.class);
    private static final String OSX_EXCLUDES = "(Macintosh HD|Time Machine Backups|com.apple.TimeMachine.localsnapshots)";
    private final String usbStickDir;
    private final File baseDir;

    public VideoLurker(String baseDirName, String videoDirName) {
        baseDir = new File(baseDirName);
        usbStickDir = videoDirName;
        dirWatcher = newSingleThreadScheduledExecutor(r -> new Thread(r, "Video Lurker"));
    }

    private final ScheduledExecutorService dirWatcher;

    private List<File> dirs = Collections.synchronizedList(new ArrayList<>());


    /**
     * Gets a fresh list of video files based on the following pattern:
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
        dirs.clear();
        dirs.addAll(newDirs);
        if (dirs.size() == 0) {
            logger.info("Media dirs: None");
        } else {
            logger.info("Media dirs: {}", dirs.stream().map(File::getAbsolutePath).collect(toList()));
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

    @Override
    public List<String> getMedia() {
        List<String> media = new ArrayList<>();
        for (File dir : dirs) {
            // double check it's still there
            final String dirname = dir.getAbsolutePath();
            if (dir.exists() && dir.isDirectory()) {
                try {
                    final List<String> files = collectFilenames(dir.toPath(), 2);
                    media.addAll(files);
                    logger.info("added {} files from {}", files.size(), dirname);
                } catch (IOException e) {
                    logger.warn("Unable to read media dir {}", dirname);
                }
            } else {
                logger.warn("skipping bad dir: {}", dirname);
            }
        }
        return media;
    }

}
