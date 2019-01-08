package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

public interface DragonProgram {

    public void settings(DragonMind mind);

    public void setup(DragonMind mind);

    public PGraphics draw(DragonMind mind, int width, int height);

    void mouseClicked(DragonMind mind);
}
