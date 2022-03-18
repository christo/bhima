package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

import java.util.List;

/**
 * A program is a runnable visual effect with potentially multiple parametised instances.
 * Corresponds to a Processing Sketch.
 */
public interface DragonProgram {

    /**
     * Pre-setup, runs once post construtor, analogous to Processing's settings.
     */
    void settings(DragonMind mind);

    /**
     * Do one time setup, analogous to Processing's setup.
     */
    void setup(DragonMind mind);

    /**
     * Handle request to draw in the given {@link DragonMind} context and coordinate bounds.
     */
    PGraphics draw(DragonMind mind, int width, int height);

    void mouseClicked(DragonMind mind);

    /**
     * Get all configured instances of this program.
     */
    List<ProgramInfo> getProgramInfos(int x, int y, int w, int h);

    /**
     * Switch to program variant with the given id as returned from the
     * {@link com.chromosundrift.bhima.api.ProgramInfo ProgramInfos} returned from any
     * {@link #getProgramInfos(int, int, int, int)} call.
     */
    ProgramInfo runProgram(String id);

    ProgramInfo getCurrentProgramInfo(int inx, int iny, int innerWidth, int innerHeight);

    boolean isMute();

    void setMute(boolean mute);
}
