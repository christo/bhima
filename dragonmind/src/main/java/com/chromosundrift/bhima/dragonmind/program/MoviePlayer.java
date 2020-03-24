package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PGraphics;

import java.util.List;

import static com.chromosundrift.bhima.api.ProgramInfo.NULL_PROGRAM_INFO;
import static java.util.Collections.singletonList;

public interface MoviePlayer extends DragonProgram {

    ProgramInfo getCurrentProgramInfo(int inx, int iny, int innerWidth, int innerHeight);

    class NullMoviePlayer implements MoviePlayer {

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
            return NULL_PROGRAM_INFO;
        }

        @Override
        public List<ProgramInfo> getProgramInfos(int inx, int iny, int innerWidth, int innerHeight) {
            return singletonList(NULL_PROGRAM_INFO);
        }

        @Override
        public ProgramInfo runProgram(String id) {
            return NULL_PROGRAM_INFO;
        }
    }
}
