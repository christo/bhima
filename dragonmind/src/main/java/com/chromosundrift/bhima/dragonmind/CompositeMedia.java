package com.chromosundrift.bhima.dragonmind;

import java.util.List;

/**
 * Aggregates the built in videos and usb scanning videos into a logically singular implementation.
 */
public class CompositeMedia extends MediaSource {

    private MediaSource preferred;
    private MediaSource fallback;

    public CompositeMedia(MediaSource preferred, MediaSource fallback) {
        this.preferred = preferred;
        this.fallback = fallback;
    }

    @Override
    public List<String> getMedia() {
        final List<String> preferredMedia = preferred.getMedia();
        if (!preferredMedia.isEmpty()) {
            return preferredMedia;
        } else {
            return fallback.getMedia();
        }
    }

}
