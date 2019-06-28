package com.chromosundrift.bhima.dragonmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RainbowPalette {

    private final List<NamedColour> colours;

    RainbowPalette() {
        List<NamedColour> c = new ArrayList<>();
        c.add(new NamedColour("Violet", 148, 0, 211));
        c.add(new NamedColour("Indigo", 75, 0, 130));
        c.add(new NamedColour("Blue", 0, 0, 255));
        c.add(new NamedColour("Green", 0, 255, 0));
        c.add(new NamedColour("Yellow", 255, 255, 0));
        c.add(new NamedColour("Orange", 255, 127, 0));
        c.add(new NamedColour("Red", 255, 0, 0));
        this.colours = Collections.unmodifiableList(c);
    }

    List<NamedColour> getColours() {
        return colours;
    }
}
