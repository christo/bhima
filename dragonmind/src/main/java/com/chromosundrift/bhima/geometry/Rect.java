package com.chromosundrift.bhima.geometry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Rectangle implementation based on minimum x,y (top left) and maximum x,y (bottom right) points.
 */
public final class Rect {

    private static final Logger logger = LoggerFactory.getLogger(Rect.class);

    private final Point minMin;
    private final Point maxMax;

    public Rect(int x1, int y1, int x2, int y2) {
        this.minMin = new Point(min(x1, x2), min(y1, y2));
        this.maxMax = new Point(max(x1, x2), max(y1, y2));
    }

    public Rect(final Point aCorner, final Point oppositeCorner) {
        this(aCorner.getX(), aCorner.getY(), oppositeCorner.getX(), oppositeCorner.getY());
    }

    public boolean contains(int x, int y) {
        return x >= minMin.getX() && y >= minMin.getY() && x <= maxMax.getX() && y <= maxMax.getY();
    }

    public boolean contains(final Point p) {
        final int x = p.getX();
        final int y = p.getY();
        return contains(x, y);
    }

    public Point getMinMin() {
        return minMin;
    }

    public Point getMaxMax() {
        return maxMax;
    }

    @Override
    public String toString() {
        return "Rect{" + minMin + ", " + maxMax + '}';
    }
}
