package com.chromosundrift.bhima.dragonmind;

import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public final class CachingImageLoader {

    private static final Logger logger = LoggerFactory.getLogger(CachingImageLoader.class);

    private final Map<String, BufferedImage> imageCache;
    private final Map<String, PImage> pImageCache;
    private final ReentrantLock lock = new ReentrantLock();

    public CachingImageLoader(int max) {
        this.imageCache = new LRUMap<>(max);
        this.pImageCache = new LRUMap<>(max);
    }

    public BufferedImage loadImage(String filename) throws IOException {
        lock.lock();
        try {
            BufferedImage cached = imageCache.get(filename);
            if (cached!= null) {
                return cached;
            } else {
                BufferedImage image = ImageIO.read(new File(filename));
                if (image == null) {
                    throw new NullPointerException("wot?");
                }
                imageCache.put(filename, image);
                return image;
            }
        } finally {
            lock.unlock();
        }
    }

    public PImage loadPimage(String filename) throws IOException {
        lock.lock();
        try {
            PImage cached = pImageCache.get(filename);
            if (cached != null) {
                return cached;
            } else {
                PImage pImage = new PImage(loadImage(filename));
                pImageCache.put(filename, pImage);
                return pImage;
            }
        } finally {
            lock.unlock();
        }
    }

}
