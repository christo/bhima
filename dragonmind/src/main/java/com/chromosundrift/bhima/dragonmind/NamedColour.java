package com.chromosundrift.bhima.dragonmind;

final class NamedColour {
    private final String name;
    private final int red;
    private final int green;
    private final int blue;

    public NamedColour(String name, int red, int green, int blue) {
        this.name = name;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String getName() {
        return name;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }
}
