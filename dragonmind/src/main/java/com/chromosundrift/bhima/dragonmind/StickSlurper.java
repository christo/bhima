package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Watches for specific mounted filesystem and slurps everything in /bhima/ reading the contents as
 * animations to play.
 */
public class StickSlurper {

    private static final Logger logger = LoggerFactory.getLogger(StickSlurper.class);
    public static final String EXCLUDE_DIRS = "(Macintosh HD|Time Machine Backups)";
    private final File baseDir = new File("/Volumes");
    private final Pattern excludeRegex = Pattern.compile(EXCLUDE_DIRS);

    private final ScheduledExecutorService dirWatcher = Executors.newSingleThreadScheduledExecutor();

    private List<File> dirs = Collections.synchronizedList(new ArrayList<>());

    private List<File> getMediaDirs() {
        if (baseDir.isDirectory()) {
            File[] mediaDirs = baseDir.listFiles(file -> file.isDirectory() && file.getName().equalsIgnoreCase("bhima"));
            if (mediaDirs != null) {
                dirs.clear();
                dirs.addAll(Arrays.asList(mediaDirs));
            }
        } else {
            logger.error("can't find video dirs in /Volumes dir");
        }
        return dirs;
    }

    public void start() {
        logger.info("starting stick slurpoer");
        dirWatcher.schedule(this::getMediaDirs, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        dirWatcher.shutdown();
    }

    public List<File> getMedia() {
        List<File> media = new ArrayList<>();
        for (File dir : dirs) {
            if (dir.exists() && dir.isFile()) {
                media.addAll(Arrays.asList(dir.listFiles()));
            }
        }
        return media;
    }

}
