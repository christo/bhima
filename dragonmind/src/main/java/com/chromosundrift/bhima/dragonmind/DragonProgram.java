package com.chromosundrift.bhima.dragonmind;

import processing.core.PGraphics;

public interface DragonProgram {

    public void settings(DragonMind mind);

    public void setup(DragonMind mind);

    public PGraphics draw(DragonMind mind, int width, int height);

    void mouseClicked(DragonMind mind);
}
