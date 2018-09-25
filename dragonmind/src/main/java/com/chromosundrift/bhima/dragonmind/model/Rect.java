package com.chromosundrift.bhima.dragonmind.model;


import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Rectangle implementation based on minimum x,y and maximum x,y points.
 */
public final class Rect {
    private final Point minMin;
    private final Point maxMax;

    public Rect(final Point aCorner, final Point anotherCorner) {
        int x1 = aCorner.getX();
        int x2 = anotherCorner.getX();
        int y1 = aCorner.getY();
        int y2 = anotherCorner.getY();
        this.minMin = new Point(min(x1, x2), min(y1, y2));
        this.maxMax = new Point(max(x1, x2), max(y1, y2));
    }

    public boolean contains(final Point p) {
        final int x = p.getX();
        final int y = p.getY();
        return x >= minMin.getX() && y >= minMin.getY()
                && x <= maxMax.getX() && y <= maxMax.getY();
    }

    public Point getMinMin() {
        return minMin;
    }

    public Point getMaxMax() {
        return maxMax;
    }

    public Rect scaled(float scaleX, float scaleY) {

        int w = maxMax.getX() - minMin.getX();
        int x1 = (int) (minMin.getX() - w*scaleX/2);
        int x2 = (int) (maxMax.getX() + w*scaleX/2);
        int h = maxMax.getY() - minMin.getY();
        int y1 = (int) (minMin.getY() - h*scaleY/2);
        int y2 = (int) (maxMax.getY() + h*scaleY/2);
        return new Rect(new Point(x1, y1), new Point(x2, y2));

    }
}
