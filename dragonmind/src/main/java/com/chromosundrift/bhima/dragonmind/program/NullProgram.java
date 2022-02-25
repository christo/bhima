package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

import java.util.List;

import static com.chromosundrift.bhima.api.ProgramInfo.getNullProgramInfo;
import static java.util.Collections.singletonList;

public class NullProgram implements DragonProgram {

    PGraphics screenBuffer = new PGraphics();

    @Override
    public void settings(DragonMind mind) {
    }

    @Override
    public void setup(DragonMind mind) {

    }

    @Override
    public void mouseClicked(DragonMind mind) {

    }

    @Override
    public PGraphics draw(DragonMind mind, int width, int height) {
        return screenBuffer;
    }

    @Override
    public ProgramInfo getCurrentProgramInfo(int inx, int iny, int innerWidth, int innerHeight) {
        return ProgramInfo.getNullProgramInfo();
    }

    @Override
    public List<ProgramInfo> getProgramInfos(int inx, int iny, int innerWidth, int innerHeight) {
        return singletonList(ProgramInfo.getNullProgramInfo());
    }

    @Override
    public ProgramInfo runProgram(String id) {
        return ProgramInfo.getNullProgramInfo();
    }
}
