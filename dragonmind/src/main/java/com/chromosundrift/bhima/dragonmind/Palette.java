package com.chromosundrift.bhima.dragonmind;

/**
 * Baby two-colour palette implementation. More later.
 */
public final class Palette {

    private final int light;
    private final int dark;
    private int colour;

    public Palette(int light, int dark) {
        this.light = light;
        this.dark = dark;
        this.colour = dark;
    }

    public int next() {
        if (isDark()) {
            this.colour = light;
        } else {
            this.colour = dark;
        }
        return this.colour;
    }

    public boolean isDark() {
        return this.colour == dark;
    }

    public int getColour() {
        return colour;
    }

    public int getDark() {
        return dark;
    }

    public int getLight() {
        return light;
    }

    public String getColourName() {
        return isDark() ? "dark" : "light";
    }

    public void setLight() {
        this.colour = light;
    }
    public void setDark() {
        this.colour = dark;
    }
}
