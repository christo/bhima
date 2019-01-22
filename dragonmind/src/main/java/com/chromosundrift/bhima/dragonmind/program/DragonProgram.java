package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

public interface DragonProgram {

    void settings(DragonMind mind);

    void setup(DragonMind mind);

    PGraphics draw(DragonMind mind, int width, int height);

    void mouseClicked(DragonMind mind);
}
