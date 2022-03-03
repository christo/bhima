package com.chromosundrift.bhima.dragonmind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.util.stream.Collectors.toList;

public abstract class MediaSource {

    private static final Logger logger = LoggerFactory.getLogger(MediaSource.class);

    private Set<String> duds;

    public MediaSource() {
        this.duds = ConcurrentHashMap.newKeySet();
    }

    public void excludeMovieFile(String filename) {
        logger.info("excluding movie {}", filename);
        duds.add(filename);
    }

    public abstract List<String> getMedia();

    /**
     * Fetch a list of file names of all regular files under the given root to the given depth.
     *
     * @param root  path to start finding.
     * @param depth dig no deeper.
     * @return the file names.
     * @throws IOException probably only if permissions are bad.
     */
    protected List<String> collectFilenames(Path root, int depth) throws IOException {
        return Files.find(root, depth, this::fileOk, FOLLOW_LINKS)
                .map(Path::toString)
                .sorted()
                .collect(toList());
    }

    private boolean fileOk(Path p, BasicFileAttributes bfa) {
        String filename = p.getFileName().toString();
        return !filename.contains(".json") &&
                bfa.isRegularFile() &&
                !filename.startsWith(".") &&
                !filename.endsWith(".mov") &&
                p.toFile().canRead() &&
                !duds.contains(p.toString());
    }
}
