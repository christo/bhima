package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class LocalVideos extends MediaSource {

    private static final Logger logger = LoggerFactory.getLogger(LocalVideos.class);
    private static final int MAX_DEPTH = 2;

    private Path dir;

    public LocalVideos(String directory) throws IOException {
        final File dir = new File(directory);
        if (!dir.isDirectory() || !dir.canRead()) {
            throw new IOException(String.format("can't read %s as a directorty", directory));
        }
        this.dir = dir.toPath();
    }

    @Override
    public List<String> getMedia() {
        logger.debug("Getting media from built-in videos");
        try {
            return collectFilenames(dir, MAX_DEPTH);
        } catch (IOException e) {
            logger.error("Can't collect filenames for local videos.", e);
            return Collections.emptyList();
        }
    }

}
