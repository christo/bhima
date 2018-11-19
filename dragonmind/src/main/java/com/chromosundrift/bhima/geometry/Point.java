package com.chromosundrift.bhima.geometry;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class Point {

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
        this.x = x;
        this.y = y;
    }
}
