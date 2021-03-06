package com.chromosundrift.bhima.dragonmind.model;

import com.chromosundrift.bhima.geometry.Point;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class PixelPoint {
    private final Point point;
    private final int strip;
    private final int pixel;

    public PixelPoint(int stripIndex, int pixelIndex, int x, int y) {
        this.strip = stripIndex;
        this.pixel = pixelIndex;
        this.point = new Point(x, y);
    }

    public PixelPoint(int stripIndex, int pixelIndex, Point point) {
        this.point = point;
        this.strip = stripIndex;
        this.pixel = pixelIndex;
    }

    public int getStrip() {
        return strip;
    }

    public int getPixel() {
        return pixel;
    }

    public int getX() {
        return point.getX();
    }

    public int getY() {
        return point.getY();
    }

    public Point getPoint() {
        return point;
    }

    public static String columnHeaders() {
        return "strip,pixel,x,y";
    }

    @Override
    public String toString() {
        return this.strip + "," + this.pixel + "," + this.point.getX() + "," + this.point.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PixelPoint that = (PixelPoint) o;
        return strip == that.strip &&
                pixel == that.pixel &&
                Objects.equals(point, that.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, strip, pixel);
    }
}
