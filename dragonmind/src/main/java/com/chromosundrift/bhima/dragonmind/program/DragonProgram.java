package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

import java.util.List;

public interface DragonProgram {

    /** presetup, runs once post ctor. */
    void settings(DragonMind mind);

    void setup(DragonMind mind);

    PGraphics draw(DragonMind mind, int width, int height);

    void mouseClicked(DragonMind mind);

    List<ProgramInfo> getProgramInfos(int x, int y, int w, int h);

    /** switch to program variant with the given id as returned from the
     * {@link com.chromosundrift.bhima.api.ProgramInfo ProgramInfos} returned from any
     * {@link #getProgramInfos(int, int, int, int)} call.
     *
     */
    ProgramInfo runProgram(String id);
}
