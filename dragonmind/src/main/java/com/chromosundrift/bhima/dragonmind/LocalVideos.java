package com.chromosundrift.bhima.dragonmind;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LocalVideos extends MediaSource {

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
    public List<String> getMedia() throws IOException {
        return collectFilenames(dir, MAX_DEPTH);
    }

}
