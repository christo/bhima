package com.chromosundrift.bhima.geometry;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Threadsafe mutable 2D cartesian coordinate.
 */
@SuppressWarnings("WeakerAccess")
public final class Point {

    private final ReentrantLock mutate = new ReentrantLock();

    public static Point centroid(Iterable<Point> points) {
        int avgX = 0;
        int avgY = 0;
        int n = 0;

        for (Point p : points) {
            n++;
            avgX += p.x;
            avgY += p.y;
        }

        avgX /= n;
        avgY /= n;
        return new Point(avgX, avgY);
    }

    public static double variance(Collection<Point> points) {
        Point centroid = centroid(points);
        double sumD2 = 0;
        for (Point point : points) {

            long d2 = Math.abs(point.x - centroid.x) ^ 2
                    + Math.abs(point.y - centroid.y) ^ 2;
            sumD2 += d2;
        }
        return sumD2 / points.size();
    }

    private int x;

    private int y;

    public Point(int ix, int iy) {
        this.x = ix;
        this.y = iy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void moveTo(int x, int y) {
        try {
            mutate.lock();
            this.x = x;
            this.y = y;
        } finally {
            mutate.unlock();
        }
    }

    public void moveTo(Supplier<Point> point) {
        try {
            mutate.lock();
            Point to = point.get();
            this.x = to.getX();
            this.y = to.getY();
        } finally {
            mutate.unlock();
        }
    }

    public void moveTo(Point point) {
        try {
            mutate.lock();
            Point to = point.copy();
            this.x = to.getX();
            this.y = to.getY();
        } finally {
            mutate.unlock();
        }
    }

    public Point copy() {
        try {
            mutate.lock();
            return new Point(x, y);
        } finally {
            mutate.unlock();
        }
    }
}
